package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.CreateReservationRequest;
import com.stratiq.backend.warehouse.dto.response.ReservationResponse;

import java.util.List;
import java.util.UUID;

public interface StockReservationService {

    // Reserve stock; fails with InsufficientStockException if not enough is available.
    ReservationResponse create(CreateReservationRequest req);

    // Release an active reservation, freeing the blocked stock.
    ReservationResponse release(UUID id);

    // List reservations, optionally filtered by warehouse, item and/or status (defaults to ACTIVE).
    List<ReservationResponse> list(UUID warehouseId, UUID itemId, String status);
}
