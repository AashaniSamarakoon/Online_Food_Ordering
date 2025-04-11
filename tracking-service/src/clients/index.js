const axios = require('axios');
const logger = require('../utils/logger');
const { getRedisClient } = require('../config/redis');

const redis = getRedisClient();

// Order service client
const orderClient = {
  baseURL: process.env.ORDER_SERVICE_URL || 'http://order-service:8080',
  
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
  
  async updateOrderStatus(orderId, status) {
    try {
      const response = await axios.patch(`${this.baseURL}/api/orders/${orderId}/status`, { status });
      return response.data;
    } catch (error) {
      logger.error(`Error updating order status: ${error.message}`, { orderId, status });
      throw error;
    }
  }
};

// Driver service client
const driverClient = {
  baseURL: process.env.DRIVER_SERVICE_URL || 'http://driver-service:8082',
  
  async getDriverDetails(driverId) {
    try {
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
  
  async updateDriverStatus(driverId, status) {
    try {
      const response = await axios.patch(`${this.baseURL}/api/drivers/status`, {
        driverId,
        status
      });
      return response.data;
    } catch (error) {
      logger.error(`Error updating driver status: ${error.message}`, { driverId, status });
      throw error;
    }
  }
};

// Notification service client
const notificationClient = {
  baseURL: process.env.NOTIFICATION_SERVICE_URL || 'http://notification-service:8080',
  
  async sendDriverArrivedNotification(customerId, orderId) {
    try {
      await axios.post(`${this.baseURL}/api/notifications`, {
        recipient: customerId,
        type: 'SMS',
        message: `Your delivery driver has arrived for order #${orderId}.`
      });
    } catch (error) {
      logger.error(`Error sending driver arrived notification: ${error.message}`, { customerId, orderId });
      // Don't throw - notifications shouldn't break core functionality
    }
  },
  
  async sendDeliveryStartedNotification(customerId, orderId) {
    try {
      await axios.post(`${this.baseURL}/api/notifications`, {
        recipient: customerId,
        type: 'SMS',
        message: `Your delivery for order #${orderId} has started. Track your order in real-time in the app.`
      });
    } catch (error) {
      logger.error(`Error sending delivery started notification: ${error.message}`, { customerId, orderId });
    }
  },
  
  async sendDeliveryCompletedNotification(customerId, orderId) {
    try {
      await axios.post(`${this.baseURL}/api/notifications`, {
        recipient: customerId,
        type: 'SMS',
        message: `Your delivery for order #${orderId} is complete. Thank you for using our service!`
      });
    } catch (error) {
      logger.error(`Error sending delivery completed notification: ${error.message}`, { customerId, orderId });
    }
  }
};

module.exports = {
  orderClient,
  driverClient,
  notificationClient
};