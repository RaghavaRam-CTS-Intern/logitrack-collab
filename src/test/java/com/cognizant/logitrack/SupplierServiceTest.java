package com.cognizant.logitrack;

import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.SupplierDTO;
import com.cognizant.logitrack.entity.Supplier;
import com.cognizant.logitrack.enums.SupplierStatus;
import com.cognizant.logitrack.repository.SupplierRepository;
import com.cognizant.logitrack.service.SupplierService;
import com.cognizant.logitrack.serviceImplementation.SupplierServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void addSupplier_valid_returnsDTO() {
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            s.setSupplierId(1);
            return s;
        });

        SupplierDTO dto = SupplierDTO.builder().name("Acme").category("Electronics").contactDetails("acme@x.com").leadTimeDays(7).build();
        SupplierDTO result = supplierService.addSupplier(dto);

        assertNotNull(result);
        assertEquals("Acme", result.getName());
        assertEquals(SupplierStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getById_invalid_throwsResourceNotFoundException() {
        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.getById(99));
    }
}
