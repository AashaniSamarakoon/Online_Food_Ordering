import { WebSocketServer } from 'ws';
import { Tracking } from '../models/Tracking.js';

export class WebSocketService {
  constructor(server) {
    this.wss = new WebSocketServer({ server });
    this.clients = new Map(); // Map<driverId, WebSocket>
    this.trackingUpdates = new Map(); // Map<trackingId, intervalId>

    this.wss.on('connection', (ws, req) => {
      const driverId = new URL(req.url, `http://${req.headers.host}`).searchParams.get('driverId');
      
      if (driverId) {
        this.setupDriverConnection(ws, driverId);
      }
    });
  }

  setupDriverConnection(ws, driverId) {
    this.clients.set(driverId, ws);
    console.log(`Driver ${driverId} connected`);

    // Start sending periodic location updates if available
    this.startTrackingUpdates(driverId);

    ws.on('close', () => {
      this.clients.delete(driverId);
      this.stopTrackingUpdates(driverId);
      console.log(`Driver ${driverId} disconnected`);
    });
  }

  startTrackingUpdates(driverId) {
    //get actual tracking data
    const intervalId = setInterval(async () => {
      const tracking = await Tracking.findOne({ driverId }).exec();
      if (tracking && tracking.currentLocation) {
        this.sendToDriver(driverId, {
          type: 'LOCATION_UPDATE',
          data: tracking.currentLocation
        });
      }
    }, 1000); // Update every 1 seconds

    this.trackingUpdates.set(driverId, intervalId);
  }

  stopTrackingUpdates(driverId) {
    const intervalId = this.trackingUpdates.get(driverId);
    if (intervalId) {
      clearInterval(intervalId);
      this.trackingUpdates.delete(driverId);
    }
  }

  sendToDriver(driverId, message) {
    const ws = this.clients.get(driverId);
    if (ws && ws.readyState === ws.OPEN) {
      ws.send(JSON.stringify(message));
    }
  }

  notifyOrderAssigned(driverId, orderDetails) {
    this.sendToDriver(driverId, {
      type: 'ORDER_ASSIGNED',
      data: orderDetails
    });
  }
}