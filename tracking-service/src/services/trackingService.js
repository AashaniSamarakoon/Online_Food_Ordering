const Location = require("../models/Location");
const Trip = require("../models/Trip");
const { getRedisClient } = require("../config/redis");
const { calculateETA, calculateDistance } = require("../utils/geoUtils");
const logger = require("../utils/logger");
const { orderClient, driverClient, notificationClient } = require("../clients");

const redis = getRedisClient();
const LOCATION_CACHE_TTL = 300; // 5 minutes in seconds

const isMongoConnected = () => {
  return mongoose.connection && mongoose.connection.readyState === 1;
};

// Then in each function that requires MongoDB:
async function getDriverLocation(driverId) {
  try {
    // Try to get from Redis first
    const locationKey = `driver:${driverId}:location`;
    const cachedLocation = await redis.get(locationKey);

    if (cachedLocation) {
      return JSON.parse(cachedLocation);
    }

    // If MongoDB is not connected, return null instead of querying
    if (!isMongoConnected()) {
      logger.warn(
        "MongoDB not connected, cannot retrieve driver location from database"
      );
      return null;
    }

    // Fall back to database
    const location = await Location.findOne({ driverId })
      .sort({ timestamp: -1 })
      .limit(1);

    if (!location) {
      return null;
    }

    // Rest of the function...
  } catch (error) {
    logger.error(`Error getting driver location: ${error.message}`, {
      driverId,
      error,
    });
    return null; // Return null instead of throwing
  }
}
/**
 * Update driver location
 */
async function updateDriverLocation(driverId, locationData) {
  try {
    const {
      latitude,
      longitude,
      speed,
      heading,
      accuracy,
      batteryLevel,
      status,
    } = locationData;

    // Store in MongoDB
    const location = new Location({
      driverId,
      location: {
        type: "Point",
        coordinates: [longitude, latitude],
      },
      speed,
      heading,
      accuracy,
      batteryLevel,
      status,
      timestamp: new Date(),
    });

    await location.save();

    // Cache the latest location in Redis
    const locationKey = `driver:${driverId}:location`;
    await redis.set(
      locationKey,
      JSON.stringify(locationData),
      "EX",
      LOCATION_CACHE_TTL
    );

    // Update active trips for this driver
    await updateActiveTrips(driverId, locationData);

    return { success: true, location };
  } catch (error) {
    logger.error(`Error updating driver location: ${error.message}`, {
      driverId,
      error,
    });
    throw error;
  }
}

/**
 * Get the latest driver location
 */
async function getDriverLocation(driverId) {
  try {
    // Try to get from Redis first
    const locationKey = `driver:${driverId}:location`;
    const cachedLocation = await redis.get(locationKey);

    if (cachedLocation) {
      return JSON.parse(cachedLocation);
    }

    // Fall back to database
    const location = await Location.findOne({ driverId })
      .sort({ timestamp: -1 })
      .limit(1);

    if (!location) {
      return null;
    }

    const locationData = {
      latitude: location.location.coordinates[1],
      longitude: location.location.coordinates[0],
      speed: location.speed,
      heading: location.heading,
      accuracy: location.accuracy,
      batteryLevel: location.batteryLevel,
      status: location.status,
      timestamp: location.timestamp,
    };

    // Update cache
    await redis.set(
      locationKey,
      JSON.stringify(locationData),
      "EX",
      LOCATION_CACHE_TTL
    );

    return locationData;
  } catch (error) {
    logger.error(`Error getting driver location: ${error.message}`, {
      driverId,
      error,
    });
    throw error;
  }
}

/**
 * Create a new trip
 */
async function createTrip(tripData) {
  try {
    const {
      orderId,
      driverId,
      customerId,
      waypoints,
      estimatedDistance,
      estimatedDuration,
    } = tripData;

    // Format waypoints
    const formattedWaypoints = waypoints.map((wp) => ({
      type: wp.type,
      location: {
        type: "Point",
        coordinates: [wp.longitude, wp.latitude],
      },
      address: wp.address,
      status: "PENDING",
    }));

    // Calculate ETA
    const now = new Date();
    const eta = new Date(now.getTime() + estimatedDuration * 1000);

    const trip = new Trip({
      orderId,
      driverId,
      customerId,
      status: "SCHEDULED",
      estimatedArrivalTime: eta,
      waypoints: formattedWaypoints,
      distance: estimatedDistance,
      duration: estimatedDuration,
      currentEta: estimatedDuration,
    });

    await trip.save();

    // Cache the trip for quick access
    const tripKey = `trip:${orderId}`;
    await redis.set(tripKey, JSON.stringify(trip), "EX", 86400); // 24 hours

    return trip;
  } catch (error) {
    logger.error(`Error creating trip: ${error.message}`, { error });
    throw error;
  }
}

/**
 * Update active trips based on driver location
 */
