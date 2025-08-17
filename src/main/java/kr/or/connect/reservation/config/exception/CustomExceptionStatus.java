package kr.or.connect.reservation.config.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@RequiredArgsConstructor
public enum CustomExceptionStatus {
    // 400 - Bad Request
    INVALID_REQUEST_ERROR(HttpStatus.BAD_REQUEST,            4001, "잘못된 요청입니다."),

    // 401 - Unauthorized
    MEMBER_LOGIN_FAIL(HttpStatus.UNAUTHORIZED,               4010, "아이디 또는 비밀번호를 확인해주세요."),
    NO_SESSION_EXIST(HttpStatus.UNAUTHORIZED,                4011, "로그인이 필요합니다."),

    // 404 - Not Found
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,                   4040, "사용자를 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND,              4041, "예약정보를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,                  4042, "상품정보를 찾을 수 없습니다."),
    PRODUCT_AVERAGE_SCORE_NOT_FOUND(HttpStatus.NOT_FOUND,    4043, "상품의 평점정보를 찾을 수 없습니다."),
    PRODUCT_DISPLAY_NOT_FOUND(HttpStatus.NOT_FOUND,          4044, "상품의 전시정보를 찾을 수 없습니다."),
    PRODUCT_DISPLAY_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND,    4045, "상품의 전시 이미지 정보를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,                  4046, "댓글을 찾을 수 없습니다."),
    COMMENT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND,            4047, "댓글 이미지를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND,                 4048, "카테고리를 찾을 수 없습니다."),
    PRODUCT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND,         4049, "상품 예약정보가 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND,                    4050, "장소 정보가 없습니다."),

    // 409 - Conflict
    DUPLICATE_MEMBER_EMAIL(HttpStatus.CONFLICT,              4090, "이미 가입된 이메일입니다."),
    DUPLICATE_RESERVATION(HttpStatus.CONFLICT,               4091, "중복된 요청 입니다."),
    DUPLICATE_PRODUCT_SCHEDULE(HttpStatus.CONFLICT,          4092, "중복된 상품 예약정보가 있습니다."),
    MULTIPLICITY_VIOLATION(HttpStatus.CONFLICT,              4093, "둘 이상의 값을 저장할 수 없습니다."),
    MULTIPLICITY_COMMENTS_VIOLATION(HttpStatus.CONFLICT,     4094, "둘 이상의 메시지를 등록할 수 없습니다."),
    NO_SEAT_AVAILABLE(HttpStatus.CONFLICT,                   4095, "사용 가능한 좌석이 없습니다."),

    // 429 - Too Many Requests
    RESERVATION_TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, 4290, "잠시 후 다시 시도하세요.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
