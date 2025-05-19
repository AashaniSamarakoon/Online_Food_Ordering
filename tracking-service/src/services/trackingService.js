const Location = require("../models/Location");
const Trip = require("../models/Trip");
const { getRedisClient } = require("../config/redis");
const { calculateETA, calculateDistance } = require("../utils/geoUtils");
const logger = require("../utils/logger");
const { notificationClient } = require("../clients/notificationClient");
const { orderClient } = require("../clients/orderClient");
const { driverClient } = require("../clients/driverClient");

const redis = getRedisClient();
const LOCATION_CACHE_TTL = 300; // 5 minutes in seconds

const isMongoConnected = () => {
  return mongoose.connection && mongoose.connection.readyState === 1;
};

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

    // //  Update driver status to AVAILABLE
    // await driverClient.updateDriverStatus(location.driverId, "AVAILABLE");

    return { success: true, location };
  } catch (error) {
    logger.error(`Error updating driver location: ${error.message}`, {
      driverId,
      error,
    });
    throw error;
  }
}

// /**
//  * Create a new trip
//  */
// async function createTrip(tripData) {
//   try {
//     const {
//       orderId,
//       driverId,
//       customerId,
//       waypoints,
//       estimatedDistance,
//       estimatedDuration,
//       mapApiData // Add this parameter to accept map API response
//     } = tripData;

//     // Format waypoints
//     const formattedWaypoints = waypoints.map((wp) => ({
//       type: wp.type,
//       location: {
//         type: "Point",
//         coordinates: [wp.longitude, wp.latitude],
//       },
//       address: wp.address,
//       status: "PENDING",
//     }));

//     // Calculate ETA using map API data
//     const now = new Date();
//     const eta = new Date(now.getTime() + estimatedDuration * 1000);

//     const trip = new Trip({
//       orderId,
//       driverId,
//       customerId,
//       status: "SCHEDULED",
//       estimatedArrivalTime: eta,
//       waypoints: formattedWaypoints,
//       distance: estimatedDistance,
//       duration: estimatedDuration,
//       originalEta: estimatedDuration, // Store original ETA
//       currentEta: estimatedDuration,
//       etaUpdatedAt: now,

//       // Store detailed route information from map API
//       route: {
//         type: "LineString",
//         coordinates: mapApiData?.route?.coordinates || [],
//         polylineEncoded: mapApiData?.route?.polyline,
//         trafficConditions: mapApiData?.trafficSegments || []
//       },

//       // Store map provider metadata
//       mapData: {
//         provider: mapApiData?.provider || "Unknown",
//         routeId: mapApiData?.routeId,
//         alternativeRoutes: mapApiData?.alternativeRoutes || 0,
//         trafficLevel: mapApiData?.trafficLevel || "NORMAL"
//       }
//     });

//     await trip.save();

//     // Cache the trip for quick access
//     const tripKey = `trip:${orderId}`;
//     await redis.set(tripKey, JSON.stringify(trip), "EX", 86400); // 24 hours

//     return trip;
//   } catch (error) {
//     logger.error(`Error creating trip: ${error.message}`, { error });
//     throw error;
//   }
// }

/**
 * Create a preliminary trip for driver assignment
 * This creates a lightweight trip record before driver accepts
 */
async function createPendingTrip(tripData) {
  try {
    const {
      orderId,
      customerId,
      waypoints,
      estimatedDistance,
      estimatedDuration,
      mapApiData,
    } = tripData;

    // Create a trip without assigning a driver yet
    const trip = new Trip({
      orderId,
      customerId,
      // No driverId yet - will be assigned later
      status: "PENDING_ACCEPTANCE", // Special status before driver accepts
      estimatedArrivalTime: new Date(Date.now() + estimatedDuration * 1000),
      waypoints: waypoints.map((wp) => ({
        type: wp.type,
        location: {
          type: "Point",
          coordinates: [wp.longitude, wp.latitude],
        },
        address: wp.address,
        status: "PENDING",
      })),
      distance: estimatedDistance,
      duration: estimatedDuration,
      originalEta: estimatedDuration,
      currentEta: estimatedDuration,
      etaUpdatedAt: new Date(),
      route: {
        type: "LineString",
        coordinates: mapApiData?.route?.coordinates || [],
        polylineEncoded: mapApiData?.route?.polyline,
        trafficConditions: mapApiData?.trafficSegments || [],
      },
      mapData: {
        provider: mapApiData?.provider || "Unknown",
        routeId: mapApiData?.routeId,
        alternativeRoutes: mapApiData?.alternativeRoutes || 0,
        trafficLevel: mapApiData?.trafficLevel || "NORMAL",
      },
    });

    await trip.save();

    return trip;
  } catch (error) {
    logger.error(`Error creating pending trip: ${error.message}`, { error });
    throw error;
  }
}

