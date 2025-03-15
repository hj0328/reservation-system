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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static kr.or.connect.reservation.utils.UtilConstant.RESERVATION_PAGE_SIZE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

	@Transactional
	public NewReservationResponse createReservation(NewReservationRequest request) {
		Reservation reservation = makeReservation(request);

		List<ReservationPriceDto> reservationPriceDtos = request.getReservationPriceDtos();
		List<Long> priceIdsResponse = new ArrayList<>();
		Integer totalReservedQuantity = 0;
		for (ReservationPriceDto reservationPriceDto : reservationPriceDtos) {
			ReservationPrice reservationPrice = makeReservationPrice(reservationPriceDto, reservation);

			calculateQuantity(reservationPriceDto, reservationPrice);

			priceIdsResponse.add(reservationPrice.getId());
			totalReservedQuantity += reservationPrice.getReservedQuantity();
		}

		saveInMemoryProduct(reservation, totalReservedQuantity);
//		em.flush();
//		em.clear();
		return NewReservationResponse.of(reservation.getId(), priceIdsResponse);
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

	public List<MyReservationResponse> getReservation(Long memberId, Integer start) {
		PageRequest pageRequest = PageRequest.of(start, RESERVATION_PAGE_SIZE
				, Sort.by(Sort.Direction.DESC, "reservedDate"));
		List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId, pageRequest);

		List<MyReservationResponse> response = new ArrayList<>();
		for (Reservation reservation : reservations) {
			Product product = reservation.getProduct();
			List<ReservationPrice> reservationPriceList = reservation.getReservationPrice();

			response.add(MyReservationResponse.of(product, reservation, reservationPriceList));
		}

		return response;
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
