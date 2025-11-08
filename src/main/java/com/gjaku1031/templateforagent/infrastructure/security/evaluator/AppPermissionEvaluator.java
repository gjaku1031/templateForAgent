package com.gjaku1031.templateforagent.infrastructure.security.evaluator;

import com.gjaku1031.templateforagent.domain.board.BoardRepository;
import com.gjaku1031.templateforagent.domain.user.UserRepository;
import com.gjaku1031.templateforagent.infrastructure.security.CustomUserDetails;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * 도메인 권한 평가기
 * <p>관리자(ROLE_ADMIN) 즉시 허용, User: 자기 자신, Board: 소유자(작성자)만 허용.</p>
 */
@Component
@RequiredArgsConstructor
public class AppPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // targetDomainObject 기반 검증은 미사용. 명시적 ID 기반 API만 지원.
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (isAdmin(authentication)) return true;
        if (!(targetId instanceof Long id)) return false;

        Long currentUserId = extractUserId(authentication);
        if (currentUserId == null) return false;

        String type = targetType == null ? "" : targetType;
        switch (type) {
            case "User":
                // 본인만 허용
                return id.equals(currentUserId);
            case "Board":
                // 보드 작성자만 허용
                return boardRepository.findById(id)
                        .map(b -> b.getAuthor() != null && currentUserId.equals(b.getAuthor().getId()))
                        .orElse(false);
            default:
                return false;
        }
    }

    private boolean isAdmin(Authentication authentication) {
        for (GrantedAuthority a : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(a.getAuthority())) return true;
        }
        return false;
    }

    private Long extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUser().getId();
        }
        try {
            var userField = principal.getClass().getDeclaredField("user");
            userField.setAccessible(true);
            Object user = userField.get(principal);
            var idMethod = user.getClass().getMethod("getId");
            Object id = idMethod.invoke(user);
            return (id instanceof Long) ? (Long) id : null;
        } catch (Exception ignore) {
            return null;
        }
    }
}

