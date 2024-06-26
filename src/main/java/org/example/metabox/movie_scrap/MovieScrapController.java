package org.example.metabox.movie_scrap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.metabox.user.SessionUser;
import org.example.metabox.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class MovieScrapController {
    private final MovieScrapService movieScrapService;
    private final HttpSession session;

    @PostMapping("/scrap/{id}/movie")
    public String scrapMovie(@PathVariable Integer id) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");
        movieScrapService.movieScrap(id,sessionUser.getId());
        return "redirect:/movies/list";
    }

    @GetMapping("/scrap/movie/list")
    public String scrapDetailMovie(HttpServletRequest request) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");
        List<MovieScrapResponse.ScrapMovieListDTO> movieScrapList = movieScrapService.movieScrapList(sessionUser.getId());
        request.setAttribute("movieScrapList", movieScrapList);
        return "user/mypage-detail-saw-scrap";
    }

}
