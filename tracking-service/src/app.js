import express from 'express';
import http from 'http';
import cors from 'cors';
import { WebSocketService } from './services/WebSocketService.js';
import { TrackingController } from './controllers/TrackingController.js';

const app = express();
const server = http.createServer(app);

// Middleware
app.use(cors());
app.use(express.json());

// Initialize WebSocket
const wsService = new WebSocketService(server);

// Initialize Controller
const trackingController = new TrackingController(wsService);

// Routes
app.post('/api/tracking/update', (req, res) => 
  trackingController.handleLocationUpdate(req, res));
app.get('/api/tracking/active', (req, res) => 
  trackingController.getActiveDeliveries(req, res));

// Start server
const PORT = process.env.PORT || 8084;
server.listen(PORT, () => {
  console.log(`Tracking service running on port ${PORT}`);
});