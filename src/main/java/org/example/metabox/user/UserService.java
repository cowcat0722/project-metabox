package org.example.metabox.user;

import lombok.RequiredArgsConstructor;
import org.example.metabox._core.errors.exception.*;
import org.example.metabox.book.BookRepository;
import org.example.metabox.movie.Movie;
import org.example.metabox.movie.MovieQueryRepository;
import org.example.metabox.movie.MovieRepository;
import org.example.metabox.movie_scrap.MovieScrap;
import org.example.metabox.movie_scrap.MovieScrapRepository;
import org.example.metabox.review.Review;
import org.example.metabox.review.ReviewRepository;
import org.example.metabox.review.ReviewResponse;
import org.example.metabox.theater.Theater;
import org.example.metabox.theater.TheaterRepository;
import org.example.metabox.theater_scrap.TheaterScrap;
import org.example.metabox.theater_scrap.TheaterScrapRepository;
import org.example.metabox.trailer.Trailer;
import org.example.metabox.trailer.TrailerRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MovieQueryRepository movieQueryRepository;
    private final GuestRepository guestRepository;
    private final TheaterRepository theaterRepository;
    private final TheaterScrapRepository theaterScrapRepository;
    private final TrailerRepository trailerRepository;
    private final ReviewRepository reviewRepository;
    private final MovieScrapRepository movieScrapRepository;

    // 마이페이지의 theater Scrap save, update
    @Transactional
    public List<UserResponse.TheaterNameDTO> myScrapSave(Integer sessionUserId, List<UserRequest.TheaterScrapDTO> reqDTOs) {
        List<Integer> theaterIds = reqDTOs.stream().mapToInt(value -> value.getTheaterNameId()).boxed().toList();

        // 세션에서 받아오는게 더 나은가
        User user = userRepository.findById(sessionUserId).orElseThrow(() -> new Exception404("유저 못찾음"));
        // 존재하는지 한 번 더 확인
        Boolean userExist = theaterScrapRepository.findByExistUserId(user.getId());

        if (userExist) {
            // 기존 스크랩 삭제 (유저에 있는 것 모두 삭제 되어야함! )
            theaterScrapRepository.deleteAllByUser(user.getId());
        }

        for (int i = 0; i < reqDTOs.size(); i++) {
            Theater theater = theaterRepository.findById(theaterIds.get(i)).orElseThrow(() -> new Exception404("영화관 못찾음"));
            theaterScrapRepository.save(TheaterScrap.builder()
                    .user(user)
                    .theater(theater)
                    .build());
        }

        return userRepository.findByTheaterId(theaterIds);
    }

    // 메인 페이지 무비차트, 상영예정작
    public UserResponse.MainChartDTO findMainMovie() {

        //1위 영화
        Integer topMovieId = movieQueryRepository.firstRankMovie();

        // 영화의 트레일러 찾기
        Trailer oneTrailer = trailerRepository.findById(topMovieId)
                .orElseThrow(() -> new RuntimeException("예매율 1위의 트레일러가 없습니다."));


        List<UserResponse.MainChartDTO.MainMovieChartDTO> movieChartDTOS = movieQueryRepository.getMainMovieChart();



        // 순위 계산
        for (int i = 0; i < movieChartDTOS.size(); i++) {
            movieChartDTOS.get(i).setRank(i + 1);
        }


        // 상영예정작
        List<UserResponse.MainChartDTO.ToBeChartDTO> toBeChartDTOS = movieQueryRepository.getToBeChart();

        UserResponse.MainChartDTO mainChartDTO = UserResponse.MainChartDTO.builder()
                .movieCharts(movieChartDTOS)
                .toBeCharts(toBeChartDTOS)
                .trailerId(oneTrailer.getId())
                .build();
        return mainChartDTO;
    }

    // 게스트 예매 조회
    @Transactional
    public UserResponse.GuestCheckDTO findGuestBook(UserRequest.GuestBookCheckDTO reqDTO) {
        UserResponse.GuestCheckDTO.UserDTO guest = guestRepository.findByGuest(reqDTO.getName(), reqDTO.getPassword(), reqDTO.getPhone(), reqDTO.getBookNumb())
                .orElseThrow(() -> new ApiException400("잘못된 정보를 입력하셨습니다."));

        List<UserResponse.GuestCheckDTO.SeatDTO> seatDTOs = movieQueryRepository.findGuestTicketV1(guest.getGuestId());
        List<UserResponse.GuestCheckDTO.TotalPriceDTO> totalPriceDTOs = movieQueryRepository.findGuestTicketV3(guest.getGuestId());
        List<UserResponse.GuestCheckDTO.TicketingDTO> ticketingDTOs = movieQueryRepository.findGuestTicketV2(guest.getGuestId(), totalPriceDTOs, seatDTOs);

        UserResponse.GuestCheckDTO guestCheckDTO = new UserResponse.GuestCheckDTO(guest, ticketingDTOs);

        return guestCheckDTO;

    }




    // 마이페이지 detail-book의 today best 무비차트
    public UserResponse.DetailBookDTO findMyBookDetail(SessionUser sessionUser) {
        User userOP = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new Exception401("로그인이 필요한 서비스입니다."));

        List<UserResponse.DetailBookDTO.MovieChartDTO> movieChartDTOS = movieQueryRepository.getMovieChart();
        UserResponse.DetailBookDTO.UserDTO userDTO = new UserResponse.DetailBookDTO.UserDTO(userOP);
