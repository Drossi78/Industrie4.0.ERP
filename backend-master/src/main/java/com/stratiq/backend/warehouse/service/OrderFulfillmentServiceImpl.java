package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.shared.exception.InsufficientStockException;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.dto.request.GoodsIssueRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsReceiptRequest;
import com.stratiq.backend.warehouse.dto.request.OrderFulfillRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.warehouse.dto.response.WarehouseStockResponse;
import com.stratiq.backend.warehouse.model.StockMovement;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderFulfillmentServiceImpl implements OrderFulfillmentService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final StockMovementRepository movementRepo;
    private final StockService stockService;

    @Override
    public List<StockMovementResponse> fulfill(UUID orderId, OrderFulfillRequest req) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        String direction = req.direction() == null ? "" : req.direction().trim().toUpperCase();
        if (!direction.equals("IN") && !direction.equals("OUT")) {
            throw new IllegalArgumentException("direction must be IN or OUT");
        }
        // Goods receipt always targets a single warehouse.
        if (direction.equals("IN") && req.warehouseId() == null) {
            throw new IllegalArgumentException("warehouseId is required for direction IN");
        }

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order has no items to fulfill");
        }

        // Movements already booked for this order, used to only book the still-open quantity.
        List<StockMovement> existing = movementRepo.findByOrderId(orderId);

        // Per-item batch metadata for goods receipt (direction IN). Keyed by item id.
        Map<UUID, OrderFulfillRequest.BatchLine> batchByItem = req.batchLines() == null
                ? Map.of()
                : req.batchLines().stream()
                    .filter(bl -> bl.itemId() != null)
                    .collect(Collectors.toMap(OrderFulfillRequest.BatchLine::itemId,
                            Function.identity(), (a, b) -> a));

        String note = "Order " + (order.getOrderNumber() != null ? order.getOrderNumber() : orderId);
        List<StockMovementResponse> created = new ArrayList<>();

        for (OrderItem item : items) {
            UUID itemId = item.getItem().getId();
            // Order quantities are decimal; stock movements are whole units -> round to nearest.
            long ordered = item.getQuantity().setScale(0, RoundingMode.HALF_UP).longValueExact();
            long alreadyBooked = existing.stream()
                    .filter(m -> m.getItem() != null && m.getItem().getId().equals(itemId))
                    .filter(m -> direction.equalsIgnoreCase(m.getType()))
                    .mapToLong(m -> m.getQuantity() != null ? m.getQuantity() : 0L)
                    .sum();
            long open = ordered - alreadyBooked;
            if (open <= 0) {
                continue;
            }

            if (direction.equals("IN")) {
                OrderFulfillRequest.BatchLine batch = batchByItem.get(itemId);
                String batchNumber = batch != null ? batch.batchNumber() : null;
                LocalDateTime expiryDate = batch != null ? batch.expiryDate() : null;
                created.add(stockService.goodsReceipt(
                        new GoodsReceiptRequest(itemId, req.warehouseId(), open, batchNumber, expiryDate, note, orderId)));
            } else {
                created.addAll(issueAcrossWarehouses(itemId, open, note, orderId, item.getItemName()));
            }
        }

        if (created.isEmpty()) {
            throw new IllegalArgumentException("Order is already fully booked in this direction");
        }
        return created;
    }

    // Books a goods issue spread across all warehouses that hold stock of the item,
    // taking from the fullest warehouse first until the required quantity is covered.
    private List<StockMovementResponse> issueAcrossWarehouses(UUID itemId, long needed,
                                                             String note, UUID orderId, String itemName) {
        List<WarehouseStockResponse> perWarehouse = movementRepo.findStockByWarehouseForItem(itemId).stream()
                .filter(w -> w.balance() != null && w.balance() > 0)
                .sorted(Comparator.comparingLong((WarehouseStockResponse w) -> w.balance()).reversed())
                .toList();

        List<StockMovementResponse> created = new ArrayList<>();
        long remaining = needed;
        for (WarehouseStockResponse w : perWarehouse) {
            if (remaining <= 0) {
                break;
            }
            long take = Math.min(remaining, w.balance());
            created.addAll(stockService.goodsIssue(
                    new GoodsIssueRequest(itemId, w.warehouseId(), take, null, note, orderId)));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException(
                    "Insufficient stock across warehouses for '%s': missing %d".formatted(itemName, remaining));
        }
        return created;
    }
}
