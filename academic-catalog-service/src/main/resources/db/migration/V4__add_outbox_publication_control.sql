ALTER TABLE outbox_event
    ADD COLUMN next_attempt_at timestamp with time zone,
    ADD COLUMN locked_at timestamp with time zone,
    ADD COLUMN last_error varchar(1000),
    ADD COLUMN broker_topic varchar(180),
    ADD COLUMN broker_partition integer,
    ADD COLUMN broker_offset bigint;

CREATE INDEX idx_outbox_publication_candidates
    ON outbox_event(status, next_attempt_at, locked_at, occurred_at);
