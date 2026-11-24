package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.CustomerCreateRequest;
import com.stratiq.backend.order.dto.request.CustomerUpdateRequest;
import com.stratiq.backend.order.mapper.CustomerMapper;
import com.stratiq.backend.order.model.Customer;
import com.stratiq.backend.order.repository.CustomerRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();

        testCustomer = new Customer();
        testCustomer.setId(testCustomerId);
        testCustomer.setName("Mustermann GmbH");
        testCustomer.setEmail("info@mustermann.de");
    }

    @Test
    void testCreate_Success() {
        CustomerCreateRequest request = new CustomerCreateRequest(
            "Mustermann GmbH", "info@mustermann.de", null, null,
            null, null, null, null, null, null, null
        );

        when(mapper.toEntity(request)).thenReturn(testCustomer);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        customerService.create(request);

        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void testGet_Success() {
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));

        customerService.get(testCustomerId);

        verify(customerRepository, times(1)).findById(testCustomerId);
    }

    @Test
    void testGet_NotFound() {
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customerService.get(testCustomerId));
    }

    @Test
    void testList_Success() {
        var pageable = PageRequest.of(0, 10);
        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testCustomer)));

        customerService.list(pageable);

        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByName_Success() {
        when(customerRepository.findByName("Mustermann GmbH")).thenReturn(Optional.of(testCustomer));

        customerService.findByName("Mustermann GmbH");

        verify(customerRepository, times(1)).findByName("Mustermann GmbH");
    }

    @Test
    void testUpdate_Success() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
            "Updated GmbH", null, null, null, null, null, null, null, null, null, null
        );

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));

        customerService.update(testCustomerId, request);

        verify(customerRepository, times(1)).findById(testCustomerId);
        verify(mapper, times(1)).update(testCustomer, request);
    }

    @Test
    void testUpdate_NotFound() {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
            "Updated GmbH", null, null, null, null, null, null, null, null, null, null
        );

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customerService.update(testCustomerId, request));
    }

    @Test
    void testDelete_Success() {
        when(customerRepository.existsById(testCustomerId)).thenReturn(true);

        customerService.delete(testCustomerId);

        verify(customerRepository, times(1)).deleteById(testCustomerId);
    }

    @Test
    void testDelete_NotFound() {
        when(customerRepository.existsById(testCustomerId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> customerService.delete(testCustomerId));
        verify(customerRepository, never()).deleteById(any());
    }
}
