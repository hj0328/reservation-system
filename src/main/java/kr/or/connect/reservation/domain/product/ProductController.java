package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dao.dto.ProductProfitDto;
import kr.or.connect.reservation.domain.product.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	/**
	 * categoryId 상품의 start 기준 상품목록과 categoryId의 상품 수를 조회
	 * 만약, categoryId가 0이라면 전체 상품을 대상으로 조회한다.
	 * <p>
	 * 조회 대상은 가장 최근 출시된 상품순서대로 정렬
	 *
	 * @param categoryId 카테고리 아이디
	 * @param productId      조회 시작 위치
	 */
	@GetMapping
	public ProductListResponse getProduct(
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Long productId) {

		List<ProductResponse> products = productService.getPagedProductsByCategoryId(categoryId, productId);
		Long productTotalCount = productService.getProductCountByCategoryId(categoryId);

		ProductListResponse response = ProductListResponse.builder()
				.products(products)
				.totalProductCount(productTotalCount)
				.build();
		return response;
	}

	/**
	 * 예매 인기 순위
	 * 현재 가장 많이 예매한 순위 상위 20개 조회
	 * categoryId가 주어지면 해당 카테고리에 대한 인기 순위 가져온다.
	 */
	@GetMapping("/popular-products/db")
	public List<PopularProductResponse> getRealTimePopularProduct(
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Integer lastProductId,
			@RequestParam(required = false) Integer lastCount
	) {
		if (lastCount == null) {
			lastCount = Integer.MAX_VALUE;
		}
		return productService.getRealTimePopularProduct(lastProductId, categoryId, lastCount);
	}

	/**
	 * 매출 높은 product 조회
	 * categoryId가 주어지면 해당 카테고리에 대한 인기 순위 가져온다.
	 */
	@GetMapping("/high-profit-products/db")
	public List<ProductProfitDto> getRealTimeHighProfitProduct(
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Integer startPage
	) {
		return productService.getRealTimeHighProfitProducts(startPage, categoryId);
	}

	/**
	 * local cache 예매 인기 순위
	 * 현재 가장 많이 예매한 순위 상위 20개 조회
	 * categoryId가 주어지면 해당 카테고리에 대한 인기 순위 가져온다.
	 */
	@GetMapping("/popular-products/local-cache")
	public List<PopularProductResponse> getRealTimePopularProductLocalCache (
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Integer startPage
	) {
		return productService.getRealTimePopularProductLocalCache(startPage, categoryId);
	}

	/**
	 * redis 예매 인기 순위
	 * 현재 가장 많이 예매한 순위 상위 20개 조회
	 * categoryId가 주어지면 해당 카테고리에 대한 인기 순위 가져온다.
	 */
	@GetMapping("/popular-products/redis")
	public List<PopularProductResponse> getRealTimePopularProductRedis(
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Integer startPage
	) {
		return productService.getRealTimePopularProductRedis(startPage, categoryId);
	}

	/**
	 * product schedule (시간, 남은 좌석) 정보 조회
	 */
	@GetMapping("/schedule")
	public ProductSeatScheduleListResponse getProductSeatSchedule(
			@RequestParam Long productId,
			@RequestParam(required = false, defaultValue = "0") Long productSeatScheduleId) {

		List<ProductSeatScheduleResponse> scheduleListList = productService.getProductSeatScheduleList(productId, productSeatScheduleId);
		return ProductSeatScheduleListResponse.builder()
				.productId(productId)
				.productSeatScheduleList(scheduleListList)
				.build();
	}

	/**
	 * product 상세 조회(좌석, 가격 종류, 스케줄, 장소)
	 */
	@GetMapping("/detail-info")
	public ProductDetailResponse getProductDetailInfo(@RequestParam Long productId) {
		return productService.getProductDetailInfo(productId);
	}

	/**
	 * product 등록
	 * product 정보, 좌석 등급별 금액
	 */
	@PostMapping("/new")
	public ResponseEntity<ProductResponse> registerProduct(
			@Valid @RequestBody ProductRegisterRequest request) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(productService.addNewProduct(request));
	}

	/**
	 * product schedule 등록
	 * 좌석, 시간표 정보를 저장
	 */
	@PostMapping("/{productId}/schedule")
	public ResponseEntity<ProductSeatScheduleResponse> registerProductQuantitySchedule(
			@Valid @RequestBody ProductSeatScheduleRequest request
	) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(productService.addProductSeatSchedule(request));
	}

	/**
	 * product schedule 수정
	 */
	@PutMapping("/{productId}/schedule/{productSeatScheduleId}")
	public ResponseEntity<ProductSeatScheduleResponse> updateProductSeatSchedule(
			@PathVariable Long productId,
			@PathVariable Long productSeatScheduleId,
			@Valid @RequestBody ProductSeatScheduleUpdateRequest request
	) {

		return ResponseEntity.ok(
				productService.updateProductSeatSchedule(productId, productSeatScheduleId, request)
		);
	}

	/**
	 * product 검색
	 * - startDate가 주어진다면, startDate 포함 이후에 개봉된 상품을 조회
	 * - startDate가 없다면, title 로 시작하는 상품을 조회
	 * categoryId가 주어지면 해당하는 모든 product를 최대 100개까지 검색
	 * productId를 cursor 로 사용
	 */
	@GetMapping("/search")
	public List<ProductResponse> searchProducts (
			@RequestParam(required = false) String title,
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Long productId,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate startDate) {

		List<ProductResponse> productResponses;
		if (startDate == null) {
			productResponses = productService.searchProductByTitle(title, categoryId, productId);
		} else {
			productResponses = productService.searchProductAfterDate(startDate, categoryId, productId);
		}

		return productResponses;
	}

	/**
	 * product 수정
	 * category, product
	 */
	@PutMapping("/{productId}")
	public ProductResponse updateProduct(
			@PathVariable Long productId,
			@RequestBody ProductRequest productRequest
	) {

		return productService.updateProduct(productId, productRequest);
	}
}
