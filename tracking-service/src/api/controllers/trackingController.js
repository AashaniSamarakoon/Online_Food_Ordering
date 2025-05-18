const trackingService = require("../../services/trackingService");
const logger = require("../../utils/logger");

/**
 * Get driver location
 */
exports.getDriverLocation = async (req, res) => {
  try {
    const { driverId } = req.params;

    const location = await trackingService.getDriverLocation(driverId);

    if (!location) {
      return res.status(404).json({ message: "Driver location not found" });
    }

    return res.status(200).json(location);
  } catch (error) {
    logger.error(`Error getting driver location: ${error.message}`, { error });
    return res.status(500).json({ message: "Internal server error" });
  }
};

/**
 * Update driver location
 */
exports.updateDriverLocation = async (req, res) => {
  try {
    const { driverId } = req.params;
    const locationData = req.body;

    // Validate required fields
    if (!locationData.latitude || !locationData.longitude) {
      return res
        .status(400)
        .json({ message: "Latitude and longitude are required" });
    }

    const updatedLocation = await trackingService.updateDriverLocation(
      driverId,
      {
        latitude: locationData.latitude,
        longitude: locationData.longitude,
        accuracy: locationData.accuracy || 0,
        speed: locationData.speed || 0,
        bearing: locationData.bearing || 0,
        timestamp: locationData.timestamp || new Date().toISOString(),
      }
    );

    // Emit socket event if available
    if (req.app.io) {
      req.app.io.emit("driver_location_updated", {
        driverId,
        location: updatedLocation,
      });
    }

    return res.status(200).json(updatedLocation);
  } catch (error) {
    logger.error(`Error updating driver location: ${error.message}`, { error });
    return res.status(500).json({ message: "Internal server error" });
  }
};

/**
 * Get trip status
 */
exports.getTripStatus = async (req, res) => {
  try {
    const { orderId } = req.params;

    const trip = await trackingService.getTripStatus(orderId);

    if (!trip) {
      return res.status(404).json({ message: "Trip not found" });
    }

    return res.status(200).json(trip);
  } catch (error) {
    logger.error(`Error getting trip status: ${error.message}`, { error });
    return res.status(500).json({ message: "Internal server error" });
  }
};

/**
 * Create a new trip
 */
exports.createTrip = async (req, res) => {
  try {
    const tripData = req.body;

    // Validate required fields
    if (
      !tripData.orderId ||
      !tripData.driverId ||
      !tripData.customerId ||
      !tripData.waypoints
    ) {
      return res.status(400).json({ message: "Missing required fields" });
    }

    const trip = await trackingService.createTrip(tripData);

    return res.status(201).json(trip);
  } catch (error) {
    logger.error(`Error creating trip: ${error.message}`, { error });
    return res.status(500).json({ message: "Internal server error" });
  }
};

/**
 * Get nearby drivers
 */
exports.getNearbyDrivers = async (req, res) => {
  try {
    const { latitude, longitude, radius, limit } = req.query;

    if (!latitude || !longitude) {
      return res
        .status(400)
        .json({ message: "Latitude and longitude are required" });
    }

    const drivers = await trackingService.getNearbyDrivers(
      parseFloat(latitude),
      parseFloat(longitude),
      radius ? parseFloat(radius) : 5000,
      limit ? parseInt(limit) : 10
    );

    console.log("Sending response:", JSON.stringify(drivers));

    return res.status(200).json(drivers);
  } catch (error) {
    logger.error(`Error finding nearby drivers: ${error.message}`, { error });
    return res.status(500).json({ message: "Internal server error" });
  }
};

/**
 * Get heatmap data
 */
exports.getHeatmapData = async (req, res) => {
  try {
    const { startTime, endTime, resolution } = req.query;

    if (!startTime || !endTime) {
      return res
        .status(400)
        .json({ message: "Start time and end time are required" });
    }

    const data = await trackingService.generateHeatmapData(
      new Date(startTime),
      new Date(endTime),
      resolution ? parseFloat(resolution) : 0.01
    );

    return res.status(200).json(data);
  } catch (error) {
    logger.error(`Error generating heatmap data: ${error.message}`, { error });
    return res.status(500).json({ message: "Internal server error" });
  }
};
