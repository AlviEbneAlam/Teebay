CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE teebays.rent_bookings
  ADD COLUMN IF NOT EXISTS period tsrange
    GENERATED ALWAYS AS (tsrange(rent_start_time, rent_end_time, '[]'))
    STORED;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
      FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
     WHERE c.relname = 'idx_rent_bookings_period'
       AND n.nspname = 'teebays'
  ) THEN
    CREATE INDEX idx_rent_bookings_period
      ON teebays.rent_bookings USING GIST (product_id, period);
  END IF;
END
$$;

ALTER TABLE teebays.rent_bookings
  ADD CONSTRAINT IF NOT EXISTS no_overlapping_bookings
    EXCLUDE USING GIST (
      product_id WITH =,
      period     WITH &&
    );