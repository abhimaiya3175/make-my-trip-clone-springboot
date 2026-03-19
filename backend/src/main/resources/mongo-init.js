db = db.getSiblingDB('makemytrip');

// Reviews & Ratings
db.createCollection('reviews');
db.reviews.createIndex({ entityType: 1, entityId: 1, createdAt: -1 });
db.reviews.createIndex({ entityType: 1, entityId: 1, helpfulCount: -1 });
db.reviews.createIndex({ userId: 1, entityType: 1, entityId: 1 }, { unique: true });
db.reviews.createIndex({ moderationStatus: 1, flagged: 1 });

// Live flight status updates
db.createCollection('flight_status_updates');
db.flight_status_updates.createIndex({ flightId: 1, updatedAt: -1 });
db.flight_status_updates.createIndex({ status: 1, updatedAt: -1 });

// Flight seat maps and locks
db.createCollection('flight_seat_maps');
db.flight_seat_maps.createIndex({ flightId: 1 }, { unique: true });
db.flight_seat_maps.createIndex({ 'seats.seatNo': 1 });

// Hotel room inventory and price tiers
db.createCollection('hotel_room_inventory');
db.hotel_room_inventory.createIndex({ hotelId: 1, roomType: 1, date: 1 }, { unique: true });

// Dynamic pricing snapshots
db.createCollection('pricing_history');
db.pricing_history.createIndex({ entityType: 1, entityId: 1, capturedAt: -1 });

// Recommendation outputs and feedback
db.createCollection('recommendations');
db.recommendations.createIndex({ userId: 1, createdAt: -1 });
db.recommendations.createIndex({ userId: 1, itemType: 1, itemId: 1 });

db.createCollection('recommendation_feedback');
db.recommendation_feedback.createIndex({ userId: 1, itemType: 1, itemId: 1 }, { unique: true });
db.recommendation_feedback.createIndex({ feedbackType: 1, createdAt: -1 });
