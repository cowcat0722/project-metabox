package org.example.metabox.book;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.metabox._core.util.ApiUtil;
import org.example.metabox.user.SessionGuest;
import org.example.metabox.user.SessionUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class BookController {
    private final HttpSession session;
    private final BookService bookService;
    private final RedisTemplate<String, Object> rt;

    @PostMapping("/books/complete")
    public ResponseEntity<?> completeBook(@RequestBody BookRequest.PaymentRequestDTO paymentRequestDTO) {
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");
        SessionGuest sessionGuest = (SessionGuest) rt.opsForValue().get("sessionGuest");
        Integer userId;
        if (sessionUser != null) {
            userId = sessionUser.getId();
            bookService.completeBook(paymentRequestDTO, userId);
            return ResponseEntity.ok("/mypage/detail-book");
        } else {
            userId = sessionGuest.getId();
            bookService.completeBookGuest(paymentRequestDTO, userId);
            return ResponseEntity.ok("/books/book-finish");
        }
    }

    @GetMapping("/books/book-finish")
    public String bookFinish(HttpServletRequest request) {
        SessionGuest sessionGuest = (SessionGuest) rt.opsForValue().get("sessionGuest");
        BookResponse.BookFinishDTO respDTO = bookService.bookFinish(sessionGuest);
        request.setAttribute("model", respDTO);
        return "book/book-finish";
    }

    //  TODO :  로그인 유저만 이용할 수 있도록 interceptor
    @GetMapping("/book-form")
    public String bookForm(HttpServletRequest request) {
        BookResponse.BookDTO respDTO = bookService.theaterAreaList();
        request.setAttribute("model", respDTO);
        return "book/book-form";
    }

    @GetMapping("/book-form-theater-ajax")
    public ResponseEntity<?> bookFormSubList(@RequestParam(value = "areaCode") String areaCode) {
        List<String> respDTO = bookService.bookFormSubList(areaCode);
        return ResponseEntity.ok(new ApiUtil<>(respDTO));
    }

    @GetMapping("/seat-form/{screeningInfoId}")
    public String seatForm(HttpServletRequest request, @PathVariable int screeningInfoId) {
        BookResponse.BookSeatDTO respDTO = bookService.seatDetail(screeningInfoId);
        System.out.println(respDTO);
        request.setAttribute("model", respDTO);
        return "book/seat-basic-form";
    }

    // 결제 페이지
    @GetMapping("/payment-form")
    public String bookForm(@RequestParam("id") int screeningInfoId, @RequestParam("ids") String ids, HttpServletRequest request) {
        // 콤마로 구분된 ID 문자열을 리스트로 변환
        List<String> idList = Arrays.asList(ids.split(","));
        SessionUser sessionUser = (SessionUser) rt.opsForValue().get("sessionUser");
        BookResponse.PaymentDTO respDTO = bookService.payment(idList, screeningInfoId, sessionUser);
        request.setAttribute("model", respDTO);
        return "payment/payment-form";
    }
}
