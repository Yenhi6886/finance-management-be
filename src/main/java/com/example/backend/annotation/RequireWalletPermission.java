package com.example.backend.annotation;

import com.example.backend.enums.PermissionType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireWalletPermission {

    PermissionType value();

    String walletId() default "";

    boolean requireOwnership() default false;
}