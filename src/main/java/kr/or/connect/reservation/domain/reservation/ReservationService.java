package kr.or.connect.reservation.domain.reservation;

import kr.or.connect.reservation.config.exception.CustomException;
import kr.or.connect.reservation.config.exception.CustomExceptionStatus;
import kr.or.connect.reservation.domain.member.dao.MemberRepository;
import kr.or.connect.reservation.domain.member.entity.Member;
import kr.or.connect.reservation.domain.product.InMemoryPopularProduct;
import kr.or.connect.reservation.domain.product.InMemoryProductDto;
import kr.or.connect.reservation.domain.product.RedisPopularProduct;
import kr.or.connect.reservation.domain.product.dao.ProductRepository;
import kr.or.connect.reservation.domain.product.dao.ProductSeatScheduleRepository;
import kr.or.connect.reservation.domain.product.entity.Product;
import kr.or.connect.reservation.domain.product.entity.ProductSeatSchedule;
import kr.or.connect.reservation.domain.reservation.dao.ReservationPriceRepository;
import kr.or.connect.reservation.domain.reservation.dao.ReservationRepository;
import kr.or.connect.reservation.domain.reservation.dto.*;
import kr.or.connect.reservation.domain.reservation.entity.Reservation;
import kr.or.connect.reservation.domain.reservation.entity.ReservationPrice;
import kr.or.connect.reservation.domain.reservation.entity.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static kr.or.connect.reservation.utils.UtilConstant.RESERVATION_PAGE_SIZE;

@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)
@Slf4j
public class ReservationService {

//	private final ReservationDao reservationDao;
	private final ReservationRepository reservationRepository;
	private final ReservationPriceRepository reservationPriceRepository;

	private final ProductRepository productRepository;
	private final ProductSeatScheduleRepository productSeatScheduleRepository;
	private final MemberRepository memberRepository;

	private final InMemoryPopularProduct inMemoryPopularProduct;
	private final RedisPopularProduct redisPopularProduct;
	private final EntityManager em;

	private final RedissonClient redissonClient;

	private static final long DEDEUPE_TTL_SEC = 3;
	private static final long LOCK_WAIT_MS    = 1000;
	private static final long LOCK_LEASE_MS   = 3000;

	@Transactional(readOnly = false)
	public NewReservationResponse createReservation(NewReservationRequest request) throws InterruptedException {

		// 중복 요청 방지
		String key = request.getMemberId() + ":" + request.getProductId();
		boolean isFirst = redissonClient.getBucket(key)
				.setIfAbsent("1", Duration.ofSeconds(DEDEUPE_TTL_SEC));
		if (!isFirst) {
			throw new CustomException(CustomExceptionStatus.DUPLICATE_MEMBER_EMAIL);
		}

		List<ReservationPriceDto> list = request.getReservationPriceDtos();
		if (list == null || list.isEmpty()) {
			throw new CustomException(CustomExceptionStatus.INVALID_REQUEST_ERROR);
		}

		// 예약 상태 확인
		Long productId = request.getProductId();
		Long placeId   = list.get(0).getPlaceId();
		String seatType = list.get(0).getSeatType();
		Long memberId = request.getMemberId();

		String lockKey = "lock:productId:" + productId + ":placeId:" + placeId + "seatType:" + seatType;
		RLock lock = redissonClient.getLock(lockKey);

		boolean acquired;
		try {
			acquired = lock.tryLock(LOCK_WAIT_MS, LOCK_LEASE_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new CustomException(CustomExceptionStatus.RESERVATION_TOO_MANY_REQUESTS);
		}

		if (!acquired) {
			throw new CustomException(CustomExceptionStatus.RESERVATION_TOO_MANY_REQUESTS);
		}

		try {
			// 락 획득 처리
			Reservation reservation = makeReservation(request);

			List<Long> priceIds = new ArrayList<>();
			int total = 0;
			for (ReservationPriceDto dto : list) {
				ReservationPrice rp = makeReservationPrice(dto, reservation);
				calculateQuantity(dto, rp);
				priceIds.add(rp.getId());
				total += rp.getReservedQuantity();
			}

			saveInMemoryProduct(reservation, total);
			return NewReservationResponse.of(reservation.getId(), priceIds);

		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new CustomException(CustomExceptionStatus.DUPLICATE_RESERVATION);
		} finally {
			if (lock.isHeldByCurrentThread()) lock.unlock();
			// dedupeKey는 TTL로 자연 만료 (성공 직후 재시도 허용하고 싶으면 여기서 delete 가능)
		}
	}

	private Reservation makeReservation(NewReservationRequest request) {
		Reservation reservation = Reservation.create(ReservationStatus.RESERVED, request.getReservedDate());

		Member member = memberRepository.findById(request.getMemberId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.MEMBER_NOT_FOUND));

		Product product = productRepository.findById(request.getProductId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PRODUCT_NOT_FOUND));

