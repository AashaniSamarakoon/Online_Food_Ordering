const { v4: uuidv4 } = require('uuid');
const { getRedisClient } = require('../config/redis');
const trackingService = require('../services/trackingService');
const logger = require('../utils/logger');

const redis = getRedisClient();
const connectedUsers = new Map();
const driverSockets = new Map();
const customerSockets = new Map();

function setupSocketHandlers(io) {
  // Create a namespace for driver connections
  const driverNamespace = io.of('/driver');
  
  driverNamespace.on('connection', socket => {
    logger.info('Driver connected to socket', { socketId: socket.id });
    
    // Handle driver authentication
    socket.on('authenticate', async (data) => {
      try {
        const { driverId, token } = data;
        
        // In production, validate the token with your auth service
        // For now, we're just checking if the driverId exists
        
        if (!driverId) {
          socket.emit('authentication_error', { message: 'Driver ID is required' });
          return;
        }
        
        // Associate this socket with the driver
        driverSockets.set(driverId, socket.id);
        socket.driverId = driverId;
        
        socket.emit('authenticated', { success: true });
        logger.info('Driver authenticated', { driverId, socketId: socket.id });
      } catch (error) {
        logger.error('Driver authentication error', { error: error.message });
        socket.emit('authentication_error', { message: error.message });
      }
    });
    
    // Handle location updates from drivers
    socket.on('location_update', async (data) => {
      try {
        const driverId = socket.driverId;
        
        if (!driverId) {
          socket.emit('error', { message: 'Not authenticated' });
          return;
        }
        
        const { latitude, longitude, speed, heading, accuracy, batteryLevel, status } = data;
        
        // Validate required fields
        if (!latitude || !longitude) {
          socket.emit('error', { message: 'Latitude and longitude are required' });
          return;
        }
        
        // Process location update
        await trackingService.updateDriverLocation(driverId, {
          latitude, longitude, speed, heading, accuracy, batteryLevel, status
        });
        
        // Acknowledge update to driver
        socket.emit('location_ack', { success: true, timestamp: new Date() });
        
        // Find active trips for this driver
        const activeTrips = await findActiveTripsForDriver(driverId);
        
        // Notify customers tracking this driver
        if (activeTrips && activeTrips.length > 0) {
          for (const trip of activeTrips) {
            const { orderId, customerId } = trip;
            
            // Find socket IDs for customers tracking this order
            const customerSocketId = customerSockets.get(customerId);
            if (customerSocketId) {
              const customerSocket = io.of('/customer').sockets.get(customerSocketId);
              if (customerSocket) {
                customerSocket.emit('driver_location_update', {
                  orderId,
                  driverId,
                  latitude,
                  longitude,
                  speed,
                  heading,
                  estimatedArrival: trip.currentEta,
                  timestamp: new Date()
                });
              }
            }
          }
        }
      } catch (error) {
        logger.error('Error processing driver location update', { error: error.message, socketId: socket.id });
        socket.emit('error', { message: 'Failed to process location update' });
      }
    });
    
    // Handle disconnection
    socket.on('disconnect', () => {
      if (socket.driverId) {
        driverSockets.delete(socket.driverId);
        logger.info('Driver disconnected', { driverId: socket.driverId });
      }
    });
  });
  
  // Create a namespace for customer connections
  const customerNamespace = io.of('/customer');
  
  customerNamespace.on('connection', socket => {
    logger.info('Customer connected to socket', { socketId: socket.id });
    
    // Handle customer authentication
    socket.on('authenticate', async (data) => {
      try {
        const { customerId, token } = data;
        
        // In production, validate the token with your auth service
        
        if (!customerId) {
          socket.emit('authentication_error', { message: 'Customer ID is required' });
          return;
        }
        
        // Associate this socket with the customer
        customerSockets.set(customerId, socket.id);
        socket.customerId = customerId;
        
        socket.emit('authenticated', { success: true });
        logger.info('Customer authenticated', { customerId, socketId: socket.id });
      } catch (error) {
        logger.error('Customer authentication error', { error: error.message });
        socket.emit('authentication_error', { message: error.message });
      }
    });
    
    // Handle tracking requests
    socket.on('track_order', async (data) => {
      try {
        const { orderId } = data;
        const customerId = socket.customerId;
        
        if (!customerId) {
          socket.emit('error', { message: 'Not authenticated' });
          return;
        }
        
        if (!orderId) {
          socket.emit('error', { message: 'Order ID is required' });
          return;
        }
        
        // Get trip status
        const trip = await trackingService.getTripStatus(orderId);
        
        if (!trip) {
          socket.emit('error', { message: 'Trip not found for this order' });
          return;
        }
        
        // Verify this customer has access to this order
        if (trip.customerId !== customerId) {
          socket.emit('error', { message: 'Unauthorized to track this order' });
          return;
        }
        
        // Subscribe to real-time updates
        socket.join(`order:${orderId}`);
        
        // Send initial status
        socket.emit('trip_status', {
          orderId,
          driverId: trip.driverId,
          status: trip.status,
          waypoints: trip.waypoints,
          estimatedArrival: trip.currentEta,
          driverLocation: trip.driverLocation,
          timestamp: new Date()
        });
        
        logger.info('Customer started tracking order', { customerId, orderId });
      } catch (error) {
        logger.error('Error processing tracking request', { error: error.message });
        socket.emit('error', { message: 'Failed to start tracking' });
      }
    });
    
    // Handle stop tracking
    socket.on('stop_tracking', (data) => {
      const { orderId } = data;
      if (orderId) {
        socket.leave(`order:${orderId}`);
        logger.info('Customer stopped tracking order', { customerId: socket.customerId, orderId });
      }
    });
    
    // Handle disconnection
    socket.on('disconnect', () => {
      if (socket.customerId) {
        customerSockets.delete(socket.customerId);
        logger.info('Customer disconnected', { customerId: socket.customerId });
      }
    });
  });
}

async function findActiveTripsForDriver(driverId) {
  // This is a simplified implementation
  // In a real app, you would query your database for active trips
  const tripKey = `driver:${driverId}:trips`;
  const cachedTrips = await redis.get(tripKey);
  
  if (cachedTrips) {
    return JSON.parse(cachedTrips);
  }
  
  // If not in cache, query the database
  const Trip = require('../models/Trip');
  const trips = await Trip.find({
    driverId,
    status: { $in: ['SCHEDULED', 'IN_PROGRESS'] }
  });
  
  if (trips && trips.length > 0) {
    // Cache for 1 minute
    await redis.set(tripKey, JSON.stringify(trips), 'EX', 60);
  }
  
  return trips;
}

module.exports = { setupSocketHandlers };