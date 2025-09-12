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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Aspect để xử lý kiểm tra quyền truy cập ví tự động
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletPermissionAspect {

    private final WalletPermissionService walletPermissionService;
    private final WalletService walletService;

    @Around("@annotation(com.example.backend.annotation.RequireWalletPermission)")
    public Object checkWalletPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireWalletPermission annotation = method.getAnnotation(RequireWalletPermission.class);
        
        // Lấy thông tin user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new BadRequestException("Người dùng chưa đăng nhập");
        }
        
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long userId = currentUser.getId();
        
        // Lấy wallet ID từ parameters
        Long walletId = extractWalletId(joinPoint, annotation);
        if (walletId == null) {
            throw new BadRequestException("Không tìm thấy wallet ID trong request");
        }
        
        // Kiểm tra quyền sở hữu nếu cần
        if (annotation.requireOwnership()) {
            if (!walletService.isWalletOwner(walletId, userId)) {
                throw new BadRequestException("Bạn không có quyền sở hữu ví này");
            }
            log.debug("Kiểm tra quyền sở hữu thành công cho user {} với ví {}", userId, walletId);
            return joinPoint.proceed();
        }
        
        // Kiểm tra quyền cụ thể
        PermissionType requiredPermission = annotation.value();
        boolean hasPermission = walletPermissionService.hasPermission(walletId, userId, requiredPermission);
        
        if (!hasPermission) {
            log.warn("User {} không có quyền {} với ví {}", userId, requiredPermission.getDisplayName(), walletId);
            throw new BadRequestException("Bạn không có quyền " + requiredPermission.getDisplayName() + " với ví này");
        }
        
        log.debug("Kiểm tra quyền {} thành công cho user {} với ví {}", 
                requiredPermission.getDisplayName(), userId, walletId);
        
        return joinPoint.proceed();
    }
    
    private Long extractWalletId(ProceedingJoinPoint joinPoint, RequireWalletPermission annotation) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        
        // Tìm parameter có annotation @PathVariable với tên phù hợp
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            
            if (pathVariable != null) {
                String paramName = pathVariable.value().isEmpty() ? parameter.getName() : pathVariable.value();
                
                // Kiểm tra theo walletIdParam hoặc idParam
                if (paramName.equals(annotation.walletIdParam()) || 
                    paramName.equals(annotation.idParam()) ||
                    paramName.equals("id") || 
                    paramName.equals("walletId")) {
                    
                    if (args[i] instanceof Long) {
                        return (Long) args[i];
                    } else if (args[i] instanceof String) {
                        try {
                            return Long.parseLong((String) args[i]);
                        } catch (NumberFormatException e) {
                            log.error("Không thể parse wallet ID: {}", args[i]);
                        }
                    }
                }
            }
        }
        
        return null;
    }
}
