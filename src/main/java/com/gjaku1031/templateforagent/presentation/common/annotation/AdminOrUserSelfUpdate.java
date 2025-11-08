package com.gjaku1031.templateforagent.presentation.common.annotation;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasPermission(#id, 'User', 'UPDATE')")
public @interface AdminOrUserSelfUpdate {}

