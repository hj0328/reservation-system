package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.domain.product.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
	public ResponseEntity<ProductListResponse> getProduct(
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Long productId) {

		List<ProductResponse> products = productService.getPagedProductsByCategoryId(categoryId, productId);
		Long productTotalCount = productService.getProductCountByCategoryId(categoryId);

		ProductListResponse response = ProductListResponse.builder()
				.products(products)
				.totalProductCount(productTotalCount)
				.build();
		return ResponseEntity.ok(response);
	}

	/**
	 * 예매 인기 순위
	 * 현재 가장 많이 예매한 순위 상위 20개 조회
	 * categoryId가 주어지면 해당 카테고리에 대한 인기 순위 가져온다.
	 */
	@GetMapping("/popular-products/db")
	public List<PopularProductResponse> getRealTimePopularProduct(
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Integer startPage
	) {
		return productService.getRealTimePopularProduct(startPage, categoryId);
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
	public ResponseEntity<ProductSeatScheduleListResponse> getProductSeatSchedule(
			@RequestParam Long productId,
			@RequestParam(required = false, defaultValue = "0") Long productSeatScheduleId) {

		List<ProductSeatScheduleResponse> scheduleListList = productService.getProductSeatScheduleList(productId, productSeatScheduleId);
		ProductSeatScheduleListResponse resp = ProductSeatScheduleListResponse.builder()
				.productId(productId)
				.productSeatScheduleList(scheduleListList)
				.build();
		return ResponseEntity.ok(resp);
	}

	/**
	 * product 상세 조회(좌석, 가격 종류, 스케줄, 장소)
	 */
	@GetMapping("/detail-info")
	public ResponseEntity<ProductDetailResponse> getProductDetailInfo(@RequestParam Long productId) {
		return ResponseEntity.ok(
				productService.getProductDetailInfo(productId)
		);
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
	 * categoryId가 주어지면 해당하는 모든 product를 최대 20개까지 검색
	 */
	@GetMapping("/search")
	public ResponseEntity<List<ProductResponse>> searchProducts (
			@RequestParam String title,
			@RequestParam(required = false, defaultValue = "0") Long categoryId,
			@RequestParam(required = false, defaultValue = "0") Long productId) {

		return ResponseEntity.ok(
				productService.searchProductByTitle(title, categoryId, productId)
		);
	}

	/**
	 * product 수정
	 * category, product
	 */
	@PutMapping("/{productId}")
	public ResponseEntity<ProductResponse> updateProduct(
			@PathVariable Long productId,
			@RequestBody ProductRequest productRequest
	) {

		return ResponseEntity.ok(
				productService.updateProduct(productId, productRequest)
		);
	}

}
