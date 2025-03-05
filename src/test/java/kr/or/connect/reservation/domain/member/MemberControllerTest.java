package kr.or.connect.reservation.domain.member;

import kr.or.connect.reservation.domain.member.dto.MemberRequest;
import kr.or.connect.reservation.domain.member.dto.MemberResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static kr.or.connect.reservation.utils.UtilConstant.MEMBER_ID;
import static kr.or.connect.reservation.utils.UtilConstant.USER_EMAIL;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    void 회원가입() throws Exception {
        // given

        MemberResponse response = new MemberResponse(1L, "hj", "test@gmail.com");
        Mockito.when(memberService.join(Mockito.any(MemberRequest.class)))
                .thenReturn(response);

        String jsonBody = "{\"name\": \"hj\",\"email\": \"test@gmail.com\", \"password\": \"1234\"}";
        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("email").exists());
    }

    @Test
    void 로그인() throws Exception {

        MemberResponse response = new MemberResponse(1L, "hj", "test@gmail.com");
        Mockito.when(memberService.login(
                        ArgumentMatchers.argThat(email -> email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}")),
                        Mockito.anyString()))
                .thenReturn(response);


        String jsonBody = "{\"name\": \"hj\",\"email\": \"test@gmail.com\", \"password\": \"1234\"}";

        // 로그인 세션 확인!
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").exists());
    }

    @Test
    void 로그아웃() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(MEMBER_ID, "session");

        // 로그아웃 세션없음 확인!
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute(MEMBER_ID, nullValue()))
                .andExpect(request().sessionAttribute(USER_EMAIL, nullValue()));
    }
}