		reservation.setReservationInfo(member, product);
		return reservationRepository.save(reservation);
	}
	/*
		reservation seat 정보
		producct schedule에 저장
			저장 가능 유무 확인
		product schedule를 reservation price에 연관 관계
		reservation price를 reservation에 연관관계
	 */

	private ReservationPrice makeReservationPrice(ReservationPriceDto reservationPriceDto,
												  Reservation reservation) {
		ReservationPrice reservationPrice = ReservationPrice.create(reservationPriceDto.getQuantity(), reservationPriceDto.getPrice(), reservationPriceDto.getSeatType());
		reservationPrice.register(reservation);
		return reservationPriceRepository.save(reservationPrice);
	}

	private void calculateQuantity(ReservationPriceDto reservationPriceDto,
													  ReservationPrice reservationPrice) {

		ProductSeatSchedule productSeatSchedule = productSeatScheduleRepository
				.findById(reservationPriceDto.getProductSeatScheduleId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.PRODUCT_SCHEDULE_NOT_FOUND));

		reservationPrice.schedule(productSeatSchedule);

		Integer seatQuantity = productSeatSchedule.getPlace().getSeatQuantity();
		if (seatQuantity < reservationPriceDto.getQuantity() + productSeatSchedule.getReservedQuantity()) {
			throw new CustomException(CustomExceptionStatus.NO_SEAT_AVAILABLE);
		}

		productSeatSchedule.addQuantity(reservationPriceDto.getQuantity());
	}

	@Transactional
	public ReservationCancelResponse cancelReservation(ReservationCancelRequest request) {
		Reservation reservation = reservationRepository.findById(request.getReservationId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.RESERVATION_NOT_FOUND));

		Long memberId = reservation.getMember().getId();
		if (!memberId.equals(request.getMemberId())) {
			throw new CustomException(CustomExceptionStatus.INVALID_REQUEST_ERROR);
		}

		reservation.cancel();

		List<ReservationPrice> reservationPrices = reservationPriceRepository.findAllByReservationId(reservation.getId());
		Integer totalReservedQuantity = 0;
		for (ReservationPrice reservationPrice : reservationPrices) {
			Integer reservedQuantity = reservationPrice.getReservedQuantity();

			totalReservedQuantity += reservedQuantity;
			ProductSeatSchedule productSeatSchedule = reservationPrice.getProductSeatSchedule();
			productSeatSchedule.minusQuantity(reservedQuantity);
		}

		cancelInMemoryProduct(reservation, totalReservedQuantity);

		return ReservationCancelResponse
				.of(memberId, reservation.getId(), reservation.getReservationStatus());
	}

	public List<ReservationDetail> getReservation(Long memberId, Integer start) {
		PageRequest pageRequest = PageRequest.of(start, RESERVATION_PAGE_SIZE);
		List<ReservationDetail> reservations = reservationRepository.findMemberReservation(memberId, pageRequest);
		return reservations;
	}

	public ReservationWatchedResponse setReservationWatched(ReservationWatchedRequest request) {
		Reservation reservation = reservationRepository.findById(request.getReservationId())
				.orElseThrow(() -> new CustomException(CustomExceptionStatus.RESERVATION_NOT_FOUND));

		Long memberId = reservation.getMember().getId();
		if (!memberId.equals(request.getMemberId())) {
			throw new CustomException(CustomExceptionStatus.INVALID_REQUEST_ERROR);
		}

		reservation.watch();
		return ReservationWatchedResponse
				.of(memberId, reservation.getId(), reservation.getReservationStatus());
	}

	private void saveInMemoryProduct(Reservation reservation, Integer totalReservedQuantity) {
		Product product = reservation.getProduct();
		InMemoryProductDto saveProductDto = InMemoryProductDto.builder()
				.productId(product.getId())
				.title(product.getTitle())
				.runningTime(product.getRunningTime())
				.description(product.getDescription())
				.releaseDate(product.getReleaseDate())
				.categoryName(product.getCategory().getName().name())
				.totalReservedCount(totalReservedQuantity).build();
		inMemoryPopularProduct.reserve(saveProductDto);
//		inMemoryPopularProduct.reserve(saveProductDto);
	}

	private void cancelInMemoryProduct(Reservation reservation, Integer totalReservedQuantity) {
		Product product = reservation.getProduct();
		InMemoryProductDto cancelProductDto = InMemoryProductDto.builder()
				.productId(product.getId())
				.title(product.getTitle())
				.runningTime(product.getRunningTime())
				.description(product.getDescription())
				.releaseDate(product.getReleaseDate())
				.categoryName(product.getCategory().getName().name())
				.totalReservedCount(totalReservedQuantity).build();
		inMemoryPopularProduct.cancel(cancelProductDto);
		// inMemoryPopularProduct.cancel(cancelProductDto);
	}
}
