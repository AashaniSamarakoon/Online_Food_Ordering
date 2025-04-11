const schedule = require('node-schedule');
const Location = require('../models/Location');
const logger = require('../utils/logger');

/**
 * Scheduled task to clean up old location data
 * Runs daily at midnight
 */
function startCleanupTask() {
  // Schedule to run at midnight
  schedule.scheduleJob('0 0 * * *', async () => {
    try {
      logger.info('Starting location data cleanup task');
      
      // Keep data for last 30 days
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - 30);
      
      const result = await Location.deleteMany({
        timestamp: { $lt: cutoffDate }
      });
      
      logger.info(`Cleanup completed. Deleted ${result.deletedCount} old location records`);
    } catch (error) {
      logger.error(`Error in cleanup task: ${error.message}`, { error });
    }
  });
  
  logger.info('Cleanup task scheduled');
}

module.exports = { startCleanupTask };