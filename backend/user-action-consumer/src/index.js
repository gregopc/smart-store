require("dotenv").config();

const amqp = require("amqplib");
const { randomUUID } = require("node:crypto");
const { Pool } = require("pg");

const rabbitUrl = process.env.RABBITMQ_URL || "amqp://guest:guest@localhost:5672";
const exchange = process.env.RABBITMQ_EXCHANGE || "smartstore.user-actions";
const queue = process.env.RABBITMQ_QUEUE || "smartstore.user-actions.queue";
const routingKey = process.env.RABBITMQ_ROUTING_KEY || "user-actions";

const pool = new Pool({
  connectionString:
    process.env.DATABASE_URL || "postgresql://smartstore:smartstore@localhost:5432/smartstore"
});

const createTableSql = `
CREATE TABLE IF NOT EXISTS user_actions (
  event_id UUID PRIMARY KEY,
  event_type VARCHAR(80) NOT NULL,
  user_id UUID,
  user_email VARCHAR(255),
  product_id UUID,
  cart_id UUID,
  cart_item_id UUID,
  search_query TEXT,
  assistant_message TEXT,
  cart_total NUMERIC(19, 2),
  route VARCHAR(180),
  metadata_json TEXT,
  raw_event_json TEXT NOT NULL,
  occurred_at TIMESTAMPTZ,
  consumed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
)
`;

main().catch(async (error) => {
  console.error("Consumer stopped:", error);
  await pool.end().catch(() => { });
  process.exit(1);
});

async function main() {
  await pool.query(createTableSql);

  const connection = await amqp.connect(rabbitUrl);
  const channel = await connection.createChannel();

  await channel.assertExchange(exchange, "topic", { durable: true });
  await channel.assertQueue(queue, { durable: true });
  await channel.bindQueue(queue, exchange, routingKey);
  await channel.prefetch(10);

  console.log(`Listening for user actions on queue "${queue}"`);

  await channel.consume(queue, async (message) => {
    if (!message) {
      return;
    }

    try {
      const event = JSON.parse(message.content.toString("utf8"));
      await insertUserAction(event);
      channel.ack(message);
      console.log(`Stored event ${event.eventType || "UNKNOWN"} (${event.eventId || "no-id"})`);
    } catch (error) {
      console.error("Failed to store event:", error.message);
      channel.nack(message, false, true);
    }
  });
}

function insertUserAction(event) {
  const values = [
    event.eventId || randomUUID(),
    event.eventType || "UNKNOWN",
    event.userId || null,
    event.userEmail || null,
    event.productId || null,
    event.cartId || null,
    event.cartItemId || null,
    event.searchQuery || null,
    event.assistantMessage || null,
    toNumberOrNull(event.cartTotal),
    event.route || null,
    JSON.stringify(event.metadata || {}),
    JSON.stringify(event),
    toDateOrNull(event.occurredAt)
  ];

  return pool.query(
    `
    INSERT INTO user_actions (
      event_id,
      event_type,
      user_id,
      user_email,
      product_id,
      cart_id,
      cart_item_id,
      search_query,
      assistant_message,
      cart_total,
      route,
      metadata_json,
      raw_event_json,
      occurred_at
    ) VALUES (
      $1, $2, $3, $4, $5, $6, $7,
      $8, $9, $10, $11, $12, $13, $14
    )
    ON CONFLICT (event_id) DO NOTHING
    `,
    values
  );
}

function toNumberOrNull(value) {
  if (value === undefined || value === null || value === "") {
    return null;
  }

  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function toDateOrNull(value) {
  if (!value) {
    return null;
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}
