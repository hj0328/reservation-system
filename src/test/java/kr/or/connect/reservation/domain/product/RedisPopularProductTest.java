package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"classpath:data/data.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)    // h2에 추가한 sql 초기화
public class RedisPopularProductTest {

    private static final Logger log = LoggerFactory.getLogger(RedisPopularProductTest.class);
    @Autowired
    private ProductSeatScheduleRepository productSeatScheduleRepository;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisPopularProduct redisPopularProduct;

    @BeforeEach
    public void setup() {
        // redis에 테스트 데이터 cache
        redisPopularProduct.initialize();
    }

    @Test
    public void 레디스_상품_조회() {
        // when
        List<PopularProductDto> popularProductDtos = productSeatScheduleRepository
                .findAllPopularProductByReservation();

        List<InMemoryProductDto> productDtos = redisPopularProduct.getProductDtos();

        // then
        assertThat(productDtos.size()).isEqualTo(popularProductDtos.size());
    }

    @Test
    public void 동일제품_등록_방지() {
        // given
        InMemoryProductDto saveProduct = new InMemoryProductDto(1000000L, "상품1", "설명1", LocalDate.of(2023, 1, 1), 120, 10);
        redisPopularProduct.register(saveProduct);

        RSortedSet<InMemoryProductDto> sortedSet = redissonClient.getSortedSet("popularProducts");
        int originalSize = sortedSet.size();

        // when
        InMemoryProductDto duplicateProduct = new InMemoryProductDto(1000000L, "상품1", "설명1", LocalDate.of(2023, 1, 1), 120, 10);
        redisPopularProduct.register(duplicateProduct);

        // then
        sortedSet = redissonClient.getSortedSet("popularProducts");
        int duplicateSize = sortedSet.size();

        assertThat(duplicateSize).isEqualTo(originalSize);
    }

    @Test
    public void 제품_예약_레디스_반영() {
        // given
        InMemoryProductDto productDto = redisPopularProduct.getProductDtos().stream()
                .filter(v -> v.getProductId().equals(1L))
                .findFirst().get();
        int originalReservationCount = productDto.getTotalReservedCount();

        // when: 새로운 상품 예약 호출
        int addReservedCount = 5;
        redisPopularProduct.reserve(productDto.getProductId(), addReservedCount);

        // then
        InMemoryProductDto resultProductDto = redisPopularProduct.getProductDtos().stream()
                .filter(v -> v.getProductId().equals(1L))
                .findFirst().get();

        assertThat(resultProductDto.getTotalReservedCount())
                .isEqualTo(originalReservationCount + addReservedCount);
    }

    @Test
    public void 제품_예약_취소_레디스반영() {
        // given
        InMemoryProductDto productDto = redisPopularProduct.getProductDtos().stream()
                .filter(v -> v.getProductId().equals(1L))
                .findFirst().get();
        int originalReservationCount = productDto.getTotalReservedCount();

        // when: 상품 예약 1건 취소 호출
        int cancelReservedCount = 1;
        redisPopularProduct.cancel(productDto.getProductId(), cancelReservedCount);

        // then
        InMemoryProductDto resultProductDto = redisPopularProduct.getProductDtos().stream()
                .filter(v -> v.getProductId().equals(1L))
                .findFirst().get();

        assertThat(resultProductDto.getTotalReservedCount())
                .isEqualTo(originalReservationCount - cancelReservedCount);
    }
}
