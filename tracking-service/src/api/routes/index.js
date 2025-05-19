const express = require('express');
const router = express.Router();
const trackingController = require('../controllers/trackingController');

// Driver location routes
router.get('/drivers/:driverId/location', trackingController.getDriverLocation);
router.put('/drivers/:driverId/location', trackingController.updateDriverLocation);

// Trip routes
router.get('/trips/:orderId', trackingController.getTripStatus);
router.post('/trips/pending', trackingController.createPendingTrip);
router.post('/trips/:orderId/assign', trackingController.assignDriverToTrip);
router.post('/status/:orderId', trackingController.updateOrderStatus);
// Add this new route for waypoint updates
router.put('/trips/:orderId/waypoints/:waypointIndex', trackingController.updateWaypointStatus);

// Nearby drivers
router.get('/drivers/nearby', trackingController.getNearbyDrivers);

// Analytics routes
router.get('/analytics/heatmap', trackingController.getHeatmapData);

// Health check endpoint
router.get('/health', (req, res) => res.status(200).json({ status: 'ok' }));
router.get('/health/ready', (req, res) => res.status(200).json({ status: 'ready' }));

module.exports = router;