/**
 * Assign driver to existing trip after acceptance
 */
async function assignDriverToTrip(orderId, driverId) {
  try {
    // Find the existing trip
    const trip = await Trip.findOne({ orderId });
    if (!trip) {
      throw new Error(`No trip found for order ${orderId}`);
    }

    // Update with driver information
    trip.driverId = driverId;
    trip.status = "SCHEDULED"; // Change from PENDING_ACCEPTANCE to SCHEDULED
    trip.updatedAt = new Date();

    // Get current driver location for ETA refinement
    const driverLocation = await getDriverLocation(driverId);
    if (driverLocation) {
      // Potentially recalculate ETA based on driver's current location
      // This could call your map service again for a more accurate ETA
    }

    await trip.save();

    // Update cache
    const tripKey = `trip:${orderId}`;
    await redis.set(tripKey, JSON.stringify(trip), "EX", 86400);

    return trip;
  } catch (error) {
    logger.error(`Error assigning driver to trip: ${error.message}`, {
      orderId,
      driverId,
      error,
    });
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

      // Calculate ETA using distance and driver's actual speed
      let eta;
      if (locationData.speed && locationData.speed > 1) {
        // If driver is moving, calculate based on current speed
        eta = distance / locationData.speed;
      } else {
        // If driver is stopped or speed data is unreliable, use a percentage approach
        // Calculate how far along the route we are
        const totalDistance = trip.distance;
        const remainingPercentage = distance / totalDistance;

        // Apply the percentage to the original duration from the map API
        eta = trip.originalEta * remainingPercentage;

        // Apply a traffic factor based on time of day if original data is over 5 minutes old
        const now = new Date();
        const timeSinceCreation = (now - trip.createdAt) / 1000; // seconds
        if (timeSinceCreation > 300) {
          // Get current hour (0-23)
          const currentHour = now.getHours();

          // Simple traffic adjustment based on time of day
          if (currentHour >= 7 && currentHour <= 9) eta *= 1.3; // Morning rush
          else if (currentHour >= 16 && currentHour <= 19) eta *= 1.25; // Evening rush
        }
      }

      // Update trip with new ETA
      trip.currentEta = Math.max(60, eta); // Minimum ETA of 1 minute
      trip.etaUpdatedAt = new Date();

      // Check if driver has arrived at waypoint (within 50 meters)
      if (distance <= 50) {
        nextWaypoint.status = "ARRIVED";
        nextWaypoint.arrivalTime = new Date();

        // Rest of your existing waypoint arrival code...
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

// Centralized status update endpoint
async function updateOrderStatus(orderId, updateData) {
  try {
    const { driverId, status, metadata } = updateData;

    // 1. Update local trip data if needed
    let trip = null;
    try {
      trip = await Trip.findOne({ orderId });
      if (trip) {
        // Update trip status based on order status
        if (status === "DRIVER_CONFIRMED") trip.status = "SCHEDULED";
        if (status === "PICKED_UP") trip.status = "IN_PROGRESS";
        if (status === "DELIVERED") trip.status = "COMPLETED";
        if (status === "CANCELLED") trip.status = "CANCELLED";

        await trip.save();
        
        // Update cache
        const tripKey = `trip:${orderId}`;
        await redis.set(tripKey, JSON.stringify(trip), "EX", 86400);
      }
    } catch (tripErr) {
      logger.error(`Failed to update trip: ${tripErr.message}`);
      // Continue even if trip update fails
    }

    // 2. Forward to order service
    try {
      await orderClient.updateOrderStatus(orderId, {
        status,
        driverId,
        ...metadata,
      });
    } catch (orderErr) {
      logger.error(`Failed to update order: ${orderErr.message}`);
    }

    // 3. Update driver status if needed
    if (driverId) {
      try {
        // Different driver status depending on order status
        let driverStatus = "BUSY";
        if (status === "DELIVERED" || status === "CANCELLED") {
          driverStatus = "AVAILABLE";
        }

        await driverClient.updateDriverStatus(driverId, {
          status: driverStatus,
          lastActiveAt: new Date(),
        });
      } catch (driverErr) {
        logger.error(`Failed to update driver: ${driverErr.message}`);
      }
    }

    return {
      success: true,
      message: "Status updated and propagated",
      trip: trip ? trip._id : null,
    };
  } catch (error) {
    logger.error(`Error in update order status: ${error.message}`, {
      orderId,
      error,
    });
    throw error;
  }
}

/**
 * Update waypoint status
 */
async function updateWaypointStatus(orderId, waypointIndex, newStatus) {
  try {
    // Find the trip
    const trip = await Trip.findOne({ orderId });
    if (!trip) {
      throw new Error(`Trip not found for order ${orderId}`);
    }
    
    // Validate waypoint index
    if (waypointIndex < 0 || waypointIndex >= trip.waypoints.length) {
      throw new Error(`Invalid waypoint index: ${waypointIndex}`);
    }
    
    const waypoint = trip.waypoints[waypointIndex];
    const oldStatus = waypoint.status;
    
    // Update waypoint status
    waypoint.status = newStatus;
    
    // Add status-specific updates
    if (newStatus === 'ARRIVED' && !waypoint.arrivalTime) {
      waypoint.arrivalTime = new Date();
    }
    
    if (newStatus === 'COMPLETED' && !waypoint.departureTime) {
      waypoint.departureTime = new Date();
    }
    
    // If first waypoint (pickup) is completed, update trip status to IN_PROGRESS
    if (waypointIndex === 0 && newStatus === 'COMPLETED' && trip.status === 'SCHEDULED') {
      trip.status = 'IN_PROGRESS';
      trip.startTime = new Date();
      
      // Notify order service
      try {
        await orderClient.updateOrderStatus(orderId, {
          status: 'PICKED_UP',
          driverId: trip.driverId
        });
      } catch (err) {
        logger.warn(`Failed to update order status: ${err.message}`);
      }
    }
    
    // If last waypoint is completed, update trip status to COMPLETED
    if (waypointIndex === trip.waypoints.length - 1 && newStatus === 'COMPLETED') {
      trip.status = 'COMPLETED';
      trip.endTime = new Date();
      
      // Notify order service
      try {
        await orderClient.updateOrderStatus(orderId, {
          status: 'DELIVERED',
          driverId: trip.driverId
        });
      } catch (err) {
        logger.warn(`Failed to update order status: ${err.message}`);
      }
      
      // Update driver status to AVAILABLE
      try {
        await driverClient.updateDriverStatus(trip.driverId, {
          status: 'AVAILABLE',
          lastActiveAt: new Date()
        });
      } catch (err) {
        logger.warn(`Failed to update driver status: ${err.message}`);
      }
    }
    
    await trip.save();
    
    // Update cache
    const tripKey = `trip:${orderId}`;
    await redis.set(tripKey, JSON.stringify(trip), "EX", 86400);
    
    return trip;
  } catch (error) {
    logger.error(`Error updating waypoint status: ${error.message}`, { 
      orderId, waypointIndex, newStatus, error 
    });
    throw error;
  }
}

/**
 * Refresh trip route data from external map API when conditions change significantly
 */
async function refreshTripRoute(tripId) {
  try {
    const trip = await Trip.findById(tripId);
    if (!trip || trip.status === "COMPLETED" || trip.status === "CANCELLED") {
      return null;
    }

    // Get latest driver location
    const driverLocation = await getDriverLocation(trip.driverId);
    if (!driverLocation) {
      return null;
    }

    // Find next pending waypoint
    const nextWaypoint = trip.waypoints.find((wp) => wp.status === "PENDING");
    if (!nextWaypoint) {
      return null;
    }

    // Call external map API to get fresh route based on current position
    const mapApiResponse = await mapApiClient.getRoute({
      origin: {
        latitude: driverLocation.latitude,
        longitude: driverLocation.longitude,
      },
      destination: {
        latitude: nextWaypoint.location.coordinates[1],
        longitude: nextWaypoint.location.coordinates[0],
      },
    });

    // Update trip with fresh route data
    trip.distance = mapApiResponse.distance;
    trip.duration = mapApiResponse.duration;
    trip.currentEta = mapApiResponse.duration;
    trip.etaUpdatedAt = new Date();
    trip.route.coordinates =
      mapApiResponse.route.coordinates || trip.route.coordinates;
    trip.route.polylineEncoded =
      mapApiResponse.route.polyline || trip.route.polylineEncoded;

    await trip.save();

    // Update cache
    const tripKey = `trip:${trip.orderId}`;
    await redis.set(tripKey, JSON.stringify(trip), "EX", 86400);

    return trip;
  } catch (error) {
    logger.error(`Error refreshing trip route: ${error.message}`, {
      tripId,
      error,
    });
    return null;
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
    // 1. Get available driver IDs from driver service
    const availableDrivers = await driverClient.getAvailableDrivers();
    console.log("Available drivers:", availableDrivers);

    // Check if we got valid data
    if (
      !availableDrivers ||
      !Array.isArray(availableDrivers) ||
      availableDrivers.length === 0
    ) {
      logger.warn("No available drivers returned from driver service");
      return []; // Return empty array instead of proceeding with invalid data
    }

    // Convert driver IDs to strings to match MongoDB storage format
    const availableDriverIds = availableDrivers
      .map((driver) =>
        driver && driver.driverId ? driver.driverId.toString() : null
      )
      .filter((id) => id !== null); // Filter out any null IDs

    console.log("Available driver IDs:", availableDriverIds);

    // Check if we have location records for these drivers
    const locationCount = await Location.countDocuments({
      driverId: { $in: availableDriverIds },
    });
    console.log(`Found ${locationCount} location records for drivers`);

    if (locationCount === 0) {
      // No location records found, return driver data from driver service
      // Calculate distance for each driver
      return (
        availableDrivers
          .map((driver) => {
            // Calculate distance if coordinates are available
            let distance = 0;
            if (driver.latitude && driver.longitude) {
              distance = calculateDistance(
                parseFloat(latitude),
                parseFloat(longitude),
                driver.latitude,
                driver.longitude
              );
            }

            return {
              driverId: driver.driverId.toString(),
              latitude: driver.latitude,
              longitude: driver.longitude,
              distance: distance,
              timestamp: new Date(),
            };
          })
          // Sort by distance
          .sort((a, b) => a.distance - b.distance)
          // Limit results
          .slice(0, limit)
      );
    }

    console.log(
      `Executing geospatial query for point [${longitude}, ${latitude}] with radius ${radius}m`
    );

    // 2. Use MongoDB geospatial indexing to find nearby drivers from the available pool
    const drivers = await Location.aggregate([
      {
        $geoNear: {
          near: {
            type: "Point",
            coordinates: [parseFloat(longitude), parseFloat(latitude)],
          },
          distanceField: "distance",
          maxDistance: radius,
          query: {
            driverId: { $in: availableDriverIds },
          },
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

    console.log(`Geospatial query returned ${drivers.length} results`);

    if (drivers.length === 0) {
      // IMPORTANT: Fall back to returning the driver data we already have
      // This ensures clients get data even if the geospatial query returns nothing
      console.log(
        "No drivers found in radius, falling back to available drivers"
      );
      return availableDrivers.map((driver) => {
        // Calculate distance
        const distance = calculateDistance(
          parseFloat(latitude),
          parseFloat(longitude),
          driver.latitude,
          driver.longitude
        );

        return {
          driverId: driver.driverId.toString(),
          latitude: driver.latitude,
          longitude: driver.longitude,
          distance: distance,
          timestamp: new Date(),
        };
      });
    }

    // 3. Map to the expected format and include the actual distance
    return drivers.map((driver) => ({
      driverId: driver._id ? driver._id.toString() : driver._id, // Convert ObjectId to string
      latitude: driver.location ? driver.location.coordinates[1] : null,
      longitude: driver.location ? driver.location.coordinates[0] : null,
      distance: driver.distance || 0,
      timestamp: driver.timestamp
        ? new Date(driver.timestamp).toISOString()
        : new Date().toISOString(),
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
  createPendingTrip,
  assignDriverToTrip,
  getTripStatus,
  getNearbyDrivers,
  generateHeatmapData,
  refreshTripRoute,
  updateWaypointStatus,
  updateOrderStatus
};
