package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.OrderFulfillRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;

import java.util.List;
import java.util.UUID;

public interface OrderFulfillmentService {

    // Books the open quantity (ordered - already booked) of each order item as a stock
    // movement linked to the order. Direction IN = goods receipt, OUT = goods issue.
    List<StockMovementResponse> fulfill(UUID orderId, OrderFulfillRequest req);
}
