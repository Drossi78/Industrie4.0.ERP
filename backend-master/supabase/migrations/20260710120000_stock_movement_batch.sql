ALTER TABLE public.stock_movement ADD COLUMN IF NOT EXISTS batch_id UUID REFERENCES public.batches(id);

CREATE INDEX IF NOT EXISTS idx_stock_movement_batch_id ON public.stock_movement(batch_id);
