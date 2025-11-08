package com.gjaku1031.templateforagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 애플리케이션 부트스트랩 클래스
 * <p>Spring Boot 실행 진입점.</p>
 */
@SpringBootApplication
public class TemplateForAgentApplication {

    /**
     * 메인 엔트리포인트
     *
     * @param args 프로그램 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(TemplateForAgentApplication.class, args);
    }

}
