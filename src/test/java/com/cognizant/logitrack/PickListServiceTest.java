package com.cognizant.logitrack;

import com.cognizant.logitrack.dto.PickListDTO;
import com.cognizant.logitrack.entity.PickList;
import com.cognizant.logitrack.enums.PickListStatus;
import com.cognizant.logitrack.repository.PickListRepository;
import com.cognizant.logitrack.service.PickListService;
import com.cognizant.logitrack.serviceImplementation.PickListServiceImpl;
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
class PickListServiceTest {

    @Mock
    private PickListRepository pickListRepository;

    @InjectMocks
    private PickListServiceImpl pickListService;

    @Test
    void createPickList_valid_savedWithOpenStatus() {
        when(pickListRepository.save(any(PickList.class))).thenAnswer(inv -> {
            PickList p = inv.getArgument(0);
            p.setPickListId(1);
            return p;
        });

        PickListDTO dto = PickListDTO.builder().freightOrderId(1).warehouseId(1).build();
        PickListDTO result = pickListService.createPickList(dto);

        assertNotNull(result);
        assertEquals(PickListStatus.OPEN, result.getStatus());
    }

    @Test
    void updateStatus_openToCompleted() {
        PickList pickList = PickList.builder().pickListId(1).status(PickListStatus.OPEN).build();
        when(pickListRepository.findById(1)).thenReturn(Optional.of(pickList));
        when(pickListRepository.save(any(PickList.class))).thenAnswer(inv -> inv.getArgument(0));

        PickListDTO result = pickListService.updatePickListStatus(1, PickListStatus.COMPLETED);

        assertEquals(PickListStatus.COMPLETED, result.getStatus());
    }
}
