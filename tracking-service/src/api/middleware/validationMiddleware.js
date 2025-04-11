const { validationResult } = require('express-validator');
const logger = require('../../utils/logger');

/**
 * Middleware to validate request data using express-validator
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 * @param {Function} next - Express next function
 */
exports.validateRequest = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    logger.warn('Validation error', { errors: errors.array() });
    return res.status(400).json({ 
      status: 'error',
      message: 'Validation error',
      errors: errors.array() 
    });
  }
  next();
};