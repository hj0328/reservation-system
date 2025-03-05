package kr.or.connect.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class ApplicationTest {
    @DisplayName("SpringBoot 정상 기동 검사")
    @Test
    public void contextLoads() {
    }
}
