CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    response_body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);