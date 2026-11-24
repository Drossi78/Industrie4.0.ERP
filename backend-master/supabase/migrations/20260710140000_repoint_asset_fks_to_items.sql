-- The "asset -> item" refactor switched every JPA entity to reference items(id),
-- but the legacy foreign keys on the *.asset_id columns still validated against the old,
-- now-abandoned `asset` table. Inserting a valid item id therefore raised a
-- data-integrity / foreign-key violation (e.g. adding an item to an order).
-- Repoint all asset_id foreign keys to items(id). The `asset` table has no entity and no rows.

-- Order items (auftrag_assets.asset_id)
ALTER TABLE public.auftrag_assets   DROP CONSTRAINT IF EXISTS fk_auftrag_items_asset;
ALTER TABLE public.auftrag_assets   DROP CONSTRAINT IF EXISTS fk_auftrag_assets_item;
ALTER TABLE public.auftrag_assets
    ADD CONSTRAINT fk_auftrag_assets_item FOREIGN KEY (asset_id) REFERENCES public.items(id);

-- Stock movements (stock_movement.asset_id)
ALTER TABLE public.stock_movement   DROP CONSTRAINT IF EXISTS fkkv6aaghrak19carehe6u3aoyg;
ALTER TABLE public.stock_movement   DROP CONSTRAINT IF EXISTS fk_stock_movement_item;
ALTER TABLE public.stock_movement
    ADD CONSTRAINT fk_stock_movement_item FOREIGN KEY (asset_id) REFERENCES public.items(id);

-- Stock reservations (stock_reservation.asset_id)
ALTER TABLE public.stock_reservation DROP CONSTRAINT IF EXISTS stock_reservation_asset_id_fkey;
ALTER TABLE public.stock_reservation DROP CONSTRAINT IF EXISTS fk_stock_reservation_item;
ALTER TABLE public.stock_reservation
    ADD CONSTRAINT fk_stock_reservation_item FOREIGN KEY (asset_id) REFERENCES public.items(id);

-- Production steps (production_steps.asset_id)
ALTER TABLE public.production_steps  DROP CONSTRAINT IF EXISTS fk_production_steps_asset;
ALTER TABLE public.production_steps  DROP CONSTRAINT IF EXISTS fk_production_steps_item;
ALTER TABLE public.production_steps
    ADD CONSTRAINT fk_production_steps_item FOREIGN KEY (asset_id) REFERENCES public.items(id);
