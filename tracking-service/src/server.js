require("dotenv").config();
const express = require("express");
const http = require("http");
const socketIo = require("socket.io");
const cors = require("cors");
const helmet = require("helmet");
const morgan = require("morgan");
const { connectMongo } = require("./config/db");
const { setupSocketHandlers } = require("./socket/socketManager");
const routes = require("./api/routes/index");
const logger = require("./utils/logger");
const { createRedisClient, getRedisClient } = require("./config/redis");

const app = express();
const server = http.createServer(app);

// Basic middleware
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(morgan("combined"));

// Debugging middleware
app.use((req, res, next) => {
  console.debug(`[DEBUG] ${req.method} ${req.url} - Full path: ${req.originalUrl}`);
  next();
});

// Health check endpoint
app.get("/health", async (req, res) => {
  const health = {
    status: "UP",
    timestamp: new Date(),
    services: {},
  };

  try {
    // MongoDB health
    health.services.mongodb = mongoose.connection?.readyState === 1 ? "connected" : "disconnected";
    if (health.services.mongodb === "disconnected") health.status = "DEGRADED";

    // Redis health
    try {
      const redis = getRedisClient();
      await redis.ping();
      health.services.redis = "connected";
    } catch {
      health.services.redis = "disconnected";
      health.status = "DEGRADED";
    }

    res.status(health.status === "UP" ? 200 : 503).json(health);
  } catch (error) {
    res.status(500).json({ status: "DOWN", error: error.message });
  }
});

// Mount API routes
app.use("/api/tracking", routes);

// Catch-all handler for unmatched routes
app.use("*", (req, res) => {
  console.debug(`[DEBUG] No route found for ${req.method} ${req.originalUrl}`);
  res.status(404).send(`Cannot ${req.method} ${req.originalUrl}`);
});

// Initialize services
async function initializeServices() {
  try {
    await connectMongo();
    await createRedisClient();
    return true;
  } catch (error) {
    logger.error(`Service initialization failed: ${error.message}`);
    return false;
  }
}

// Start the application
async function startApp() {
  const servicesInitialized = await initializeServices();

  if (!servicesInitialized) logger.warn("Running in degraded mode.");

  // Initialize socket.io
  const io = socketIo(server, { cors: { origin: "*", methods: ["GET", "POST"] } });
  app.io = io;
  setupSocketHandlers(io);

  // Start server
  const PORT = 8089;
  server.listen(PORT, () => logger.info(`Server running on port ${PORT}`));
}

// Handle graceful shutdown
process.on("SIGTERM", () => {
  logger.info("SIGTERM received, shutting down...");
  server.close(() => process.exit(0));
});

startApp().catch((error) => {
  logger.error(`Application startup failed: ${error.message}`);
  process.exit(1);
});

module.exports = { app, server };