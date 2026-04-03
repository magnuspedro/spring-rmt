package br.com.magnus.projectsyncbff.audit;

import br.com.magnus.projectsyncbff.validation.UploadValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Aspect
@Component
public class AuditLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditLoggingAspect.class);

    @Around("""
            execution(* br.com.magnus.projectsyncbff.controller.HtmxController.registration(..)) ||
            execution(* br.com.magnus.projectsyncbff.controller.RestfulController.registration(..)) ||
            execution(* br.com.magnus.projectsyncbff.controller.HtmxController.getProject(..)) ||
            execution(* br.com.magnus.projectsyncbff.controller.RestfulController.getProject(..)) ||
            execution(* br.com.magnus.projectsyncbff.controller.HtmxController.downloadProject(..)) ||
            execution(* br.com.magnus.projectsyncbff.controller.RestfulController.downloadProject(..))
            """)
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        var correlationId = UUID.randomUUID().toString();
        var startedAt = System.currentTimeMillis();
        var request = currentRequest();
        var event = joinPoint.getSignature().getName();
        var clientIp = request == null ? "unknown" : clientIp(request);
        var file = findArg(joinPoint.getArgs(), MultipartFile.class);
        var projectId = findProjectId(joinPoint.getArgs());

        MDC.put("correlationId", correlationId);
        try {
            var result = joinPoint.proceed();
            log.info("[AUDIT] event={} clientIp={} filename={} size={} projectId={} timestamp={} durationMs={} validationResult=passed correlationId={}",
                    event,
                    clientIp,
                    file == null ? "-" : file.getOriginalFilename(),
                    file == null ? "-" : file.getSize(),
                    projectId,
                    Instant.now(),
                    System.currentTimeMillis() - startedAt,
                    correlationId);
            return result;
        } catch (UploadValidationException exception) {
            log.warn("[AUDIT] event={} clientIp={} filename={} size={} projectId={} timestamp={} durationMs={} validationResult=failed errors={} correlationId={}",
                    event,
                    clientIp,
                    file == null ? "-" : file.getOriginalFilename(),
                    file == null ? "-" : file.getSize(),
                    projectId,
                    Instant.now(),
                    System.currentTimeMillis() - startedAt,
                    exception.getErrors(),
                    correlationId);
            throw exception;
        } catch (Throwable throwable) {
            log.warn("[AUDIT] event={} clientIp={} filename={} size={} projectId={} timestamp={} durationMs={} validationResult=error correlationId={}",
                    event,
                    clientIp,
                    file == null ? "-" : file.getOriginalFilename(),
                    file == null ? "-" : file.getSize(),
                    projectId,
                    Instant.now(),
                    System.currentTimeMillis() - startedAt,
                    correlationId);
            throw throwable;
        } finally {
            MDC.remove("correlationId");
        }
    }

    private HttpServletRequest currentRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private MultipartFile findArg(Object[] args, Class<MultipartFile> type) {
        for (var arg : args) {
            if (type.isInstance(arg)) {
                return type.cast(arg);
            }
        }
        return null;
    }

    private String findProjectId(Object[] args) {
        for (var arg : args) {
            if (arg instanceof String value) {
                return value;
            }
        }
        return "-";
    }
}
