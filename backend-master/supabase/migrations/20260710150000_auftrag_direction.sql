-- Orders carry a stock direction chosen at creation: OUT = sale (goods issue),
-- IN = procurement (goods receipt). Stored as plain varchar (JPA enum name).
-- Existing orders default to OUT (the common sales case).
ALTER TABLE public.auftrag
    ADD COLUMN IF NOT EXISTS direction VARCHAR(10) NOT NULL DEFAULT 'OUT';
