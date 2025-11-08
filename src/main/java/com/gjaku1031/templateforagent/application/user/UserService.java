package com.gjaku1031.templateforagent.application.user;

import com.gjaku1031.templateforagent.common.error.ErrorCode;
import com.gjaku1031.templateforagent.common.error.exception.BusinessException;
import com.gjaku1031.templateforagent.common.error.exception.NotFoundException;
import com.gjaku1031.templateforagent.domain.user.User;
import com.gjaku1031.templateforagent.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long create(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email already exists");
        }
        User saved = userRepository.save(User.builder().username(username).email(email).build());
        return saved.getId();
    }

    @Transactional
    public Long register(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email already exists");
        }
        String encoded = passwordEncoder.encode(rawPassword);
        User saved = userRepository.save(User.builder().username(username).email(email).password(encoded).role("ROLE_USER").build());
        return saved.getId();
    }

    public User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public void update(Long id, String username, String email) {
        User user = get(id);
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email already exists");
        }
        user.change(username, email);
    }

    @Transactional
    public void delete(Long id) {
        User user = get(id);
        userRepository.delete(user);
    }

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}

