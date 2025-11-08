package com.gjaku1031.templateforagent.infrastructure.security;

import com.gjaku1031.templateforagent.domain.user.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 애플리케이션 전용 UserDetails 구현
 * <p>SecurityContext에서 현재 로그인 사용자의 식별자/권한을 제공.</p>
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    // CurrentUserArgumentResolver가 리플렉션으로 접근하므로 필드명 'user' 유지
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}

