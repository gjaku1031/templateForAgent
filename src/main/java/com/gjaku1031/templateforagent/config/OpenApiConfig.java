package com.gjaku1031.templateforagent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Swagger/OpenAPI 설정
 * - git.properties 값을 읽어 문서에 커밋/브랜치 정보를 노출
 */
@Configuration
public class OpenApiConfig {

    private static final String TITLE = "TemplateForAgent API";
    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KST_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss (z)", Locale.KOREA)
                    .withZone(ZONE_SEOUL);

    // 단순화를 위해 문자열은 있는 그대로 노출(null 허용)

    /**
     * git.commit.time(예: 2025-10-29T15:59:50+0900)을 한국 시간 문자열로 변환
     *
     * @param rawTime git.commit.time 원문(yyyy-MM-dd'T'HH:mm:ssZ)
     * @return Asia/Seoul 기준 "yyyy년 MM월 dd일 HH:mm:ss (z)", 실패 시 null
     */
    private static String formatCommitTime(String rawTime) {
        if (rawTime == null || rawTime.isBlank()) return null;
        try {
            DateTimeFormatter rawFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            OffsetDateTime odt = OffsetDateTime.parse(rawTime.trim(), rawFmt);
            return KST_FORMATTER.format(odt.toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * OpenAPI 문서 메타정보 구성
     *
     * @param gitProvider Spring Boot가 제공하는 GitProperties(Optional)
     * @return OpenAPI 빈(제목/버전/설명 포함)
     */
    @Bean
    public OpenAPI openAPI(ObjectProvider<GitProperties> gitProvider) {
        GitProperties git = gitProvider.getIfAvailable();
        String shortCommit = (git != null) ? git.getShortCommitId() : null;
        String version = (shortCommit != null && !shortCommit.isBlank()) ? "v1+" + shortCommit : "v1";
        return new OpenAPI().info(new Info().title(TITLE)
                .version(version)
                .description(buildDescription(git)));
    }

    /**
     * Swagger UI 그룹 및 스캔 범위 설정
     *
     * @return GroupedOpenApi 빈
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1")
                .packagesToScan("com.gjaku1031.templateforagent")
                .pathsToMatch("/api/**")
                .build();
    }

    /**
     * Swagger 설명 영역에 표시할 Markdown 문자열 생성
     *
     * @param git GitProperties(Optional). null이면 "unknown"으로 대체
     * @return Markdown 형식의 설명 문자열
     */
    private String buildDescription(GitProperties git) {
        String commitMessage = (git != null) ? git.get("commit.message.short") : null;
        String commitId = (git != null) ? git.getCommitId() : null;
        String commitUser = (git != null) ? git.get("commit.user.name") : null;
        String branch = (git != null) ? git.getBranch() : null;
        String commitTime = (git != null) ? formatCommitTime(git.get("commit.time")) : null;

        return """
                ## Last Commit Information

                #### Message
                - %s
                
                #### Commit ID
                - `%s`
                
                #### Commit Time
                - %s
                
                #### Branch
                - %s
                
                #### Committed by
                - %s
                """.formatted(commitMessage, commitId, commitTime, branch, commitUser);
    }
}
