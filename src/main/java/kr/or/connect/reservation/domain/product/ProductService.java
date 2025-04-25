package kr.or.connect.reservation.domain.product;

import kr.or.connect.reservation.config.exception.CustomException;
import kr.or.connect.reservation.config.exception.CustomExceptionStatus;
import kr.or.connect.reservation.domain.category.CategoryRepository;
import kr.or.connect.reservation.domain.product.dao.*;
import kr.or.connect.reservation.domain.product.dao.dto.PopularProductDto;
import kr.or.connect.reservation.domain.product.dao.dto.ProductProfitDto;
import kr.or.connect.reservation.domain.product.dto.*;
import kr.or.connect.reservation.domain.product.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static kr.or.connect.reservation.utils.UtilConstant.ALL_PRODUCTS;
import static kr.or.connect.reservation.utils.UtilConstant.PRODUCT_PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	//	private final CommentService commentService;
	private final ProductDao productDao;
	private final ProductRepository productRepository;
	private final ProductSeatScheduleRepository productSeatScheduleRepository;
	private final ProductPriceRepository productPriceRepository;
	private final PlaceRepository placeRepository;
	private final CategoryRepository categoryRepository;

	private final InMemoryPopularProduct inMemoryPopularProduct;
	private final RedisPopularProduct redisPopularProduct;

	private final Long ALL_CATEGORY = 0L;

	public List<ProductResponse> getPagedProductsByCategoryId(Long categoryId, Long productId) {

		List<Product> products;
//		PageRequest pageRequest = PageRequest.of(0, PRODUCT_PAGE_SIZE);
		PageRequest pageRequest = PageRequest.of((int) (productId * PRODUCT_PAGE_SIZE), PRODUCT_PAGE_SIZE);
		if (ALL_PRODUCTS.equals(categoryId)) {
//			products = productRepository.findAll(pageRequest).getContent();
//			products = productRepository.findAllProducts(productId, pageRequest);
			products = productRepository.findAllProductsTemp(productId, pageRequest);
		} else {
//			products = productRepository.findAllByCategoryId(productId, categoryId, pageRequest);
			products = productRepository.findAllByCategoryIdTemp(categoryId, pageRequest);
		}
		return products.stream()
				.map(ProductResponse::of)
				.sorted(Comparator.comparing(ProductResponse::getReleaseDate).reversed())
				.collect(Collectors.toList());
	}

	public Long getProductCountByCategoryId(Long categoryId) {
		if (ALL_PRODUCTS.equals(categoryId)) {
			return Long.valueOf(productRepository.count());
		}
		return productRepository.countByCategoryId(categoryId);
	}

	public ProductDetailResponse getProductDetailInfo(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PRODUCT_NOT_FOUND));

		List<ProductPriceResponse> priceDtoList = product.getProductPriceList().stream()
				.map(ProductPriceResponse::of)
				.collect(Collectors.toList());

		String category = product.getCategory().getName().toString();
		return ProductDetailResponse.of(category, product, priceDtoList);
	}

	public List<ProductSeatScheduleResponse> getProductSeatScheduleList(Long productId, Long productSeatScheduleId) {
		// cursor pagination을 사용하기 때문에 0부터 시작
//		Pageable pageRequest = PageRequest.of(0, PRODUCT_PAGE_SIZE);
//		List<ProductSeatScheduleDto> seatScheduleList = productSeatScheduleRepository
//				.findAllScheduleFromPssId(productId, productSeatScheduleId, pageRequest);

		Pageable pageRequest = PageRequest.of((int) (productId * PRODUCT_PAGE_SIZE), PRODUCT_PAGE_SIZE);
		List<ProductSeatScheduleDto> seatScheduleList = productSeatScheduleRepository
				.findAllScheduleFromPssIdTemp(productId, pageRequest);

		log.info("seatScheduleList size={}", seatScheduleList.size());
		log.info("seatScheduleList first' place name={}", seatScheduleList.get(0).getPlaceName());

		return seatScheduleList.stream()
				.map(ProductSeatScheduleResponse::of)
				.collect(Collectors.toList());
	}

	@Transactional
	public ProductResponse addNewProduct(ProductRegisterRequest request) {
		Category category = categoryRepository.getReferenceById(request.getCategoryId());

		Product product = Product.create(request.getTitle(), request.getDescription(), request.getReleaseDate(), request.getRunningTime());
		List<ProductPrice> priceList = request.getPriceList().stream()
				.map(v -> v.toProductPrice(product))
				.collect(Collectors.toList());
		product.registerPrices(priceList);
		product.registerCategory(category);

		// Productprice entity 저장
		for (ProductPrice productPrice : priceList) {
			productPriceRepository.save(productPrice);
		}

		// product entity 저장
		Product saveProduct = productRepository.save(product);
		return ProductResponse.of(saveProduct);
	}

	@Transactional
	public ProductSeatScheduleResponse addProductSeatSchedule(ProductSeatScheduleRequest request) {
		ProductSeatSchedule requestSchedule = ProductSeatSchedule.from(request);

		Product product = productRepository.findById(request.getProductId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PRODUCT_NOT_FOUND));
		Place place = placeRepository.findById(request.getPlaceId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PLACE_NOT_FOUND));

		requestSchedule.targetProduct(product);
		requestSchedule.registerPlace(place);
		ProductSeatSchedule saveProductSeatSchedule = productSeatScheduleRepository.save(requestSchedule);

		return ProductSeatScheduleResponse.of(saveProductSeatSchedule, place);
	}

	public List<ProductResponse> searchProductByTitle(String title, Long categoryId, Long productId) {


		List<Product> foundProductList;
		if (ALL_CATEGORY.equals(categoryId)) {
			foundProductList = productRepository
					.findByTitleStartsWithTemp(title, (int) (productId * PRODUCT_PAGE_SIZE), PRODUCT_PAGE_SIZE);
		} else {
			foundProductList = productRepository
					.findByTitleStartingWithAndCategoryIdTemp(title, categoryId, (int) (productId * PRODUCT_PAGE_SIZE), PRODUCT_PAGE_SIZE);
		}

		return foundProductList.stream()
				.sorted(Comparator.comparing(Product::getId))
				.map(ProductResponse::of)
				.collect(Collectors.toList());
	}

	@Transactional
	public ProductResponse updateProduct(Long productId, ProductRequest productRequest) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PRODUCT_NOT_FOUND));
		product.updateProduct(productRequest);

		product.getCategory().updateCategory(productRequest.getCategory());
		return ProductResponse.of(product);
	}

	@Transactional
	public ProductSeatScheduleResponse updateProductSeatSchedule(Long productId, Long productSeatScheduleId, ProductSeatScheduleUpdateRequest request) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PRODUCT_NOT_FOUND));

		ProductSeatSchedule seatSchedule = productSeatScheduleRepository.findById(productSeatScheduleId)
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PRODUCT_SCHEDULE_NOT_FOUND));

		Long expectProductId = seatSchedule.getProduct().getId();
		if (!expectProductId.equals(product.getId())) {
			throw new CustomException(CustomExceptionStatus.INVALID_REQUEST_ERROR);
		}

		Place place = placeRepository.findById(request.getPlaceId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PLACE_NOT_FOUND));

		seatSchedule.update(request.getEventDateTime(), request.getReservedQuantity(), place);

		return ProductSeatScheduleResponse.of(seatSchedule, place);
	}

	/**
	 * DB 에서 인기 데이터 조회
	 *
	 * @param startPage
	 * @param categoryId
	 * @return
	 */
	public List<PopularProductResponse> getRealTimePopularProduct(Integer startPage, Long categoryId) {
		PageRequest pageRequest = PageRequest.of(startPage, PRODUCT_PAGE_SIZE);
		List<PopularProductDto> result;
		if (ALL_CATEGORY.equals(categoryId)) {
			result = productSeatScheduleRepository
					.findPagedPopularProduct(pageRequest);
		} else {
			result = productSeatScheduleRepository
					.findPopularProductByCategory(pageRequest, categoryId);
		}

		List<PopularProductDto> popularProductDtos = new ArrayList<>();
		if (result != null) {
			popularProductDtos = result;
		}

		return popularProductDtos.stream()
				.map(PopularProductResponse::of)
				.collect(Collectors.toList());
	}

	/**
	 * Redis 에서 인기 데이터 조회
	 *
	 * @param startPage
	 * @param categoryId
	 * @return
	 */
	public List<PopularProductResponse> getRealTimePopularProductRedis(Integer startPage, Long categoryId) {
		// Redis는 기동 시 warm-up 된 상태에서 데이터를 가져옴
		List<InMemoryProductDto> productDtos = redisPopularProduct.getProductDtos();

		// 특정 카테고리 필터링 (ALL_CATEGORY와 다르면 필터 적용)
		if (!ALL_CATEGORY.equals(categoryId)) {
			Category category = categoryRepository.findById(categoryId)
					.orElseThrow(() -> new CustomException(CustomExceptionStatus.CATEGORY_NOT_FOUND));
			productDtos = productDtos.stream()
					.filter(dto -> dto.getCategoryName().equals(category.getName().name()))
					.collect(Collectors.toList());
		}

		// 전체 리스트를 정렬: 여기서는 productId를 내림차순 정렬 (원하는 정렬 기준에 맞게 수정 가능)
		List<InMemoryProductDto> sortedList = productDtos.stream()
				.sorted(Comparator.comparing(InMemoryProductDto::getTotalReservedCount).reversed())
				.collect(Collectors.toList());

		// Pagination: startPage를 기반으로 오프셋과 종료 인덱스 계산
		int offset = startPage * PRODUCT_PAGE_SIZE;
		if (offset < 0 || offset >= sortedList.size()) {
			return Collections.emptyList();
		}
		int toIndex = Math.min(offset + PRODUCT_PAGE_SIZE, sortedList.size());

		// 해당 범위의 데이터를 PopularProductResponse로 매핑하여 반환
		return sortedList.subList(offset, toIndex).stream()
				.map(PopularProductResponse::of)
				.collect(Collectors.toList());
	}


	// local cache 에서 조회. 없다면 db 조회 후 캐싱
	public List<PopularProductResponse> getRealTimePopularProductLocalCache(Integer startPage, Long categoryId) {

		if (inMemoryPopularProduct.isEmpty()) {
			log.info("get popular product from DB");

			List<PopularProductDto> popularProducts = productSeatScheduleRepository.findAllPopularProducts();
			List<InMemoryProductDto> inMemoryProductDtos = popularProducts.stream()
					.map(v -> InMemoryProductDto.of(v))
					.sorted(Comparator.comparing(InMemoryProductDto::getTotalReservedCount).reversed())
					.collect(Collectors.toList());

			inMemoryPopularProduct.refresh(inMemoryProductDtos);

			int offset = startPage * PRODUCT_PAGE_SIZE;
			int limit = PRODUCT_PAGE_SIZE;
			int fromIndex = offset;
			int toIndex = offset + limit;
			int idsSize = inMemoryProductDtos.size();

			if (offset > idsSize) {
				return Collections.emptyList();
			}

			if (toIndex > idsSize) {
				toIndex = idsSize;
			}

			if (ALL_CATEGORY.equals(categoryId)) {
				return inMemoryProductDtos.subList(fromIndex, toIndex).stream()
						.map(PopularProductResponse::of)
						.collect(Collectors.toList());
			} else {
				Category category = categoryRepository.findById(categoryId)
						.orElseThrow(() -> new CustomException(CustomExceptionStatus.CATEGORY_NOT_FOUND));


				return inMemoryProductDtos.subList(fromIndex, toIndex).stream()
						.filter(p -> p.getCategoryName().equals(category.getName().name()))
						.map(PopularProductResponse::of)
						.collect(Collectors.toList());
			}
		}

		return inMemoryPopularProduct.getProducts(startPage, PRODUCT_PAGE_SIZE);
	}

	public List<ProductProfitDto> getRealTimeHighProfitProducts(Integer startPage, Long categoryId) {
		PageRequest pageRequest = PageRequest.of(startPage, PRODUCT_PAGE_SIZE);
		List<ProductProfitDto> result;
		if (ALL_CATEGORY.equals(categoryId)) {
			result = productSeatScheduleRepository
					.findPagedHighProfitProduct(pageRequest);
		} else {
			result = productSeatScheduleRepository
					.findHighProfitProductByCategory(pageRequest, categoryId);
		}

		List<ProductProfitDto> popularProductDtos = new ArrayList<>();
		if (result != null) {
			popularProductDtos = result;
		}

		return result;
	}

}
