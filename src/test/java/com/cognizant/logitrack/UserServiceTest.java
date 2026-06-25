package com.cognizant.logitrack;

import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.RegisterRequestDTO;
import com.cognizant.logitrack.dto.UserDTO;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.enums.Role;
import com.cognizant.logitrack.enums.UserStatus;
import com.cognizant.logitrack.repository.UserRepository;
import com.cognizant.logitrack.service.UserService;
import com.cognizant.logitrack.serviceImplementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private com.cognizant.logitrack.service.AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO("John Doe", "john@logitrack.com",
                "password123", Role.SHIPPER, "9000000000", 1);
    }

    @Test
    void createUser_validInput_returnsUserDTO() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(1);
            return u;
        });

        UserDTO result = userService.createUser(registerRequest);

        assertNotNull(result);
        assertEquals("john@logitrack.com", result.getEmail());
        assertEquals(Role.SHIPPER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsBadRequestException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.createUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_validId_returnsDTO() {
        User user = User.builder().userId(1).name("John").email("john@logitrack.com")
                .role(Role.SHIPPER).status(UserStatus.ACTIVE).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1);

        assertEquals(1, result.getUserId());
        assertEquals("john@logitrack.com", result.getEmail());
    }

    @Test
    void getUserById_invalidId_throwsResourceNotFoundException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99));
    }

    @Test
    void updateUserStatus_changesStatus() {
        User user = User.builder().userId(1).name("John").email("john@logitrack.com")
                .role(Role.SHIPPER).status(UserStatus.ACTIVE).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO result = userService.updateUserStatus(1, UserStatus.INACTIVE);

        assertEquals(UserStatus.INACTIVE, result.getStatus());
    }
}
