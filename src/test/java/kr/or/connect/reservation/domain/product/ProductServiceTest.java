package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.category.CategoryRepository;
import kr.or.connect.reservation.domain.product.dao.PlaceRepository;
import kr.or.connect.reservation.domain.product.dao.ProductRepository;
import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.dto.ProductDetailResponse;
import kr.or.connect.reservation.domain.product.dto.ProductResponse;
import kr.or.connect.reservation.domain.product.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static kr.or.connect.reservation.utils.UtilConstant.PRODUCT_PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductSeatScheduleRepository productSeatScheduleRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 모든_카테고리의_상품_조회() {
        // given
        List<Product> products = new ArrayList<>();

        Product product1 = Product.create("t1", "desc", LocalDate.now(), 120);
        product1.registerCategory(Category.createCategory(CategoryType.CLASSIC));
        products.add(product1);

        Product product2 = Product.create("t1", "desc", LocalDate.now(), 120);
        product2.registerCategory(Category.createCategory(CategoryType.MOVIE));
        products.add(product2);

        Product product3 = Product.create("t1", "desc", LocalDate.now(), 120);
        product3.registerCategory(Category.createCategory(CategoryType.MUSICAL));
        products.add(product3);

        PageRequest pageRequest = PageRequest.of(0, PRODUCT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "releaseDate"));
        Page<Product> productPage = new PageImpl<>(products, pageRequest, products.size());
        when(productRepository.findAll(pageRequest))
                .thenReturn(productPage);

        // when
        List<ProductResponse> savedProducts = productService.getPagedProductsByCategoryId(0L, 0);

        // then
        List<String> list = savedProducts.stream()
                .map(ProductResponse::getCategory)
                .collect(Collectors.toList());
        assertThat(list).containsExactly(
                CategoryType.CLASSIC.name(),
                CategoryType.MOVIE.name(),
                CategoryType.MUSICAL.name());
    }

    @Test
    void 특정_카테고리의_상품_조회() {
        // given
        List<Product> products = new ArrayList<>();

        Product product1 = Product.create("t1", "desc", LocalDate.now(), 120);
        product1.registerCategory(Category.createCategory(CategoryType.CLASSIC));
        products.add(product1);

        Product product2 = Product.create("t1", "desc", LocalDate.now(), 120);
        product2.registerCategory(Category.createCategory(CategoryType.CLASSIC));
        products.add(product2);

        Product product3 = Product.create("t1", "desc", LocalDate.now(), 120);
        product3.registerCategory(Category.createCategory(CategoryType.CLASSIC));
        products.add(product3);

        PageRequest pageRequest = PageRequest.of(0, PRODUCT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "releaseDate"));
        Page<Product> productPage = new PageImpl<>(products, pageRequest, products.size());
        when(productRepository.findAllByCategoryId(1L, pageRequest))
                .thenReturn(productPage);

        // when
        List<ProductResponse> savedProducts = productService.getPagedProductsByCategoryId(1L, 0);

        // then
        List<String> list = savedProducts.stream()
                .map(ProductResponse::getCategory)
                .collect(Collectors.toList());
        assertThat(list).containsOnly(CategoryType.CLASSIC.name());
    }

    @Test
    void 상품_상세_조회() {
        // given
        Product product = Product.create("T1", "desc", LocalDate.now(), 120);

        product.registerCategory(Category.createCategory(CategoryType.CLASSIC));
        List<ProductPrice> productPriceList = new ArrayList<>();
        productPriceList.add(
                ProductPrice.create(null, 1000, SeatType.ALL_SAME_SEAT));
        productPriceList.add(
                ProductPrice.create(null, 2000, SeatType.VIP));

        product.registerPrices(productPriceList);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductDetailResponse productDetailInfo = productService.getProductDetailInfo(1L);

        assertThat(productDetailInfo.getPriceList())
                .allSatisfy(productPrice -> {
                    assertThat(productPrice.getPrice()).isIn(1000, 2000);
                    assertThat(productPrice.getSeatType()).isIn(SeatType.ALL_SAME_SEAT, SeatType.VIP);
                });
    }


}