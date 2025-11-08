package com.gjaku1031.templateforagent.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정
 * <p>Auditing 활성화.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
