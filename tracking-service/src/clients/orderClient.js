const axios = require('axios');
const logger = require('../utils/logger');
const { getRedisClient } = require('../config/redis');

const redis = getRedisClient();

// Order service client
const orderClient = {
  baseURL: process.env.ORDER_SERVICE_URL || 'http://order-service:8086',
  
  async getOrderDetails(orderId) {
    try {
      // Try cache first
      const cacheKey = `order:${orderId}:details`;
      const cachedData = await redis.get(cacheKey);
      
      if (cachedData) {
        return JSON.parse(cachedData);
      }
      
      // Call order service
      const response = await axios.get(`${this.baseURL}/api/orders/${orderId}`);
      
      // Cache the result
      if (response.data) {
        await redis.set(cacheKey, JSON.stringify(response.data), 'EX', 300); // 5 minutes
      }
      
      return response.data;
    } catch (error) {
      logger.error(`Error fetching order details: ${error.message}`, { orderId });
      throw error;
    }
  },
  
  // async updateOrderStatus(orderId, status) {
  //   try {
  //     const response = await axios.patch(`${this.baseURL}/api/orders/${orderId}/status`, { status });
  //     return response.data;
  //   } catch (error) {
  //     logger.error(`Error updating order status: ${error.message}`, { orderId, status });
  //     throw error;
  //   }
  // }
};


module.exports = {
  orderClient,
};