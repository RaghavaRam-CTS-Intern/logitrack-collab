package com.cognizant.logitrack;

import com.cognizant.logitrack.dto.NotificationDTO;
import com.cognizant.logitrack.entity.Notification;
import com.cognizant.logitrack.enums.NotificationCategory;
import com.cognizant.logitrack.enums.NotificationStatus;
import com.cognizant.logitrack.repository.NotificationRepository;
import com.cognizant.logitrack.service.NotificationService;
import com.cognizant.logitrack.serviceImplementation.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void sendNotification_savedAsUnread() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setNotificationId(1);
            return n;
        });

        NotificationDTO dto = NotificationDTO.builder().userId(1).message("Test message").category(NotificationCategory.SHIPMENT).build();
        NotificationDTO result = notificationService.sendNotification(dto);

        assertNotNull(result);
        assertEquals(NotificationStatus.UNREAD, result.getStatus());
    }

    @Test
    void markAsRead_changesStatus() {
        Notification n = Notification.builder().notificationId(1).status(NotificationStatus.UNREAD).build();
        when(notificationRepository.findById(1)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationDTO result = notificationService.markAsRead(1);

        assertEquals(NotificationStatus.READ, result.getStatus());
    }

    @Test
    void getUnreadCount_returnsCorrectNumber() {
        when(notificationRepository.countByUserIdAndStatus(1, NotificationStatus.UNREAD)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1);

        assertEquals(5L, count);
    }

    @Test
    void markAllAsRead_updatesAllUnread() {
        Notification n1 = Notification.builder().notificationId(1).status(NotificationStatus.UNREAD).build();
        Notification n2 = Notification.builder().notificationId(2).status(NotificationStatus.UNREAD).build();
        when(notificationRepository.findByUserIdAndStatus(1, NotificationStatus.UNREAD))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(1);

        assertEquals(NotificationStatus.READ, n1.getStatus());
        assertEquals(NotificationStatus.READ, n2.getStatus());
        verify(notificationRepository).saveAll(anyList());
    }
}
