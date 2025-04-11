const schedule = require('node-schedule');
const trackingService = require('../services/trackingService');
const { getRedisClient } = require('../config/redis');
const logger = require('../utils/logger');

const redis = getRedisClient();

/**
 * Scheduled task to pre-generate heatmap data for analytics
 * Runs every hour
 */
function startHeatmapGeneration() {
  // Schedule to run every hour
  schedule.scheduleJob('0 * * * *', async () => {
    try {
      logger.info('Starting heatmap data generation task');
      
      // Generate heatmap for last 24 hours
      const endTime = new Date();
      const startTime = new Date(endTime);
      startTime.setHours(startTime.getHours() - 24);
      
      // Generate heatmap data
      const heatmapData = await trackingService.generateHeatmapData(startTime, endTime);
      
      // Store in Redis with expiration of 2 hours
      await redis.set('heatmap:last24h', JSON.stringify(heatmapData), 'EX', 7200);
      
      // Also generate for last week for trending analysis
      const weekStartTime = new Date(endTime);
      weekStartTime.setDate(weekStartTime.getDate() - 7);
      
      const weeklyHeatmap = await trackingService.generateHeatmapData(weekStartTime, endTime, 0.02);
      await redis.set('heatmap:lastWeek', JSON.stringify(weeklyHeatmap), 'EX', 86400);
      
      logger.info('Heatmap generation completed');
    } catch (error) {
      logger.error(`Error in heatmap generation task: ${error.message}`, { error });
    }
  });
  
  logger.info('Heatmap generation task scheduled');
}

module.exports = { startHeatmapGeneration };