async function updateActiveTrips(driverId, locationData) {
  try {
    const activeTrips = await Trip.find({
      driverId,
      status: { $in: ["SCHEDULED", "IN_PROGRESS"] },
    });

    if (activeTrips.length === 0) return;

    for (const trip of activeTrips) {
      // Get next waypoint
      const nextWaypoint = trip.waypoints.find((wp) => wp.status === "PENDING");
      if (!nextWaypoint) continue;

      // Calculate distance to next waypoint
      const distance = calculateDistance(
        locationData.latitude,
        locationData.longitude,
        nextWaypoint.location.coordinates[1],
        nextWaypoint.location.coordinates[0]
      );

      // Calculate new ETA
      const eta = calculateETA(distance, locationData.speed || 10);
      trip.currentEta = eta;

      // Check if driver has arrived at waypoint (within 50 meters)
      if (distance <= 50) {
        nextWaypoint.status = "ARRIVED";
        nextWaypoint.arrivalTime = new Date();

        // If this is the first waypoint and trip is SCHEDULED, mark as IN_PROGRESS
        if (
          trip.status === "SCHEDULED" &&
          trip.waypoints.filter((wp) => wp.status === "ARRIVED").length === 1
        ) {
          trip.status = "IN_PROGRESS";
          trip.startTime = new Date();

          // Notify customer that driver has started the delivery
          await notificationClient.sendDeliveryStartedNotification(
            trip.customerId,
            trip.orderId
          );
        }

        // If this is a dropoff waypoint, notify customer
        if (nextWaypoint.type === "DROPOFF") {
          await notificationClient.sendDriverArrivedNotification(
            trip.customerId,
            trip.orderId
          );
        }
      }

      // If all waypoints are ARRIVED or COMPLETED, mark trip as COMPLETED
      const pendingWaypoints = trip.waypoints.filter(
        (wp) => wp.status === "PENDING"
      );
      if (pendingWaypoints.length === 0) {
        trip.status = "COMPLETED";
        trip.endTime = new Date();

        // Notify customer that delivery is complete
        await notificationClient.sendDeliveryCompletedNotification(
          trip.customerId,
          trip.orderId
        );

        // Update order status via client
        await orderClient.updateOrderStatus(trip.orderId, "DELIVERED");

        // Update driver status to AVAILABLE
        await driverClient.updateDriverStatus(trip.driverId, "AVAILABLE");
      }

      await trip.save();

      // Update cached trip
      const tripKey = `trip:${trip.orderId}`;
      await redis.set(tripKey, JSON.stringify(trip), "EX", 86400);
    }
  } catch (error) {
    logger.error(`Error updating active trips: ${error.message}`, {
      driverId,
      error,
    });
  }
}

/**
 * Get trip status and tracking information
 */
async function getTripStatus(orderId) {
  try {
    // Try to get from Redis first
    const tripKey = `trip:${orderId}`;
    const cachedTrip = await redis.get(tripKey);

    if (cachedTrip) {
      const trip = JSON.parse(cachedTrip);

      // If the trip is active, get the latest driver location
      if (trip.status !== "COMPLETED" && trip.status !== "CANCELLED") {
        const driverLocation = await getDriverLocation(trip.driverId);
        if (driverLocation) {
          trip.driverLocation = driverLocation;
        }
      }

      return trip;
    }

    // Fall back to database
    const trip = await Trip.findOne({ orderId });
    if (!trip) {
      return null;
    }

    // If the trip is active, get the latest driver location
    if (trip.status !== "COMPLETED" && trip.status !== "CANCELLED") {
      const driverLocation = await getDriverLocation(trip.driverId);
      if (driverLocation) {
        trip._doc.driverLocation = driverLocation;
      }
    }

    // Cache the trip
    await redis.set(tripKey, JSON.stringify(trip), "EX", 86400);

    return trip;
  } catch (error) {
    logger.error(`Error getting trip status: ${error.message}`, {
      orderId,
      error,
    });
    throw error;
  }
}

/**
 * Get nearby drivers within a given radius
 */
async function getNearbyDrivers(
  latitude,
  longitude,
  radius = 5000,
  limit = 10
) {
  try {
    const drivers = await Location.aggregate([
      {
        $geoNear: {
          near: {
            type: "Point",
            coordinates: [parseFloat(longitude), parseFloat(latitude)],
          },
          distanceField: "distance",
          maxDistance: radius,
          query: { status: "IDLE" },
          spherical: true,
        },
      },
      {
        $sort: { distance: 1 },
      },
      {
        $group: {
          _id: "$driverId",
          location: { $first: "$location" },
          distance: { $first: "$distance" },
          timestamp: { $first: "$timestamp" },
        },
      },
      {
        $limit: limit,
      },
    ]);

    return drivers.map((driver) => ({
      driverId: driver._id,
      latitude: driver.location.coordinates[1],
      longitude: driver.location.coordinates[0],
      distance: driver.distance,
      timestamp: driver.timestamp,
    }));
  } catch (error) {
    logger.error(`Error finding nearby drivers: ${error.message}`, { error });
    throw error;
  }
}

/**
 * Generate heatmap data for analytics
 */
async function generateHeatmapData(startTime, endTime, resolution = 0.01) {
  try {
    // Get all location points within time range
    const locations = await Location.find({
      timestamp: { $gte: startTime, $lte: endTime },
    });

    const heatmapData = {};

    // Group points by grid cells based on resolution
    locations.forEach((loc) => {
      const lat = loc.location.coordinates[1];
      const lng = loc.location.coordinates[0];

      // Create grid key based on resolution
      const latGrid = Math.floor(lat / resolution) * resolution;
      const lngGrid = Math.floor(lng / resolution) * resolution;
      const key = `${latGrid.toFixed(4)},${lngGrid.toFixed(4)}`;

      if (!heatmapData[key]) {
        heatmapData[key] = {
          lat: latGrid,
          lng: lngGrid,
          count: 0,
        };
      }

      heatmapData[key].count++;
    });

    return Object.values(heatmapData);
  } catch (error) {
    logger.error(`Error generating heatmap data: ${error.message}`, { error });
    throw error;
  }
}

module.exports = {
  updateDriverLocation,
  getDriverLocation,
  createTrip,
  getTripStatus,
  getNearbyDrivers,
  generateHeatmapData,
};
