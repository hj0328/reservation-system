package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dao.CategoryRepository;
import kr.or.connect.reservation.domain.product.dao.ProductPriceRepository;
import kr.or.connect.reservation.domain.product.dao.ProductRepository;
import kr.or.connect.reservation.domain.product.dto.ProductPriceRequest;
import kr.or.connect.reservation.domain.product.dto.ProductRegisterRequest;
import kr.or.connect.reservation.domain.product.dto.ProductResponse;
import kr.or.connect.reservation.domain.product.entity.Category;
import kr.or.connect.reservation.domain.product.entity.ProductPrice;
import kr.or.connect.reservation.domain.product.entity.SeatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ProductServiceIntegratedTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductPriceRepository productPriceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 카테고리 생성 및 저장
        Category category = Category.createCategory("MUSICAL");
        categoryRepository.save(category);
    }

    @Test
    public void 새로운_예약_제품_등록() throws Exception {
        // given
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

}
