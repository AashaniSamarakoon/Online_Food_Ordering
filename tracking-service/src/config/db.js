const mongoose = require('mongoose');
const logger = require('../utils/logger');

// MongoDB connection options
const options = {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  serverSelectionTimeoutMS: 30000, 
  socketTimeoutMS: 45000,
  connectTimeoutMS: 30000, 
  family: 4, // Force IPv4
  retryWrites: true
};

let connectionAttempts = 0;
const MAX_ATTEMPTS = 5;

// Connect to MongoDB with retries
const connectMongo = async () => {
  if (connectionAttempts >= MAX_ATTEMPTS) {
    logger.warn(`Failed to connect to MongoDB after ${MAX_ATTEMPTS} attempts. Running in degraded mode.`);
    return;
  }

  connectionAttempts++;
  
  try {
    // Make sure to use the correct service name from docker-compose
    const MONGO_URI = process.env.MONGO_URI || 'mongodb://mongodb:27017/tracking_db';
    logger.info(`Connecting to MongoDB at ${MONGO_URI} (attempt ${connectionAttempts}/${MAX_ATTEMPTS})`);
    
    await mongoose.connect(MONGO_URI, options);
    logger.info('Connected to MongoDB');
    connectionAttempts = 0; // Reset on successful connection
  } catch (error) {
    logger.error(`MongoDB connection error: ${error.message}`);
    
    if (connectionAttempts < MAX_ATTEMPTS) {
      const retryDelay = Math.min(1000 * connectionAttempts, 10000);
      logger.info(`Retrying MongoDB connection in ${retryDelay/1000} seconds...`);
      
      setTimeout(connectMongo, retryDelay);
    } else {
      logger.warn('Maximum connection attempts reached. Some features requiring MongoDB will be disabled.');
    }
  }
};

// Handle MongoDB disconnections more gracefully
mongoose.connection.on('disconnected', () => {
  logger.warn('MongoDB disconnected. Attempting to reconnect...');
  setTimeout(connectMongo, 5000);
});

mongoose.connection.on('error', (err) => {
  logger.error(`MongoDB connection error: ${err.message}`);
  // Don't crash the app on connection errors
});

// Handle graceful shutdown
process.on('SIGINT', async () => {
  try {
    if (mongoose.connection && mongoose.connection.readyState !== 0) {
      await mongoose.connection.close();
      logger.info('MongoDB connection closed gracefully');
    }
    process.exit(0);
  } catch (error) {
    logger.error(`Error closing MongoDB connection: ${error.message}`);
    process.exit(1);
  }
});

module.exports = { connectMongo };