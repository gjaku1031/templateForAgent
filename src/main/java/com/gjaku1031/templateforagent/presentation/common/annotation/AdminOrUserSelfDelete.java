package com.gjaku1031.templateforagent.presentation.common.annotation;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

/**
 * 관리자 또는 본인(DELETE) 접근 허용
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasPermission(#id, 'User', 'DELETE')")
public @interface AdminOrUserSelfDelete {}

