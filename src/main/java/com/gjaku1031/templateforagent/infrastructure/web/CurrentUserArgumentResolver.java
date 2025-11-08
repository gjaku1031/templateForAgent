package com.gjaku1031.templateforagent.infrastructure.web;

import com.gjaku1031.templateforagent.presentation.common.annotation.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 현재 로그인 사용자 ID를 주입하는 아규먼트 리졸버
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        try {
            // 기대 타입: CustomUserDetails(user.id)
            var userField = principal.getClass().getDeclaredField("user");
            userField.setAccessible(true);
            Object user = userField.get(principal);
            var idMethod = user.getClass().getMethod("getId");
            Object id = idMethod.invoke(user);
            return (id instanceof Long) ? id : null;
        } catch (Exception ignore) {
            return null;
        }
    }
}

