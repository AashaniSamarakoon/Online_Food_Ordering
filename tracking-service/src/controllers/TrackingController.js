import axios from 'axios';
import { TrackingModel } from '../models/Tracking.js';

export class TrackingController {
  constructor(webSocketService) {
    this.wsService = webSocketService;
  }

  async handleLocationUpdate(req, res) {
    const { trackingId, lat, lng } = req.body;
    
    try {
      const tracking = await TrackingModel.updateLocation(trackingId, lat, lng);
      
      // Broadcast to customer
      this.wsService.broadcastLocationUpdate(
        tracking.driver_id, 
        { trackingId, lat, lng }
      );
      
      res.json({ success: true, tracking });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }

  async getActiveDeliveries(req, res) {
    try {
      const trackings = await TrackingModel.getActiveTrackings();
      res.json(trackings);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }
}