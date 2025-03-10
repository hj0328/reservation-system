package kr.or.connect.reservation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

/**
 * redis 올리기전 기동문제로 임시로 embedded redis
 */
@Slf4j
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        log.info("Embedded Redis Server start. isArmMac={}",isArmMac());

        if (isArmMac()) {
            log.info("ArmMac");
            redisServer = new RedisServer(getRedisFileForArcMac(), redisPort);
        } else {
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .build();
        }

        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    /**
     * 현재 시스템이 ARM 아키텍처를 사용하는 MAC인지 확인
     * System.getProperty("os.arch") : JVM이 실행되는 시스템 아키텍처 반환
     * System.getProperty("os.name") : 시스템 이름 반환
     */
    private boolean isArmMac() {
        String osArch = System.getProperty("os.arch").toLowerCase();
        String osName = System.getProperty("os.name").toLowerCase();
        log.info("osArch={}, osName={}", osArch,osName );
        return (osArch.equals("aarch64") || osArch.equals("arm64"))
                || osName.contains("mac");
    }

    /**
     * ARM 아키텍처를 사용하는 Mac에서 실행할 수 있는 Redis 바이너리 파일을 반환
     */
    private File getRedisFileForArcMac() {
        try {
            return new ClassPathResource("binary/redis/redis-server-7.4.2-mac-arm64").getFile();
        } catch (Exception e) {
            throw new RuntimeException("Redis File Error");
        }
    }
}
