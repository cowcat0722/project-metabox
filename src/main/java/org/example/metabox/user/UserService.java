package org.example.metabox.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public SessionUser loginKakao(String code) {
        // 1.1 RestTemplate 설정
        RestTemplate rt = new RestTemplate();

        // 1.2 http header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 1.3 http body 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "7b3698608adc154feafed29a780bcd5b");
        body.add("redirect_uri", "http://localhost:8080/oauth/callback/kakao");
        body.add("code", code);

        // 1.4 body+header 객체 만들기
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        // 1.5 api 요청하기 (토큰 받기)
        ResponseEntity<UserResponse.TokenDTO> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                UserResponse.TokenDTO.class);

        // 1.6 값 확인
//        System.out.println(response.getBody().toString());
        String accessToken = response.getBody().getAccessToken();

        // 2. 토큰으로 사용자 정보 받기 (PK, Email)
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers2.add("Authorization", "Bearer " + response.getBody().getAccessToken());

        HttpEntity<MultiValueMap<String, String>> request2 =
                new HttpEntity<>(headers2);

        ResponseEntity<UserResponse.KakaoUserDTO> response2 = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request2,
                UserResponse.KakaoUserDTO.class);

//        System.out.println("response2 : " + response2.getBody().toString());

        // 3. 해당정보로 DB조회 (있을수, 없을수)
        String nickname = "kakao_" + response2.getBody().getId();
        User userPS = userRepository.findByNickname(nickname);


        // 4. 있으면? - 조회된 유저정보 리턴
        if(userPS != null){
            SessionUser sessionUser = new SessionUser(userPS,accessToken);
//            System.out.println("어? 유저가 있네? 강제로그인 진행");
            return sessionUser;
        }else{
//            System.out.println("어? 유저가 없네? 강제회원가입 and 강제로그인 진행");

            User user = User.builder()
                    .nickname(nickname)
                    .password(UUID.randomUUID().toString())
//                    .email(response2.getBody().getProperties().getNickname()+"@nate.com")
                    .imgFilename(response2.getBody().getProperties().getProfileImage())
                    .birthYear("1997")
                    .name("박찬혁")
                    .provider("kakao")
                    .build();

            User returnUser = userRepository.save(user);
            SessionUser sessionUser = new SessionUser(returnUser,accessToken);
            return sessionUser;
        }
    }

    public SessionUser loginNaver(String code) {
        // 1.1 RestTemplate 설정
        RestTemplate rt = new RestTemplate();

        // 1.2 http header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 1.3 http body 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "MKcMHT6RxvcSJjGvAutc");
        body.add("client_secret", "SOCj3hVG3I");
        body.add("redirect_uri", "http://localhost:8080/oauth/callback/naver");
        body.add("code", code);
        body.add("status", "1234");

        // 1.4 body+header 객체 만들기
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        // 1.5 api 요청하기 (토큰 받기)
        ResponseEntity<UserResponse.TokenDTO> response = rt.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                request,
                UserResponse.TokenDTO.class);

        // 1.6 값 확인
//        System.out.println(response.getBody().toString());
        String accessToken = response.getBody().getAccessToken();

//        // 2. 토큰으로 사용자 정보 받기 (PK, Email)
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers2.add("Authorization", "Bearer " + response.getBody().getAccessToken());

        HttpEntity<MultiValueMap<String, String>> request2 =
                new HttpEntity<>(headers2);

        ResponseEntity<UserResponse.NaverUserDTO> response2 = rt.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                request2,
                UserResponse.NaverUserDTO.class);

//        System.out.println("response2 : " + response2.getBody().toString());

//        // 3. 해당정보로 DB조회 (있을수, 없을수)
        String nickname = "naver_" + response2.getBody().getResponse().getId();
        User userPS = userRepository.findByNickname(nickname);

//        // 4. 있으면? - 조회된 유저정보 리턴
        if(userPS != null){
            SessionUser sessionUser = new SessionUser(userPS, accessToken);
//            System.out.println("어? 유저가 있네? 강제로그인 진행");
            return sessionUser;

        }else {
//            System.out.println("어? 유저가 없네? 강제회원가입 and 강제로그인 진행");
            // 5. 없으면? - 강제 회원가입
            User user = User.builder()
                    .nickname(nickname)
                    .password(UUID.randomUUID().toString())
//                    .email(response2.getBody().getResponse().getEmail())
                    .imgFilename(response2.getBody().getResponse().getProfileImage())
                    .name(response2.getBody().getResponse().getName())
                    .birthYear(response2.getBody().getResponse().getBirthyear())
                    .provider("naver")
                    .build();
            User returnUser = userRepository.save(user);
            SessionUser sessionUser = new SessionUser(returnUser, accessToken);
            return sessionUser;
        }
    }

    @Transactional
    public void logoutKakao(String accessToken, String nickname) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v1/user/unlink",
                HttpMethod.POST,
                request,
                String.class);

        // 성공 여부를 확인
//        System.out.println("카카오 회원탈퇴 응답: " + response.getBody());

        userRepository.deleteByNickname(nickname);

    }

    @Transactional
    public void logoutNaver(String accessToken, String nickname) {
        RestTemplate rt = new RestTemplate();

        String url = String.format(
                "https://nid.naver.com/oauth2.0/token?grant_type=delete&client_id=MKcMHT6RxvcSJjGvAutc&client_secret=SOCj3hVG3I&access_token=%s&service_provider=NAVER",
                accessToken
        );

        // API 호출
        String response = rt.getForObject(url, String.class);
//        System.out.println("네이버 회원탈퇴 결과: " + response);

        userRepository.deleteByNickname(nickname);

    }
}
