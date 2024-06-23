package org.example.metabox.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.metabox.movie.Movie;
import org.example.metabox.movie.MovieRequest;
import org.example.metabox.movie.MovieResponse;
import org.example.metabox.movie.MovieService;
import org.hibernate.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class AdminController {
    private final AdminService adminService;
    private final MovieService movieService;
    private final HttpSession session;

    @GetMapping("admin-login-form")
    public String Login(){return "admin/login-form";}

    @PostMapping("admin-login")
    public String adminLogin(AdminRequest.LoginDTO reqDTO) {
        Admin admin = adminService.login(reqDTO);

        SessionAdmin sessionAdmin = new SessionAdmin(admin.getId(), admin.getLoginId());
        session.setAttribute("sessionAdmin", sessionAdmin);
        return "redirect:movie-list";
    }

    //    TODO : admin 만 접속할 수 있도록 주소에 interceptor 설정
    //    TODO : description 칼럼 데이터 입력 방법 수정

    // 전체 영화 리스트를 조회하는 GET 요청 처리 메서드
    @GetMapping("/movie-list")
    public String movieList(HttpServletRequest request) {
        // 모든 영화 정보를 가져와 models 리스트에 저장합니다.
        List<MovieResponse.MovieChartDTO> movies = movieService.getAllMovies();

        // HttpServletRequest에 movies를 추가합니다.
        request.setAttribute("models", movies);

        // "admin/movie-list" 뷰를 반환하여 영화 리스트 페이지를 표시합니다.
        return "admin/movie-list";
    }

    // 'movieId'에 해당하는 영화 정보를 조회하는 GET 요청 처리 메서드
    @GetMapping("/movie-detail/{movieId}")
    public String movieDetail(@PathVariable("movieId") Integer movieId, HttpServletRequest request) {
        // movieId에 해당하는 영화 정보를 데이터베이스에서 조회하고 MovieDetailDTO에 저장합니다.
        MovieResponse.MovieDetailDTO movie = movieService.findById(movieId);

        // HttpServletRequest에 movie를 추가합니다.
        request.setAttribute("model", movie);

        // "admin/movie-detail" 뷰를 반환하여 영화 상세 페이지를 표시합니다.
        return "admin/movie-detail";
    }

    // 영화 등록 페이지 호출을 처리하는 GET 요청 메서드
    @GetMapping("/movie-save-form")
    public String movieSaveForm() {
        return "admin/movie-save-form";
    }

    // http://localhost:8080/admin-login-form
    // 영화 등록을 처리하는 POST 요청 메서드
    @PostMapping("/movie-save")
    public String movieAdd(MovieRequest.movieSavaFormDTO reqDTO) {
        movieService.addMovie(reqDTO);
        return "redirect:movie-list";
    }

    @GetMapping("/movie-edit-form")
    public String movieEditForm() {
        return "admin/movie-edit-form";
    }

    @GetMapping("/theater-save-form")
    public String theaterSaveForm() {
        return "admin/theater-save-form";
    }
}
