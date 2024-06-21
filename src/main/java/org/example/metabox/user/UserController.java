package org.example.metabox.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final HttpSession session;
    private final UserService userService;
    private final GuestRepository guestRepository;


    @GetMapping("/")
    public String mainForm(HttpServletRequest request) {
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
    public String login(UserRequest.JoinDTO reqDTO){
        Guest guest = userService.join(reqDTO);

        // 로그인 후 세션에 정보 저장
        SessionGuest sessionGuest = new SessionGuest(guest.getId(), guest.getBirth(), guest.getPhone());
        session.setAttribute("sessionGuest", sessionGuest);

        return "book/book-form";
    }

    @GetMapping("/mypage/home")
    public String mypageHome(HttpServletRequest request) {
        return "user/mypage-home";
    }

    @GetMapping("/mypage/detail-book")
    public String myBookDetail(HttpServletRequest request) {
        UserResponse.DetailBookDTO myBookDetail = userService.findMyBookDetail();
        request.setAttribute("model", myBookDetail);

        return "user/mypage-detail-book";
    }

    @GetMapping("/mypage/detail-saw")
    public String mySawDetail(HttpServletRequest request) {
        return "user/mypage-detail-saw";
    }

    // 일단 카카오만
    @GetMapping("/oauth/callback/kakao")
    public String oauthCallbackKakao(String code) {
//        System.out.println("코드 받나요 : " + code);
        SessionUser sessionUser = userService.loginKakao(code);
        session.setAttribute("sessionUser", sessionUser);
        return "redirect:/";
    }

    @GetMapping("/oauth/callback/naver")
    public String oauthCallbackNaver(String code) {
//        System.out.println("네이버 코드 : " + code);
        SessionUser sessionUser = userService.loginNaver(code);
        session.setAttribute("sessionUser", sessionUser);
        return "redirect:/";
    }

    //로그아웃
    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }

    // 회원탈퇴
    @GetMapping("/removeAccount")
    public String removeAccount() {
        System.out.println("작동함?");
        //토큰을 session에서 받아옴
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");
        if (sessionUser.getProvider().equals("kakao")) {
            userService.removeAccountKakao(sessionUser.getAccessToken(), sessionUser.getNickname());
        }

        if (sessionUser.getProvider().equals("naver")) {
            userService.removeAccountNaver(sessionUser.getAccessToken(), sessionUser.getNickname());
        }

        session.invalidate();
        return "redirect:/";
    }



}
