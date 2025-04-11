const mongoose = require('mongoose');

const WaypointSchema = new mongoose.Schema({
  type: {
    type: String,
    enum: ['PICKUP', 'DROPOFF', 'WAYPOINT'],
    required: true
  },
  location: {
    type: {
      type: String,
      enum: ['Point'],
      default: 'Point'
    },
    coordinates: {
      type: [Number], // [longitude, latitude]
      required: true
    }
  },
  address: {
    type: String,
    required: true
  },
  arrivalTime: Date,
  departureTime: Date,
  status: {
    type: String,
    enum: ['PENDING', 'ARRIVED', 'COMPLETED'],
    default: 'PENDING'
  }
});

const TripSchema = new mongoose.Schema({
  orderId: {
    type: String,
    required: true,
    index: true
  },
  driverId: {
    type: String,
    required: true,
    index: true
  },
  customerId: {
    type: String,
    required: true
  },
  status: {
    type: String,
    enum: ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
    default: 'SCHEDULED'
  },
  startTime: Date,
  endTime: Date,
  estimatedArrivalTime: Date,
  waypoints: [WaypointSchema],
  route: {
    type: {
      type: String,
      enum: ['LineString'],
      default: 'LineString'
    },
    coordinates: {
      type: [[Number]], // array of [longitude, latitude] pairs
      default: []
    }
  },
  distance: {
    type: Number, // in meters
    default: 0
  },
  duration: {
    type: Number, // in seconds
    default: 0
  },
  currentEta: {
    type: Number, // in seconds
    default: 0
  },
  createdAt: {
    type: Date,
    default: Date.now
  },
  updatedAt: {
    type: Date,
    default: Date.now
  }
});

// Compound index for efficient querying
TripSchema.index({ driverId: 1, status: 1 });
TripSchema.index({ orderId: 1, status: 1 });

module.exports = mongoose.model('Trip', TripSchema);