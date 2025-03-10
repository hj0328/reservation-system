package kr.or.connect.reservation.domain;

import kr.or.connect.reservation.domain.config.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {RedisConfig.class})
public class ApplicationTest {
    @DisplayName("SpringBoot 정상 기동 검사")
    @Test
    public void contextLoads() {
    }
}
