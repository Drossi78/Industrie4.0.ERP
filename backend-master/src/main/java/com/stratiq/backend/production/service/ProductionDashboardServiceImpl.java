package com.stratiq.backend.production.service;

import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.production.dto.response.ProductionOrderDetailResponse;
import com.stratiq.backend.production.dto.response.ProductionOrderDetailResponse.AssetDetail;
import com.stratiq.backend.production.dto.response.ProductionOrderDetailResponse.AssignedUserInfo;
import com.stratiq.backend.production.dto.response.ProductionOrderDetailResponse.StepDetail;
import com.stratiq.backend.production.dto.response.ProductionOrderSummaryResponse;
import com.stratiq.backend.production.model.OrderProductionStep;
import com.stratiq.backend.production.model.ProductionStepStatus;
import com.stratiq.backend.production.repository.OrderProductionStepRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.user.model.AppUser;
import com.stratiq.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionDashboardServiceImpl implements ProductionDashboardService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderProductionStepRepository stepRepo;
    private final AppUserRepository userRepo;

    @Override
    public List<ProductionOrderSummaryResponse> getActiveOrders() {
        return orderRepo.findByStatus(OrderStatus.CONFIRMED).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public ProductionOrderDetailResponse getOrderDetail(UUID orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        // Batch-load all assigned users for this order to avoid N+1 queries.
        List<OrderProductionStep> allSteps = stepRepo.findByOrderIdOrderByStepOrderAsc(orderId);
        Set<UUID> userIds = allSteps.stream()
                .map(OrderProductionStep::getAssignedTo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> userEmails = userRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getEmail));

        List<AssetDetail> assets = items.stream()
                .map(item -> toAssetDetail(item, userEmails))
                .toList();

        String customerName = order.getCustomer() != null ? order.getCustomer().getName() : "";
        return new ProductionOrderDetailResponse(order.getId(), order.getOrderNumber(), customerName, assets);
    }

    private ProductionOrderSummaryResponse toSummary(Order order) {
        List<OrderProductionStep> steps = stepRepo.findByOrderIdOrderByStepOrderAsc(order.getId());
        int total = steps.size();
        int completed = (int) steps.stream().filter(s -> s.getStatus() == ProductionStepStatus.DONE).count();
        int progress = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
        String customerName = order.getCustomer() != null ? order.getCustomer().getName() : "";
        return new ProductionOrderSummaryResponse(
                order.getId(), order.getOrderNumber(), customerName,
                order.getOrderDate(), total, completed, progress);
    }

    private AssetDetail toAssetDetail(OrderItem item, Map<UUID, String> userEmails) {
        List<OrderProductionStep> steps = stepRepo.findByOrderItemIdOrderByStepOrderAsc(item.getId());
        int total = steps.size();
        int completed = (int) steps.stream().filter(s -> s.getStatus() == ProductionStepStatus.DONE).count();
        List<StepDetail> stepDetails = steps.stream()
                .map(s -> toStepDetail(s, userEmails))
                .toList();
        return new AssetDetail(item.getId(), item.getItemName(), item.getQuantity(), total, completed, stepDetails);
    }

    private StepDetail toStepDetail(OrderProductionStep step, Map<UUID, String> userEmails) {
        AssignedUserInfo assignedTo = null;
        if (step.getAssignedTo() != null && userEmails.containsKey(step.getAssignedTo())) {
            assignedTo = new AssignedUserInfo(step.getAssignedTo(), userEmails.get(step.getAssignedTo()));
        }
        return new StepDetail(step.getId(), step.getName(), step.getStepOrder(),
                step.getStatus(), assignedTo, step.getStartedAt(), step.getCompletedAt());
    }
}