//        //내 예매내역 중 좌석, 티켓
        List<UserResponse.DetailBookDTO.SeatDTO> seatDTOs = movieQueryRepository.findUnwatchTicketV1(sessionUser.getId());
        List<UserResponse.DetailBookDTO.TotalPriceDTO> totalPriceDTOs = movieQueryRepository.findUnwatchTicketV3(sessionUser.getId());
        List<UserResponse.DetailBookDTO.TicketingDTO> ticketingDTOs = movieQueryRepository.findUnwatchTicketV2(sessionUser.getId(), totalPriceDTOs, seatDTOs);


        // 상영관 가져오기
        List<Theater> theaterList = theaterRepository.findAll();

        // pk 순으로 먼저 정렬해서 중복제거 하기
        theaterList.sort(Comparator.comparing(theater -> theater.getId()));

        // areaName 중복제거    //theaterDistinct = [서울, 경기, 인천, 강원, 대전/충청, 대구, 부산/울산, 경상, 광주/전라/제주]
        List<String> theaterDistinct = theaterList.stream().map(theater -> theater.getAreaName())
                .distinct().collect(Collectors.toList());

        // METABOX 강남 METABOX 여수 .. 이런 것이 지역별로 맞게 나와야함 . filter 사용
        List<UserResponse.DetailBookDTO.TheaterDTO> theaterDTOS = new ArrayList<>();
        for (String areaName : theaterDistinct) {
            Integer theaterId = theaterList.stream().filter(theater -> theater.getAreaName().equals(areaName))
                    .map(theater -> theater.getId()).findFirst().orElse(null);

            List<UserResponse.DetailBookDTO.TheaterDTO.TheaterNameDTO> theaterNameDTOS = theaterList.stream()
                    .filter(theater -> theater.getAreaName().equals(areaName))
                    .map(theater -> new UserResponse.DetailBookDTO.TheaterDTO.TheaterNameDTO(theater))
                    .collect(Collectors.toList());

            theaterDTOS.add(new UserResponse.DetailBookDTO.TheaterDTO(theaterId, areaName, theaterNameDTOS));
        }

        // 스크랩
        List<TheaterScrap> scraps = theaterScrapRepository.findByUserId(sessionUser.getId());
        List<UserResponse.DetailBookDTO.TheaterScrapDTO> theaterScrapDTOS = scraps.stream().map(theaterScrap ->
                new UserResponse.DetailBookDTO.TheaterScrapDTO(theaterScrap.getTheater().getId(), theaterScrap.getTheater().getName())).toList();

        // DetailBookDTO 로 변형
        UserResponse.DetailBookDTO detailBookDTO = UserResponse.DetailBookDTO.builder()
                .userDTO(userDTO)
                .movieCharts(movieChartDTOS)
                .theaterDTOS(theaterDTOS)
                .theaterScrapDTOS(theaterScrapDTOS)
                .ticketingDTO(ticketingDTOs)
                .build();

        return detailBookDTO;
    }


    //mypage/home 유저조회 및 예매내역, 취소내역 조회, 극장 스크랩
    public UserResponse.MyPageHomeDTO findMyPageHome(SessionUser sessionUser) {
        User userOP = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new Exception401("로그인이 필요한 서비스입니다."));

        UserResponse.MyPageHomeDTO.UserDTO userDTO = new UserResponse.MyPageHomeDTO.UserDTO(userOP);
        List<UserResponse.MyPageHomeDTO.TicketingDTO> ticketingDTOS = movieQueryRepository.findMyTicketing(sessionUser.getId());

        //개수파악 (0건 <- 여기 뿌릴라고)
        int ticketCount = ticketingDTOS.size();

        // 상영관 가져오기
        List<Theater> theaterList = theaterRepository.findAll();

        // pk 순으로 먼저 정렬해서 중복제거 하기
        theaterList.sort(Comparator.comparing(theater -> theater.getId()));

        // areaName 중복제거    //theaterDistinct = [서울, 경기, 인천, 강원, 대전/충청, 대구, 부산/울산, 경상, 광주/전라/제주]
        List<String> theaterDistinct = theaterList.stream().map(theater -> theater.getAreaName())
                .distinct()
                .collect(Collectors.toList());


        // METABOX 강남 METABOX 여수 .. 이런 것이 지역별로 맞게 나와야함 . filter 사용
        List<UserResponse.MyPageHomeDTO.TheaterDTO> theaterDTOS = new ArrayList<>();
        for (String areaName : theaterDistinct) {
            Integer theaterId = theaterList.stream().filter(theater -> theater.getAreaName().equals(areaName))
                    .map(theater -> theater.getId()).findFirst().orElse(null);

            List<UserResponse.MyPageHomeDTO.TheaterDTO.TheaterNameDTO> theaterNameDTOS = theaterList.stream()
                    .filter(theater -> theater.getAreaName().equals(areaName))
                    .map(theater -> new UserResponse.MyPageHomeDTO.TheaterDTO.TheaterNameDTO(theater))
                    .collect(Collectors.toList());

            theaterDTOS.add(new UserResponse.MyPageHomeDTO.TheaterDTO(theaterId, areaName, theaterNameDTOS));
        }

        List<TheaterScrap> scraps = theaterScrapRepository.findByUserId(sessionUser.getId());
        List<UserResponse.MyPageHomeDTO.TheaterScrapDTO> theaterScrapDTOS = scraps.stream().map(theaterScrap ->
                new UserResponse.MyPageHomeDTO.TheaterScrapDTO(theaterScrap.getTheater().getId(), theaterScrap.getTheater().getName())).toList();

        UserResponse.MyPageHomeDTO homeDTO = new UserResponse.MyPageHomeDTO(userDTO, ticketingDTOS, theaterDTOS, theaterScrapDTOS, ticketCount);
