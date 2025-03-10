package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.category.CategoryRepository;
import kr.or.connect.reservation.domain.product.dao.ProductRepository;
import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import kr.or.connect.reservation.domain.product.dto.PopularProductResponse;
import kr.or.connect.reservation.domain.product.entity.Category;
import kr.or.connect.reservation.domain.product.entity.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"classpath:data/data.sql"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)    // h2에 추가한 sql 초기화
public class PopularProductScriptTest {

    @Autowired
    private ProductSeatScheduleRepository productSeatScheduleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisPopularProduct redisPopularProduct;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setup() {
        // redis에 테스트 데이터 cache
        redisPopularProduct.initialize();
    }

    @Test
    public void 레디스_상품_조회() {
        // when
        List<PopularProductDto> popularProductDtos = productSeatScheduleRepository
                .findAllPopularProductRedis();

        List<InMemoryProductDto> productDtos = redisPopularProduct.getProductDtos();

        // then
        assertThat(productDtos.size()).isEqualTo(popularProductDtos.size());
    }

    @Test
    public void 레디스_동일제품_등록_방지() {
        // given
        InMemoryProductDto saveProduct = new InMemoryProductDto(1000000L, "상품1", "설명1", LocalDate.of(2023, 1, 1), 120, 10, CategoryType.CLASSIC.name());
        redisPopularProduct.register(saveProduct);

        RSortedSet<InMemoryProductDto> sortedSet = redissonClient.getSortedSet("popularProducts");
        int originalSize = sortedSet.size();

        // when
        InMemoryProductDto duplicateProduct = new InMemoryProductDto(1000000L, "상품1", "설명1", LocalDate.of(2023, 1, 1), 120, 10, CategoryType.CLASSIC.name());
        redisPopularProduct.register(duplicateProduct);

        // then
        sortedSet = redissonClient.getSortedSet("popularProducts");
        int duplicateSize = sortedSet.size();

        assertThat(duplicateSize).isEqualTo(originalSize);
    }

    @Test
    public void 레디스_제품_예약_반영() {
        // given
        InMemoryProductDto productDto = redisPopularProduct.getProductDtos().stream()
                .findFirst().get();
        int originalReservationCount = productDto.getTotalReservedCount();

        // when: 새로운 상품 예약 호출
        int addReservedCount = 5;
        redisPopularProduct.reserve(productDto.getProductId(), addReservedCount);

        // then
        InMemoryProductDto resultProductDto = redisPopularProduct.getProductDtos().stream()
                .findFirst().get();

        assertThat(resultProductDto.getTotalReservedCount())
                .isEqualTo(originalReservationCount + addReservedCount);
    }

    @Test
    public void 레디스_제품_예약_취소() {
        // given
        InMemoryProductDto productDto = redisPopularProduct.getProductDtos().stream()
                .findFirst().get();
        int originalReservationCount = productDto.getTotalReservedCount();

        // when: 상품 예약 1건 취소 호출
        int cancelReservedCount = 1;
        redisPopularProduct.cancel(productDto.getProductId(), cancelReservedCount);

        // then
        InMemoryProductDto resultProductDto = redisPopularProduct.getProductDtos().stream()
                .findFirst().get();

        assertThat(resultProductDto.getTotalReservedCount())
                .isEqualTo(originalReservationCount - cancelReservedCount);
    }

    @Test
    public void DB_특정_카테고리의_인기제품_조회() {
        // given
        List<Category> categories = categoryRepository.findAll();
        Category anyCategory = categories.get(0);

        // when
        List<PopularProductResponse> popularProducts = productService.getRealTimePopularProduct(0, anyCategory.getId());

        // then
        assertThat(popularProducts).allSatisfy(p -> {
            assertThat(p.getCategory()).isEqualTo(anyCategory.getName().name());
        });
    }

//    @Test
//    public void 인기제품_조회() throws Exception {
//        // given
//
//        // when
//        List<PopularProductResponse> popularProducts = productService.getRealTimePopularProduct(0);
//
//        // then
//        for (PopularProductResponse popularProduct : popularProducts) {
//            System.out.println(popularProduct);
//        }
//    }
}
