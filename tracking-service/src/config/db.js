const mongoose = require('mongoose');
const logger = require('../utils/logger');

// MongoDB connection options optimized for Atlas
const options = {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  serverSelectionTimeoutMS: 30000, 
  socketTimeoutMS: 45000,
  connectTimeoutMS: 30000, 
  family: 4, // Force IPv4
  retryWrites: true,
  // Atlas specific recommended options
  ssl: true,
  authSource: 'admin'
};

let connectionAttempts = 0;
const MAX_ATTEMPTS = 5;

// Connect to MongoDB Atlas with retries
const connectMongo = async () => {
  if (connectionAttempts >= MAX_ATTEMPTS) {
    logger.warn(`Failed to connect to MongoDB Atlas after ${MAX_ATTEMPTS} attempts. Running in degraded mode.`);
    return;
  }

  connectionAttempts++;
  
  try {
    // Use only the environment variable - remove the local fallback
    const MONGO_URI = process.env.MONGO_URI;
    
    if (!MONGO_URI) {
      throw new Error('MONGO_URI environment variable is not defined');
    }
    
    logger.info(`Connecting to MongoDB Atlas (attempt ${connectionAttempts}/${MAX_ATTEMPTS})`);
    
    await mongoose.connect(MONGO_URI, options);
    logger.info('Connected to MongoDB Atlas successfully');
    connectionAttempts = 0; // Reset on successful connection
  } catch (error) {
    logger.error(`MongoDB Atlas connection error: ${error.message}`);
    
    if (connectionAttempts < MAX_ATTEMPTS) {
      const retryDelay = Math.min(1000 * Math.pow(2, connectionAttempts), 30000); // Exponential backoff
      logger.info(`Retrying MongoDB Atlas connection in ${retryDelay/1000} seconds...`);
      
      setTimeout(connectMongo, retryDelay);
    } else {
      logger.warn('Maximum connection attempts reached. Features requiring MongoDB will be disabled.');
    }
  }
};

// Handle MongoDB disconnections more gracefully
mongoose.connection.on('disconnected', () => {
  logger.warn('MongoDB Atlas disconnected. Attempting to reconnect...');
  setTimeout(connectMongo, 5000);
});

mongoose.connection.on('error', (err) => {
  logger.error(`MongoDB Atlas connection error: ${err.message}`);
  // Don't crash the app on connection errors
});

// Handle graceful shutdown
process.on('SIGINT', async () => {
  try {
    if (mongoose.connection && mongoose.connection.readyState !== 0) {
      await mongoose.connection.close();
      logger.info('MongoDB Atlas connection closed gracefully');
    }
    process.exit(0);
  } catch (error) {
    logger.error(`Error closing MongoDB Atlas connection: ${error.message}`);
    process.exit(1);
  }
});

module.exports = { connectMongo };