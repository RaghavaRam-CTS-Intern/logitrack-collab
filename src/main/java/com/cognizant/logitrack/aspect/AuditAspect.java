package com.cognizant.logitrack.aspect;

import com.cognizant.logitrack.entity.AuditLog;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.repository.AuditLogRepository;
import com.cognizant.logitrack.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Records an AuditLog entry for every successful state-changing request
 * (POST/PUT/PATCH/DELETE) handled by a controller — the project-wide audit
 * trail. Reads (GET) are intentionally not audited.
 *
 * AuthController and UserController audit their own events explicitly with
 * richer action names (LOGIN, USER_CREATED, ...), and AuditLogController is the
 * audit write hook itself, so all three are excluded here to avoid duplicate
 * or recursive entries.
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditAspect(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @AfterReturning("within(com.cognizant.logitrack.controller..*) "
            + "&& !within(com.cognizant.logitrack.controller.AuthController) "
            + "&& !within(com.cognizant.logitrack.controller.UserController) "
            + "&& !within(com.cognizant.logitrack.controller.AuditLogController)")
    public void auditWrite(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }
            HttpServletRequest request = attrs.getRequest();
            String action = actionFor(request.getMethod());
            if (action == null) {
                return; // GET or other non-mutating method — not audited
            }

            String entityType = entityTypeFor(joinPoint.getTarget().getClass().getSimpleName());
            User actor = currentUser();

            AuditLog auditLog = AuditLog.builder()
                    .user(actor)
                    .action(action)
                    .entityType(entityType)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit recorded: action={}, entityType={}, userId={}",
                    action, entityType, actor != null ? actor.getUserId() : null);
        } catch (Exception e) {
            // Auditing must never break the business request.
            log.warn("Failed to record audit log: {}", e.getMessage());
        }
    }

    private String actionFor(String httpMethod) {
        switch (httpMethod) {
            case "POST":
                return "CREATE";
            case "PUT":
            case "PATCH":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                return null;
        }
    }

    private String entityTypeFor(String controllerSimpleName) {
        return controllerSimpleName.endsWith("Controller")
                ? controllerSimpleName.substring(0, controllerSimpleName.length() - "Controller".length())
                : controllerSimpleName;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
