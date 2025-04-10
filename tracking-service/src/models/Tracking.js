import mongoose from 'mongoose';

const trackingSchema = new mongoose.Schema({
  orderId: { type: String, required: true },
  driverId: { type: String, required: true },
  status: { 
    type: String, 
    enum: ['PENDING', 'IN_PROGRESS', 'COMPLETED'],
    default: 'PENDING'
  },
  currentLocation: {
    lat: { type: Number },
    lng: { type: Number }
  },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

export const Tracking = mongoose.model('Tracking', trackingSchema);

export const TrackingModel = {
  async createTrackingRecord(orderId, driverId) {
    const tracking = new Tracking({
      orderId,
      driverId
    });
    return await tracking.save();
  },

  async updateLocation(trackingId, lat, lng) {
    return await Tracking.findByIdAndUpdate(
      trackingId,
      {
        $set: {
          'currentLocation.lat': lat,
          'currentLocation.lng': lng,
          updatedAt: new Date()
        }
      },
      { new: true }
    );
  },

  async getActiveTrackings() {
    return await Tracking.find({
      status: { $in: ['PENDING', 'IN_PROGRESS'] }
    }).exec();
  }
};