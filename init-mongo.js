// init-mongo.js
// Runs automatically when the MongoDB container first starts.
// Creates databases, collections and indexes for 360 Church Flow microservices.

// ── Event Service ─────────────────────────────────────────────────────────────
db = db.getSiblingDB('church360_events');

db.createCollection('events');
db.events.createIndex({ "eventDate": 1 });
db.events.createIndex({ "eventType": 1 });
db.events.createIndex({ "status": 1 });
db.events.createIndex({ "organizerId": 1 });

db.createCollection('rsvps');

// Compound unique index with an explicit name — prevents the
// "IndexOptionsConflict" error caused by Spring Data trying to
// re-create the same index with an auto-generated name like
// "eventId_1_membershipNumber_1" when one named differently already exists.
db.rsvps.createIndex(
    { "eventId": 1, "membershipNumber": 1 },
    { unique: true, name: "uq_rsvp_event_member" }
);
db.rsvps.createIndex({ "membershipNumber": 1 });
db.rsvps.createIndex({ "status": 1 });

// ── Notification Service ──────────────────────────────────────────────────────
db = db.getSiblingDB('church360_notifications');

db.createCollection('notification_logs');
db.notification_logs.createIndex({ "membershipNumber": 1 });
db.notification_logs.createIndex({ "channel": 1 });
db.notification_logs.createIndex({ "status": 1 });
db.notification_logs.createIndex({ "createdAt": -1 });
// Auto-delete logs older than 90 days
db.notification_logs.createIndex(
    { "createdAt": 1 },
    { expireAfterSeconds: 7776000 }
);

print('✅  MongoDB initialised: church360_events, church360_notifications');