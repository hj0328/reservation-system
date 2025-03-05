package kr.or.connect.reservation.domain.member;

import kr.or.connect.reservation.config.PasswordEncoder;
import kr.or.connect.reservation.config.exception.CustomException;
import kr.or.connect.reservation.domain.member.dao.MemberRepository;
import kr.or.connect.reservation.domain.member.dto.MemberRequest;
import kr.or.connect.reservation.domain.member.dto.MemberResponse;
import kr.or.connect.reservation.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder pwEncoder;

    @InjectMocks
    private MemberService userService;

    @Test
    void 로그인_성공_테스트() {
        // when
        Member testUser = getTestUser();
        when(memberRepository.findMemberByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        when(pwEncoder.matches("test1", "test1"))
                .thenReturn(true);

        // given
        MemberResponse loginUser = userService.login("test@gmail.com", "test1");

        // then
        assertThat(loginUser.getName()).isEqualTo(testUser.getName());
    }

    @Test
    void 로그인_비밀번호_실패_테스트() {
        // when
        Member testUser = getTestUser();
        when(memberRepository.findMemberByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        // then
        assertThrows(CustomException.class,
                () -> userService.login("test@gmail.com", "wrongPassword") );
    }

    @Test
    void 사용자_조회_실패_테스트() {
        // then
        assertThrows(CustomException.class,
                () -> userService.login("test@gmail.com", "wrongPassword") );
    }

    @Test
    void 회원가입_성공_테스트() {
        // when
        MemberRequest userRequest = MemberRequest.createMemberRequest("test@gmail.com", "test", "test");

        when(memberRepository.save(any()))
                .thenReturn(getTestUser());

        // given
        MemberResponse join = userService.join(userRequest);

        // then
        assertThat(join.getEmail()).isEqualTo("test@gmail.com");
        assertThat(join.getName()).isEqualTo("test");
    }

    @Test
    void 회원가입_중복_이메일_실패_테스트() {
        // when
        MemberRequest userRequest = MemberRequest.createMemberRequest("test", "test@gmail.com", "test1");

        Member testUser = Member.create("test@gmail.com", "test","test");
        when(memberRepository.findMemberByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        // then
        assertThrows(CustomException.class,
                () -> userService.join(userRequest));
    }

    private Member getTestUser() {
        return Member.create("test@gmail.com", "test", "test1");
    }
}