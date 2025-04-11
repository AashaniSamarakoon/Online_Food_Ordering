const Redis = require('ioredis');
const logger = require('../utils/logger');

let redisClient;

// Create Redis client
const createRedisClient = () => {
  const REDIS_URL = process.env.REDIS_URL || 'redis://localhost:6379';
  
  redisClient = new Redis(REDIS_URL, {
    retryStrategy: (times) => {
      const delay = Math.min(times * 50, 2000);
      return delay;
    },
    maxRetriesPerRequest: 3
  });
  
  redisClient.on('connect', () => {
    logger.info('Connected to Redis');
  });
  
  redisClient.on('error', (err) => {
    logger.error(`Redis error: ${err.message}`, { error: err });
  });
  
  redisClient.on('reconnecting', () => {
    logger.warn('Reconnecting to Redis...');
  });
  
  return redisClient;
};

// Get Redis client
const getRedisClient = () => {
  if (!redisClient) {
    return createRedisClient();
  }
  return redisClient;
};

module.exports = {
  createRedisClient,
  getRedisClient
};