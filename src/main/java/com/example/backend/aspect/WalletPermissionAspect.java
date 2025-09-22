package com.example.backend.aspect;

import com.example.backend.annotation.RequireWalletPermission;
import com.example.backend.enums.PermissionType;
import com.example.backend.exception.BadRequestException;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.WalletPermissionService;
import com.example.backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletPermissionAspect {

    private final WalletPermissionService walletPermissionService;
    private final WalletService walletService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Around("@annotation(com.example.backend.annotation.RequireWalletPermission)")
    public Object checkWalletPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireWalletPermission annotation = method.getAnnotation(RequireWalletPermission.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new BadRequestException("Người dùng chưa đăng nhập hoặc thông tin xác thực không hợp lệ.");
        }

        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long userId = currentUser.getId();

        Long walletId = extractWalletId(joinPoint, annotation);
        if (walletId == null) {
            throw new BadRequestException("Không thể xác định ID của ví từ request.");
        }

        if (annotation.requireOwnership()) {
            if (!walletService.isWalletOwner(walletId, userId)) {
                log.warn("User {} không phải là chủ sở hữu của ví {}.", userId, walletId);
                throw new BadRequestException("Bạn không phải là chủ sở hữu của ví này.");
            }
            log.debug("Kiểm tra quyền sở hữu thành công cho user {} với ví {}", userId, walletId);
        } else {
            PermissionType requiredPermission = annotation.value();
            if (!walletPermissionService.hasPermission(walletId, userId, requiredPermission)) {
                log.warn("User {} không có quyền {} trên ví {}.", userId, requiredPermission, walletId);
                throw new BadRequestException("Bạn không có quyền '" + requiredPermission.getDisplayName() + "' trên ví này.");
            }
            log.debug("Kiểm tra quyền {} thành công cho user {} với ví {}", requiredPermission, userId, walletId);
        }

        return joinPoint.proceed();
    }

    private Long extractWalletId(ProceedingJoinPoint joinPoint, RequireWalletPermission annotation) {
        String walletIdExpression = annotation.walletId();
        if (walletIdExpression.isEmpty()) {
            throw new BadRequestException("Biểu thức walletId trong @RequireWalletPermission không được để trống.");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        try {
            Object value = expressionParser.parseExpression(walletIdExpression).getValue(context);
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.parseLong((String) value);
            }
            throw new BadRequestException("Không thể trích xuất ID ví hợp lệ từ biểu thức: " + walletIdExpression);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý biểu thức SpEL '{}': {}", walletIdExpression, e.getMessage());
            throw new BadRequestException("Đã xảy ra lỗi khi xác thực quyền truy cập ví.");
        }
    }
}