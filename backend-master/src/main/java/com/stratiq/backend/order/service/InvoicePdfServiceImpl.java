package com.stratiq.backend.order.service;

import com.stratiq.backend.order.service.InvoicePdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class InvoicePdfServiceImpl implements InvoicePdfService {

    @Override
    public byte[] export(UUID invoiceId) {
        // TODO: Implement PDF export using iText or Apache PDFBox library.
        // Currently returns empty byte array as placeholder.
        log.warn("InvoicePdfService.export() is not yet implemented for invoiceId={}", invoiceId);
        return new byte[0];
    }
}