//        UserResponse.MyPageHomeDTO homeDTO = UserResponse.MyPageHomeDTO.builder()
//                .userDTO(userDTO)
//                .ticketingDTO(ticketingDTOS)
//                .build();

        return homeDTO;

    }

    //mypage/detail-saw
    public UserResponse.MyPageDetailDTO getMovieSaw(SessionUser sessionUser) {

        List<UserResponse.MyPageDetailDTO.TicketedDTO> ticketedList = movieQueryRepository.findMyTicked(sessionUser.getId());
        Integer ticketCount = ticketedList.size();
        return new UserResponse.MyPageDetailDTO(ticketedList,ticketCount);
    }


    //비회원 회원가입
    public Guest join(UserRequest.JoinDTO reqDTO) {
        // 1. 동일 한 휴대폰 정보가 있는지
        Guest guest = guestRepository.findOneByPhone(reqDTO.getPhone());


        //2. 동일 한 정보가 있다면 바로 로그인
        if (guest != null) {
            guest = guestRepository.findByBirthAndPassword(reqDTO.getBirth(), reqDTO.getPassword());

            return guest;

        } else {
            //3. 동일한 정보가 없으면 회원가입
            Guest newGuest = guestRepository.save(Guest.builder()
                    .birth(reqDTO.getBirth())
                    .password(reqDTO.getPassword())
                    .name(reqDTO.getName())
                    .phone(reqDTO.getPhone())
                    .build());

            //4. 회원가입 됐으면 로그인 진행
            newGuest = guestRepository.findByBirthAndPassword(reqDTO.getBirth(), reqDTO.getPassword());
            return newGuest;
        }
    }

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


        // 3. 해당정보로 DB조회 (있을수, 없을수)
        String nickname = "kakao_" + response2.getBody().getId();
        User userPS = userRepository.findByNickname(nickname);


        // 4. 있으면? - 조회된 유저정보 리턴
        if (userPS != null) {
            SessionUser sessionUser = new SessionUser(userPS, accessToken);
            return sessionUser;
        } else {

            User user = User.builder()
                    .nickname(nickname)
                    .password(UUID.randomUUID().toString())
//                    .email(response2.getBody().getProperties().getNickname()+"@nate.com")
                    .imgFilename(response2.getBody().getProperties().getProfileImage())
                    .birthYear("2000")
                    .name("박선규")
                    .provider("kakao")
                    .build();

            User returnUser = userRepository.save(user);
            SessionUser sessionUser = new SessionUser(returnUser, accessToken);
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


//        // 3. 해당정보로 DB조회 (있을수, 없을수)
        String nickname = "naver_" + response2.getBody().getResponse().getId();
        User userPS = userRepository.findByNickname(nickname);

//        // 4. 있으면? - 조회된 유저정보 리턴
        if (userPS != null) {
            SessionUser sessionUser = new SessionUser(userPS, accessToken);
            return sessionUser;

        } else {
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
    public void removeAccountKakao(String accessToken, String nickname) {
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

        userRepository.deleteByNickname(nickname);

    }

    @Transactional
    public void removeAccountNaver(String accessToken, String nickname) {
        RestTemplate rt = new RestTemplate();

        String url = String.format(
                "https://nid.naver.com/oauth2.0/token?grant_type=delete&client_id=MKcMHT6RxvcSJjGvAutc&client_secret=SOCj3hVG3I&access_token=%s&service_provider=NAVER",
                accessToken
        );

        // API 호출
        String response = rt.getForObject(url, String.class);

        userRepository.deleteByNickname(nickname);

    }


}
