package kr.or.connect.reservation.domain.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

/**
 *  Test 전용 RedisConfig
 *  운영환경의 Redis를 대체한다. (오버라이딩)
 */
@TestConfiguration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;
    private final String REDISSON_HOST_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDISSON_HOST_PREFIX + host + ":" + port);

        return Redisson.create(config);
    }

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
//        log.info("Embedded Redis Server start. isArmMac={}",isArmMac());

        if (isArmMac()) {
//            log.info("ArmMac");
            redisServer = new RedisServer(getRedisFileForArcMac(), port);
        } else {
            redisServer = RedisServer.builder()
                    .port(port)
                    .build();
        }

        try {
            redisServer.start();
        } catch (Exception e) {
        }
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
//        log.info("osArch={}, osName={}", osArch,osName );
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

