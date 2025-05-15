const { createClient } = require('redis');
const logger = require("../utils/logger");

let redisClient = null;

const createRedisClient = async () => {
  try {
    // If client already exists, return it
    if (redisClient) {
      return redisClient;
    }
    
    logger.info("Connecting to Redis...");

    // Use the Redis.io recommended configuration
    const client = createClient({
      username: process.env.REDIS_USERNAME,
      password: process.env.REDIS_PASSWORD,
      socket: {
        host: process.env.REDIS_HOST,
        port: parseInt(process.env.REDIS_PORT || '6379'),
      }
    });

    // Connection management
    client.on("connect", () => {
      logger.info("Redis connected successfully");
    });

    client.on("error", (err) => {
      logger.error(`Redis error: ${err.message}`);
    });

    client.on("disconnect", () => {
      logger.warn("Redis connection closed");
    });

    client.on("reconnecting", () => {
      logger.info("Redis reconnecting...");
    });

    // Connect to Redis (required for the new client)
    await client.connect();
    logger.info("Redis client connected and ready");

    redisClient = client;
    return client;
  } catch (error) {
    logger.error(`Failed to create Redis client: ${error.message}`);
    throw error;
  }
};

// Provide a safe way to get Redis without immediate errors
const getRedisClient = () => {
  if (!redisClient) {
    // Return a placeholder that will work until Redis is actually needed
    logger.warn('Redis client accessed before initialization - operations will be queued');
    return {
      get: async () => null,
      set: async () => 'OK',
      del: async () => 0,
      expire: async () => true,
      ping: async () => 'PONG'
    };
  }
  return redisClient;
};

module.exports = {
  createRedisClient,
  getRedisClient,
};