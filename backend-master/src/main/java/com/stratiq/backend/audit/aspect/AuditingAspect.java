package com.stratiq.backend.audit.aspect;

import com.stratiq.backend.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.stratiq.backend.user.model.AppUser;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditingAspect {
    // AOP intercepts create/update/delete calls. Logs to audit_logs table with user context.

    private final AuditService auditService;

    @AfterReturning(
        // Matches all service implementations in domain/**/ packages.
        pointcut = "execution(* com.stratiq.backend.*.service.*ServiceImpl.create(..)) || " +
                   "execution(* com.stratiq.backend.*.service.*ServiceImpl.update(..)) || " +
                   "execution(* com.stratiq.backend.*.service.*ServiceImpl.delete(..))",
        returning = "result"
    )
    public void logMutation(JoinPoint joinPoint, Object result) {
        try {
            String action = joinPoint.getSignature().getName().toUpperCase();
            String entityType = joinPoint.getSignature().getDeclaringType().getSimpleName()
                    .replace("ServiceImpl", "").toUpperCase();
            UUID userId = getCurrentUserId();
            // Logs action with entity type, user ID, and result DTO for compliance.
            auditService.log(userId, entityType, null, action, null, result != null ? result.toString() : null);
        } catch (Exception e) {
            // Silently fails. Audit failure doesn't block the mutation.
            log.warn("AuditingAspect failed: {}", e.getMessage());
        }
    }

    // Extracts current user from Spring Security context. Returns null if unauthenticated.
    private UUID getCurrentUserId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AppUser user) {
                return user.getId();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
