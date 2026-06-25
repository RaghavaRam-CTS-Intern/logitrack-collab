package com.cognizant.logitrack;

import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import com.cognizant.logitrack.entity.ShipmentDocument;
import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.enums.DocumentType;
import com.cognizant.logitrack.repository.ShipmentDocumentRepository;
import com.cognizant.logitrack.service.ShipmentDocumentService;
import com.cognizant.logitrack.serviceImplementation.ShipmentDocumentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentDocumentServiceTest {

    @Mock
    private ShipmentDocumentRepository documentRepository;

    @InjectMocks
    private ShipmentDocumentServiceImpl documentService;

    @Test
    void uploadDocument_savedAsPending() {
        when(documentRepository.save(any(ShipmentDocument.class))).thenAnswer(inv -> {
            ShipmentDocument d = inv.getArgument(0);
            d.setDocumentId(1);
            return d;
        });

        ShipmentDocumentDTO dto = ShipmentDocumentDTO.builder().shipmentId(1).documentType(DocumentType.BOL).filePath("/files/bol.pdf").submittedDate(LocalDate.now()).build();
        ShipmentDocumentDTO result = documentService.uploadDocument(dto);

        assertNotNull(result);
        assertEquals(DocumentStatus.PENDING, result.getStatus());
    }

    @Test
    void updateStatus_pendingToApproved() {
        ShipmentDocument doc = ShipmentDocument.builder().documentId(1).status(DocumentStatus.PENDING).build();
        when(documentRepository.findById(1)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(ShipmentDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        ShipmentDocumentDTO result = documentService.updateDocumentStatus(1, DocumentStatus.APPROVED);

        assertEquals(DocumentStatus.APPROVED, result.getStatus());
    }
}
