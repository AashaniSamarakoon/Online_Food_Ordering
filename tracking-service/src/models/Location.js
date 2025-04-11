const mongoose = require('mongoose');

const LocationSchema = new mongoose.Schema({
  driverId: {
    type: String,
    required: true,
    index: true
  },
  orderId: {
    type: String,
    index: true
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
  speed: {
    type: Number,
    default: 0
  },
  heading: {
    type: Number,
    default: 0
  },
  accuracy: {
    type: Number
  },
  timestamp: {
    type: Date,
    default: Date.now
  },
  batteryLevel: {
    type: Number
  },
  status: {
    type: String,
    enum: ['IDLE', 'PICKING_UP', 'DELIVERING', 'COMPLETED'],
    default: 'IDLE'
  }
}, {
  timestamps: true
});

// Index for geospatial queries
LocationSchema.index({ location: '2dsphere' });

// Index for timestamp-based queries
LocationSchema.index({ timestamp: -1 });

// Index for driver-specific queries
LocationSchema.index({ driverId: 1, timestamp: -1 });

module.exports = mongoose.model('Location', LocationSchema);