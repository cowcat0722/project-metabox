package org.example.metabox.book;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.metabox._core.errors.exception.Exception404;
import org.example.metabox.screening_info.ScreeningInfo;
import org.example.metabox.screening_info.ScreeningInfoRepository;
import org.example.metabox.seat.Seat;
import org.example.metabox.seat.SeatBook;
import org.example.metabox.seat.SeatBookRepository;
import org.example.metabox.seat.SeatRepository;
import org.example.metabox.theater.Theater;
import org.example.metabox.theater.TheaterRepository;
import org.example.metabox.user.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final TheaterRepository theaterRepository;
    private final ScreeningInfoRepository screeningInfoRepository;
    private final SeatRepository seatRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final SeatBookRepository seatBookRepository;

    public BookResponse.BookDTO theaterAreaList() {
        // 극장 지역 조회
        List<Theater> theaterList = theaterRepository.findAll();
        return new BookResponse.BookDTO(theaterList);
    }

    public List<String> bookFormSubList(String areaCode) {
        List<String> respDTO = screeningInfoRepository.findSubListByAreaCode(areaCode);
        return respDTO;
    }

    @Transactional
    public BookResponse.BookSeatDTO seatDetail(int screeningInfoId) {
        int screeningId = screeningInfoRepository.findByScreeningInfoId(screeningInfoId);
        List<Seat> seatList = seatRepository.findAllByScreeningId(screeningId);
        ScreeningInfo screeningInfo = screeningInfoRepository.findById(screeningInfoId).orElseThrow(() -> new Exception404("잘못된 접근 입니다."));
        return new BookResponse.BookSeatDTO(screeningInfo, seatList);
    }

    // 포인트 및 결제 페이지
    @Transactional
    public BookResponse.PaymentDTO payment(List<String> idList, int screeningInfoId, SessionUser sessionUser) {
        List<Seat> seatList = new ArrayList<>();
        for (int i = 0; i < idList.size(); i++) {
            Seat seat = seatRepository.findById(Integer.valueOf(idList.get(i))).orElseThrow(() -> new Exception404("좌석을 찾을 수 없습니다."));
            seatList.add(seat);
        }
        ScreeningInfo screeningInfo = screeningInfoRepository.findById(screeningInfoId).orElseThrow(() -> new Exception404("상영 정보를 찾을 수 없습니다."));
        int point = 0;
        if (sessionUser != null) {
            User user = userRepository.findById(sessionUser.getId()).orElseThrow(() -> new Exception404("유저 정보를 찾을 수 없습니다."));
            point = user.getPoint();
        }
        return new BookResponse.PaymentDTO(screeningInfo, seatList, point);
    }

    // 회원 예매내역 등록
    @Transactional
    public void completeBook(BookRequest.PaymentRequestDTO reqDTO, int userId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = now.format(formatter);

        int count = bookRepository.findAllByDate(now);
        String formattedBookId = String.format("%04d", count + 1);

        // 예약 테이블 만들기
        User user = userRepository.findById(userId).orElseThrow(() -> new Exception404("유저 정보를 찾을 수 없습니다."));
        Book book = bookRepository.save(Book.builder()
                .book_price(reqDTO.getBookPrice())
                .totalPrice(reqDTO.getTotalPrice())
                .point((int) (reqDTO.getBookPrice() * 0.1))
                .used_point(reqDTO.getUsedPoint())
                .user(user)
                .guest(null)
                .bookNum(formattedDate + formattedBookId)
                .build());

        // 예약된 좌석 등록
        List<Integer> selectedSeatIds = reqDTO.getSelectedSeatIds();
        ScreeningInfo screeningInfo = screeningInfoRepository.findById(reqDTO.getScreeningInfoId()).orElseThrow(() -> new Exception404("상영 정보를 찾을 수 없습니다."));
        Book booked = bookRepository.findById(book.getId()).orElseThrow(() -> new Exception404("예매 정보를 찾을 수 없습니다."));
        for (int i = 0; i < selectedSeatIds.size(); i++) {

            Seat seat = seatRepository.findById(selectedSeatIds.get(i)).orElseThrow(() -> new Exception404("좌석 정보를 찾을 수 없습니다."));
            seatBookRepository.save(SeatBook.builder()
                    .book(booked)
                    .screeningInfo(screeningInfo)
                    .seat(seat)
                    .build());
        }
    }

    public void completeBookGuest(BookRequest.PaymentRequestDTO reqDTO, int userId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = now.format(formatter);

        int count = bookRepository.findAllByDate(now);
        String formattedBookId = String.format("%04d", count + 1);

        Guest guest = guestRepository.findById(userId).orElseThrow(() -> new Exception404("비회원 정보를 찾을 수 없습니다."));
        Book book = bookRepository.save(Book.builder()
                .book_price(reqDTO.getBookPrice())
                .totalPrice(reqDTO.getTotalPrice())
                .point((int) (reqDTO.getBookPrice() * 0.1))
                .used_point(reqDTO.getUsedPoint())
                .user(null)
                .guest(guest)
                .bookNum(formattedDate + formattedBookId)
                .build());

        List<Integer> selectedSeatIds = reqDTO.getSelectedSeatIds();
        ScreeningInfo screeningInfo = screeningInfoRepository.findById(reqDTO.getScreeningInfoId()).orElseThrow(() -> new Exception404("상영 정보를 찾을 수 없습니다."));
        Book booked = bookRepository.findById(book.getId()).orElseThrow(() -> new Exception404("예매 정보를 찾을 수 없습니다."));
        for (int i = 0; i < selectedSeatIds.size(); i++) {

            Seat seat = seatRepository.findById(selectedSeatIds.get(i)).orElseThrow(() -> new Exception404("좌석 정보를 찾을 수 없습니다."));
            seatBookRepository.save(SeatBook.builder()
                    .book(booked)
                    .screeningInfo(screeningInfo)
                    .seat(seat)
                    .build());
        }
    }

    @Transactional
    public BookResponse.BookFinishDTO bookFinish(SessionGuest sessionGuest) {
        List<SeatBook> seatBookList = seatBookRepository.findByGuestId(sessionGuest.getId());
        return new BookResponse.BookFinishDTO(seatBookList);
    }
}
