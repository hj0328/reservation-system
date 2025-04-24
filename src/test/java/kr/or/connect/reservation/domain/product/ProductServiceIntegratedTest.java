package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.category.CategoryRepository;
import kr.or.connect.reservation.domain.config.RedisConfig;
import kr.or.connect.reservation.domain.product.dao.PlaceRepository;
import kr.or.connect.reservation.domain.product.dao.ProductPriceRepository;
import kr.or.connect.reservation.domain.product.dao.ProductRepository;
import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.dto.ProductPriceRequest;
import kr.or.connect.reservation.domain.product.dto.ProductRegisterRequest;
import kr.or.connect.reservation.domain.product.dto.ProductResponse;
import kr.or.connect.reservation.domain.product.dto.ProductSeatScheduleResponse;
import kr.or.connect.reservation.domain.product.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {RedisConfig.class})
@Sql(scripts = {"classpath:data/truncate.sql"})
public class ProductServiceIntegratedTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductPriceRepository productPriceRepository;
    @Autowired
    private ProductSeatScheduleRepository productSeatScheduleRepository;
    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void 새로운_제품_등록() throws Exception {
        // given
        categoryRepository.save(Category.createCategory(CategoryType.CLASSIC));

        List<ProductPriceRequest> productPriceList = new ArrayList<>();
        productPriceList.add(new ProductPriceRequest(1000, SeatType.ALL_SAME_SEAT));
        productPriceList.add(new ProductPriceRequest(2000, SeatType.S));

        ProductRegisterRequest registerRequest =
                new ProductRegisterRequest(1L, "title", "desc", LocalDate.now(), 120, productPriceList);

        // when
        ProductResponse productResponse = productService.addNewProduct(registerRequest);

        // then
        assertThat(productResponse.getTitle()).isEqualTo("title");

        List<ProductPrice> all = productPriceRepository.findAll();
        assertThat(all).allSatisfy(productPrice -> {
            assertThat(productPrice.getPrice()).isIn(1000, 2000);
            assertThat(productPrice.getSeatType()).isIn(SeatType.ALL_SAME_SEAT, SeatType.S);
        });
    }

    @Test
    void 제목으로_상품_조회() {
        // given
        Category category = categoryRepository.save(Category.createCategory(CategoryType.CLASSIC));
        Product product1 = Product.create("abcd", "desc", LocalDate.now(), 120);
        product1.registerCategory(category);

        Product product2 = Product.create("abcde", "desc", LocalDate.now(), 120);
        product2.registerCategory(category);

        Product product3 = Product.create("de", "desc", LocalDate.now(), 120);
        product3.registerCategory(category);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        // when
        List<ProductResponse> a = productService.searchProductByTitle("a", category.getId(), 0L);

        // then
        assertThat(a.size()).isEqualTo(2);
    }

    @Test
    void 제목으로_특정카테고리의_상품_조회() {
        // given
        Category category = categoryRepository.save(Category.createCategory(CategoryType.CLASSIC));
        Product product1 = Product.create("abcd", "desc", LocalDate.now(), 120);
        product1.registerCategory(category);

        Category category2 = categoryRepository.save(Category.createCategory(CategoryType.MUSICAL));
        Product product2 = Product.create("abcde", "desc", LocalDate.now(), 120);
        product2.registerCategory(category2);

        Product product3 = Product.create("de", "desc", LocalDate.now(), 120);
        product3.registerCategory(category2);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        // when
        List<ProductResponse> a = productService.searchProductByTitle("a", category.getId(), 0L);

        // then
        assertThat(a.size()).isEqualTo(1);
    }

    @Test
    public void 제품_좌석정보_조회() throws Exception {
        // given
        Category category = categoryRepository.save(Category.createCategory(CategoryType.CLASSIC));
        Product product1 = Product.create("abcd", "desc", LocalDate.now(), 120);
        product1.registerCategory(category);
        productRepository.save(product1);

        Place place1 = Place.create(SeatType.S, 100, "place1", "street", "0000");
        Place place2 = Place.create(SeatType.VIP, 100, "place2", "street", "1111");
        placeRepository.save(place1);
        placeRepository.save(place2);

        ProductSeatSchedule seatSchedule1 = ProductSeatSchedule.create(product1, place1, LocalDateTime.now(), 1, SeatType.S.name());
        ProductSeatSchedule seatSchedule2 = ProductSeatSchedule.create(product1, place2, LocalDateTime.now(), 1, SeatType.VIP.name());

        productSeatScheduleRepository.save(seatSchedule1);
        productSeatScheduleRepository.save(seatSchedule2);

        // when
        List<ProductSeatScheduleResponse> productSeatScheduleList = productService.getProductSeatScheduleList(1L, 0L);

        // then
        assertThat(productSeatScheduleList).allSatisfy(productPrice -> {
            assertThat(productPrice.getPlaceName()).isIn("place1", "place2");
            assertThat(productPrice.getSeatType()).isIn(SeatType.VIP.name(), SeatType.S.name());
        });
    }
}
