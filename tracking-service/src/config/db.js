import mongoose from 'mongoose';
import { Tracking } from '../models/Tracking.js';

export const connectDB = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true
    });
    console.log('MongoDB connected successfully');
    
    // Create indexes
    await Tracking.createIndexes({ driverId: 1 });
    await Tracking.createIndexes({ orderId: 1 });
  } catch (err) {
    console.error('MongoDB connection error:', err);
    process.exit(1);
  }
};