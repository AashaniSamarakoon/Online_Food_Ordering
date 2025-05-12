const axios = require('axios');
const logger = require('../utils/logger');
const { getRedisClient } = require('../config/redis');

// Driver service client
const driverClient = {
  baseURL: process.env.DRIVER_SERVICE_URL || 'http://driver-service:8087',
  
  async getDriverDetails(driverId) {
    try {
      // Get Redis client at function call time, not module load time
      const redis = getRedisClient();
      
      // Try cache first
      const cacheKey = `driver:${driverId}:details`;
      const cachedData = await redis.get(cacheKey);
      
      if (cachedData) {
        return JSON.parse(cachedData);
      }
      
      // Call driver service
      const response = await axios.get(`${this.baseURL}/api/drivers/${driverId}`);
      
      // Cache the result
      if (response.data) {
        await redis.set(cacheKey, JSON.stringify(response.data), 'EX', 300); // 5 minutes
      }
      
      return response.data;
    } catch (error) {
      logger.error(`Error fetching driver details: ${error.message}`, { driverId });
      throw error;
    }
  },

  async getAvailableDrivers() {
    try {
      // Get Redis client at function call time
      const redis = getRedisClient();
      
      // Try cache first
      const cacheKey = 'drivers:available';
      const cachedData = await redis.get(cacheKey);
      
      if (cachedData) {
        return JSON.parse(cachedData);
      }
      
      // Call driver service if not in cache
      const response = await axios.get(`${this.baseURL}/api/drivers/available`);
      
      // Cache the result with a short TTL (30 seconds)
      // Short TTL ensures we don't use stale driver availability data for too long
      if (response.data && Array.isArray(response.data)) {
        await redis.set(cacheKey, JSON.stringify(response.data), 'EX', 30);
      }
      
      return response.data;
    } catch (error) {
      logger.error(`Error getting available drivers: ${error.message}`);
      // If error occurs, try to return cached data even if expired
      try {
        const redis = getRedisClient();
        const cacheKey = 'drivers:available';
        const cachedData = await redis.get(cacheKey);
        if (cachedData) {
          logger.info('Returning stale driver data due to service error');
          return JSON.parse(cachedData);
        }
      } catch (redisError) {
        logger.error(`Redis error: ${redisError.message}`);
      }
      throw error;
    }
  }
};

module.exports = {
  driverClient,
};