package org.example.metabox.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.metabox._core.util.ApiUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RedisTemplate<String, Object> rt;
    private final HttpSession session;

    @GetMapping("/redis/test")
    public @ResponseBody String redisTest() {
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");
        return "redis test";
    }


    @GetMapping("/")
    public String mainForm(HttpServletRequest request) {
        // 메인페이지 무비차트 / 상영예정작
        UserResponse.MainChartDTO mainCharts = userService.findMainMovie();
        request.setAttribute("model", mainCharts);

        return "index";
    }

    @GetMapping("/login-form")
    public String loginForm(HttpServletRequest request) {
        return "user/login-form";
    }

    @GetMapping("/guest/login-form")
    public String nonMemberForm() {
        return "user/non-member";
    }

    @PostMapping("/guest/join")
    public String login(@Valid UserRequest.JoinDTO reqDTO, Errors errors) {
        Guest guest = userService.join(reqDTO);

        // 로그인 후 세션에 정보 저장
        SessionGuest sessionGuest = new SessionGuest(guest.getId(), guest.getBirth(), guest.getPhone());

        rt.opsForValue().set("sessionGuest", sessionGuest);
        return "redirect:/theaters/movie-schedule";
    }

    @GetMapping("/guest/book-check-form")
    public String nonMemberCheckForm() {
        return "user/non-member-check-form";
    }

    // TODO: ApiException 필요할듯?
    @PostMapping("/guest/book-check")
    public ResponseEntity<?> nonMemberCheck(@Valid @RequestBody UserRequest.GuestBookCheckDTO reqDTO, Errors errors) {
        UserResponse.GuestCheckDTO guestCheckDTO = userService.findGuestBook(reqDTO);

        return ResponseEntity.ok(new ApiUtil<>(guestCheckDTO));
    }

    @GetMapping("/mypage/home")
    public String mypageHome(HttpServletRequest request) {
        // user 타입 아니고 SessionUser 타입이니 조심! (sessionUser가 SessionUser 타입임)
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");
        UserResponse.MyPageHomeDTO homeDTO = userService.findMyPageHome(sessionUser);
        request.setAttribute("model", homeDTO);

        return "user/mypage-home";
    }

    // 컨트롤러는 하나의 서비스만 호출하기
    @PostMapping("/mypage/home/scrap")
    public ResponseEntity<?> mypageHomeScrap(HttpServletRequest request, @RequestBody List<UserRequest.TheaterScrapDTO> reqDTOs) {
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");

        List<UserResponse.TheaterNameDTO> theaterNameDTOS = userService.myScrapSave(sessionUser.getId(), reqDTOs);


        return ResponseEntity.ok(new ApiUtil(theaterNameDTOS)); // apiUtil
    }


    @GetMapping("/mypage/detail-book")
    public String myBookDetail(HttpServletRequest request) {
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");

        UserResponse.DetailBookDTO myBookDetail = userService.findMyBookDetail(sessionUser);
        request.setAttribute("model", myBookDetail);

        return "user/mypage-detail-book";
    }

    @GetMapping("/mypage/detail-saw")
    public String mySawDetail(HttpServletRequest request) {
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");
        UserResponse.MyPageDetailDTO ticketedList = userService.getMovieSaw(sessionUser);
        request.setAttribute("ticketedList", ticketedList);
        return "user/mypage-detail-saw";
    }

    // 일단 카카오만
    @GetMapping("/oauth/callback/kakao")
    public String oauthCallbackKakao(String code) {
        SessionUser sessionUser = userService.loginKakao(code);
        rt.opsForValue().set("sessionUser", sessionUser);
        session.setAttribute("sessionUser", sessionUser);
        return "redirect:/";
    }

    @GetMapping("/oauth/callback/naver")
    public String oauthCallbackNaver(String code) {
        SessionUser sessionUser = userService.loginNaver(code);
        rt.opsForValue().set("sessionUser", sessionUser);
        session.setAttribute("sessionUser", sessionUser);
        return "redirect:/";
    }

    //로그아웃
    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        rt.delete("sessionUser");
        return "redirect:/";
    }

    // 회원탈퇴
    @GetMapping("/removeAccount")
    public String removeAccount() {
        //토큰을 session에서 받아옴
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");
        if (sessionUser.getProvider().equals("kakao")) {
            userService.removeAccountKakao(sessionUser.getAccessToken(), sessionUser.getNickname());
        }

        if (sessionUser.getProvider().equals("naver")) {
            userService.removeAccountNaver(sessionUser.getAccessToken(), sessionUser.getNickname());
        }

        session.invalidate();
        rt.delete("sessionUser");
        return "redirect:/";
    }


}
