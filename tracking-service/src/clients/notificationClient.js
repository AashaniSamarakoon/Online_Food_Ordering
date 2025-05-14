// const axios = require('axios');
// const logger = require('../utils/logger');
// const { getRedisClient } = require('../config/redis');

// const redis = getRedisClient();


// // Notification service client
// const notificationClient = {
//   baseURL: process.env.NOTIFICATION_SERVICE_URL || 'http://notification-service:8093',
  
//   async sendDriverArrivedNotification(customerId, orderId) {
//     try {
//       await axios.post(`${this.baseURL}/api/notifications`, {
//         recipient: customerId,
//         type: 'SMS',
//         message: `Your delivery driver has arrived for order #${orderId}.`
//       });
//     } catch (error) {
//       logger.error(`Error sending driver arrived notification: ${error.message}`, { customerId, orderId });
//       // Don't throw - notifications shouldn't break core functionality
//     }
//   },
  
//   async sendDeliveryStartedNotification(customerId, orderId) {
//     try {
//       await axios.post(`${this.baseURL}/api/notifications`, {
//         recipient: customerId,
//         type: 'SMS',
//         message: `Your delivery for order #${orderId} has started. Track your order in real-time in the app.`
//       });
//     } catch (error) {
//       logger.error(`Error sending delivery started notification: ${error.message}`, { customerId, orderId });
//     }
//   },
  
//   async sendDeliveryCompletedNotification(customerId, orderId) {
//     try {
//       await axios.post(`${this.baseURL}/api/notifications`, {
//         recipient: customerId,
//         type: 'SMS',
//         message: `Your delivery for order #${orderId} is complete. Thank you for using our service!`
//       });
//     } catch (error) {
//       logger.error(`Error sending delivery completed notification: ${error.message}`, { customerId, orderId });
//     }
//   }
// };

// module.exports = {
//   notificationClient
// };