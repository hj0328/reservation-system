package kr.or.connect.reservation.domain.reservation;

import kr.or.connect.reservation.domain.reservation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(path = "/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	/**
	 * 예약정보조회
	 */
	@GetMapping("/my-reservations")
	public ResponseEntity<List<MyReservationResponse>> getReservations(
			HttpServletRequest request,
			@RequestParam(required = true) Long memberId,
			@RequestParam(required = false, defaultValue = "0") Integer start) {
//		HttpSession session = request.getSession(false);
//		if (Objects.isNull(session)) {
//			throw new CustomException(CustomExceptionStatus.NO_SESSION_EXIST);
//		}

//		Long memberId = (Long) session.getAttribute(MEMBER_ID);
		return ResponseEntity.ok(reservationService.getReservation(memberId, start));
	}

	/**
	 * 예약하기
	 */
	@PostMapping
	public ResponseEntity<NewReservationResponse> createReservation (
			@RequestBody NewReservationRequest request) {
		NewReservationResponse reservation = reservationService.createReservation(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(reservation);
	}

	/**
	 * 예약취소
	 * 예약 리소스는 바로 삭제되지 취소 표시만 남긴 후 향후 삭제
	 */
	@PutMapping("/cancellation")
	public ResponseEntity<ReservationCancelResponse> setReservationCancel(
			@RequestBody ReservationCancelRequest request) {

		return ResponseEntity.ok(reservationService.cancelReservation(request));
	}

	/**
	 * 제품 시청 완료
	 */
	@PutMapping("/complete")
	public ResponseEntity<ReservationWatchedResponse> setReservationWatched(
			@RequestBody ReservationWatchedRequest request) {

		return ResponseEntity.ok(reservationService.setReservationWatched(request));
	}
}
