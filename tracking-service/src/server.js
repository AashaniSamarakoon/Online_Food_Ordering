const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const { connectMongo } = require('./config/db');
const { createRedisClient } = require('./config/redis');
const { setupSocketHandlers } = require('./socket/socketManager');
const routes = require('./api/routes/index');
const logger = require('./utils/logger');
// const errorMiddleware = require('./api/middleware/errorMiddleware');

// Initialize Express app
const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

// Connect to databases
connectMongo();
createRedisClient();

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(morgan('combined'));

// Routes
app.use('/api', routes);

// Error handling
// app.use(errorMiddleware);

// Initialize socket handlers
setupSocketHandlers(io);

// Start scheduled tasks
require('./tasks/cleanUpTask').startCleanupTask();
require('./tasks/heatMapGenerationTask').startHeatmapGeneration();

// Start server
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  logger.info(`Tracking service running on port ${PORT}`);
});

// Handle graceful shutdown
process.on('SIGTERM', () => {
  logger.info('SIGTERM received, shutting down gracefully');
  server.close(() => {
    logger.info('Server closed');
    process.exit(0);
  });
});

module.exports = { app, server };