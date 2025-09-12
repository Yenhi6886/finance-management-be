package com.example.backend.annotation;

import com.example.backend.enums.PermissionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để kiểm tra quyền truy cập ví
 * Sử dụng trên các method controller để tự động kiểm tra quyền
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireWalletPermission {
    
    /**
     * Loại quyền cần kiểm tra
     */
    PermissionType value();
    
    /**
     * Tên parameter chứa wallet ID (mặc định là "walletId")
     */
    String walletIdParam() default "walletId";
    
    /**
     * Tên parameter chứa wallet ID (mặc định là "id")
     */
    String idParam() default "id";
    
    /**
     * Có kiểm tra quyền sở hữu không (chỉ chủ sở hữu mới có quyền)
     */
    boolean requireOwnership() default false;
}
