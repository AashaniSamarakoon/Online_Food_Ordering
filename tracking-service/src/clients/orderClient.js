const axios = require('axios');
const logger = require('../utils/logger');
const { getRedisClient } = require('../config/redis');

// Order service client
const orderClient = {
  baseURL: process.env.ORDER_SERVICE_URL || 'http://order-service:8093',
  
  async getOrderDetails(orderId) { 
    try {
      // Get Redis client at function call time, not module load time
      const redis = getRedisClient();
      
      // Try cache first
      const cacheKey = `order:${orderId}:details`;
      const cachedData = await redis.get(cacheKey);
      
      if (cachedData) {
        return JSON.parse(cachedData);
      }
      
      // Call order service
      const response = await axios.get(`${this.baseURL}/orders/public/${orderId}`);
      
      // Cache the result
      if (response.data) {
        await redis.set(cacheKey, JSON.stringify(response.data), { EX: 300 }); // 5 minutes
      }
      
      return response.data;
    } catch (error) {
      logger.error(`Error fetching order details: ${error.message}`, { orderId });
      throw error;
    }
  },
};

module.exports = {
  orderClient,
};