package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.InvoiceCreateRequest;
import com.stratiq.backend.order.dto.request.InvoiceUpdateRequest;
import com.stratiq.backend.order.dto.response.InvoiceResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.order.mapper.InvoiceMapper;
import com.stratiq.backend.order.model.Invoice;
import com.stratiq.backend.order.model.InvoiceStatus;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.repository.InvoiceRepository;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.order.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final InvoiceMapper mapper;

    // German standard VAT rate (Regelsteuersatz). Overridable via app.invoicing.vat-rate
    // for the rare case a customer's items fall under the reduced rate instead.
    @Value("${app.invoicing.vat-rate:0.19}")
    private BigDecimal vatRate;

    @Override
    public InvoiceResponse create(InvoiceCreateRequest req) {
        // Validates order exists before creating invoice. Cross-domain validation.
        Order order = orderRepo.findById(req.orderId())
                .orElseThrow(() -> new NotFoundException("Order", req.orderId()));

        // Amount is calculated from the order's line items, not taken from client input.
        BigDecimal subtotal = orderItemRepo.findByOrderId(order.getId()).stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = subtotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        LocalDateTime issueDate = req.issueDate() != null ? req.issueDate() : LocalDateTime.now();
        String invoiceNumber = generateInvoiceNumber(issueDate.getYear());

        Invoice invoice = mapper.toEntity(req, order, invoiceNumber, subtotal, taxAmount, totalAmount);
        invoice.setIssueDate(issueDate);
        return mapper.toResponse(repo.save(invoice));
    }

    // Sequential per calendar year, e.g. INV-2026-0001. Required by § 14 Abs. 4 Nr. 4 UStG
    // (invoice numbers must be unique and consecutive). Derived from the highest existing
    // number for that year, so a concurrent create in the same instant could in theory race;
    // the DB's UNIQUE constraint on invoice_number turns that into a failed save rather than a duplicate.
    private String generateInvoiceNumber(int year) {
        Integer maxSequence = repo.findMaxSequenceForYear(year);
        int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;
        return "INV-%d-%04d".formatted(year, nextSequence);
    }

    @Override
    public InvoiceResponse update(UUID id, InvoiceUpdateRequest req) {
        Invoice inv = find(id);
        mapper.update(inv, req);
        return mapper.toResponse(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> findByOrder(UUID orderId) {
        return repo.findByOrderId(orderId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> findByStatus(InvoiceStatus status) {
        return repo.findByStatus(status).stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Invoice", id);
        repo.deleteById(id);
    }

    private Invoice find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Invoice", id));
    }
}
