/**
 * Calculate distance between two points in meters using the Haversine formula
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371e3; // Earth radius in meters
    const φ1 = lat1 * Math.PI/180;
    const φ2 = lat2 * Math.PI/180;
    const Δφ = (lat2-lat1) * Math.PI/180;
    const Δλ = (lon2-lon1) * Math.PI/180;
  
    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ/2) * Math.sin(Δλ/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  
    return R * c;
  }
  
  /**
   * Calculate ETA based on distance and speed
   * @param {number} distance Distance in meters
   * @param {number} speed Speed in meters per second
   * @returns {number} ETA in seconds
   */
  function calculateETA(distance, speed) {
    // If speed is 0 or very low, use a default speed
    const effectiveSpeed = speed < 1 ? 10 : speed;
    return Math.ceil(distance / effectiveSpeed);
  }
  
  /**
   * Check if a point is inside a geo-fence
   * @param {Array} point [longitude, latitude]
   * @param {Array} polygon Array of [longitude, latitude] points
   * @returns {boolean} Whether the point is inside the polygon
   */
  function isPointInPolygon(point, polygon) {
    // Implementation of point-in-polygon algorithm
    const x = point[0];
    const y = point[1];
    
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
      const xi = polygon[i][0];
      const yi = polygon[i][1];
      const xj = polygon[j][0];
      const yj = polygon[j][1];
      
      const intersect = ((yi > y) !== (yj > y))
          && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
      if (intersect) inside = !inside;
    }
    
    return inside;
  }
  
  module.exports = {
    calculateDistance,
    calculateETA,
    isPointInPolygon
  };