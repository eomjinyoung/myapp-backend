package com.example.vibeapp.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Aspect
@Component
public class AuditLogAspect {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Around("@annotation(auditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String eventType = auditLog.value();
        String ipAddress = getClientIp();
        String userEmail = resolveEmail(joinPoint);
        String result = "SUCCESS";
        String reason = "-";

        try {
            Object response = joinPoint.proceed();
            if (response instanceof org.springframework.http.ResponseEntity<?> entity) {
                if (!entity.getStatusCode().is2xxSuccessful()) {
                    result = "FAILURE";
                    reason = "Status: " + entity.getStatusCode();
                }
            }
            return response;
        } catch (Exception e) {
            result = "FAILURE";
            reason = e.getMessage();
            throw e;
        } finally {
            auditLogger.info("Audit log entry",
                    kv("event", eventType),
                    kv("ip", ipAddress),
                    kv("user", userEmail),
                    kv("result", result),
                    kv("reason", reason));
        }
    }

    private String getClientIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String resolveEmail(ProceedingJoinPoint joinPoint) {
        // This is a simplified resolver. In a real app, you'd check arguments or
        // SecurityContext.
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof com.example.vibeapp.user.dto.LoginRequestDto loginRequest) {
                return loginRequest.email();
            }
            if (arg instanceof com.example.vibeapp.user.dto.TokenReissueRequestDto reissueRequest) {
                // For reissue, we might need to decode the token or wait for it to be
                // identified later
                return "TOKEN_REISSUE_USER";
            }
        }

        // Check SecurityContext if available
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        return "UNKNOWN";
    }
}
