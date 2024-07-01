package org.example.metabox.book;

import lombok.Data;
import org.example.metabox.screening_info.ScreeningInfo;
import org.example.metabox.seat.Seat;
import org.example.metabox.seat.SeatBook;
import org.example.metabox.theater.Theater;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.metabox._core.util.FormatUtil.getFormattedPrice;

public class BookResponse {

    @Data
    public static class BookFinishDTO {
        private String name;
        private String movieTitle;
        private String theaterName;
        private String screeningName;
        private String screeningTime;
        private String totalPrice;
        private List<String> seatNumList;

        public BookFinishDTO(List<SeatBook> seatBookList) {
            this.name = seatBookList.getFirst().getBook().getGuest().getName();
            this.movieTitle = seatBookList.getFirst().getScreeningInfo().getMovie().getTitle();
            this.theaterName = seatBookList.getFirst().getScreeningInfo().getScreening().getTheater().getName();
            this.screeningName = seatBookList.getFirst().getScreeningInfo().getScreening().getName();
            this.screeningTime = seatBookList.getFirst().getScreeningInfo().getDate().toString();
            this.totalPrice = getFormattedPrice(seatBookList.getFirst().getBook().getBook_price());
            this.seatNumList = seatBookList.stream()
                    .map(seatBook -> seatBook.getSeat().getCode())
                    .collect(Collectors.toList());
        }
    }


    @Data
    public static class PaymentDTO {
        private String movieTitle;
        private String theaterTitle;
        private String date;
        private String startTime;
        private String endTime;
        private String info;
        private int screeningInfoId;
        private String screeningName;
        private List<SelectedSeatDTO> selectedSeatDTOList;
        private String price;
        private String point;

        private class SelectedSeatDTO {
            private int seatId;
            private String seatCode;
            private String seatType;
            private String seatPrice;

            public SelectedSeatDTO(Seat seat) {
                this.seatId = seat.getId();
                this.seatCode = seat.getCode();
                this.seatType = seat.getSeatTypeKo();
                this.seatPrice = getFormattedPrice(seat.getSeatPrice());
            }

        }

        public PaymentDTO(ScreeningInfo screeningInfo, List<Seat> seatList, int point) {
            this.movieTitle = screeningInfo.getMovie().getTitle();
            this.theaterTitle = screeningInfo.getScreening().getTheater().getName();
            this.date = String.valueOf(screeningInfo.getDate());
            this.screeningInfoId = screeningInfo.getId();
            this.startTime = screeningInfo.getStartTime();
            this.endTime = screeningInfo.getEndTime();
            this.info = screeningInfo.getMovie().getInfo();
            this.screeningName = screeningInfo.getScreening().getName();
            this.selectedSeatDTOList = seatList.stream().map(SelectedSeatDTO::new).collect(Collectors.toList());
            this.price = getFormattedPrice(seatList.stream().mapToInt(Seat::getSeatPrice).sum());
            this.point = getFormattedPrice(point);
        }
    }

    @Data
    public static class BookSeatDTO {
        private int screeningInfoId;
        private String movieTitle;
        private String TheaterTitle;
        private String date;
        private String startTime;
        private String endTime;
        private String info;
        private String screeningName;
        private String movieImgFilename;
        private String screeningRank;
        private List<SeatDTO> seatDTOList;
        private List<BookedSeatDTO> bookedSeatDTOList;

        public BookSeatDTO(ScreeningInfo screeningInfo, List<Seat> seatList) {
            this.screeningInfoId = screeningInfo.getId();
            this.movieImgFilename = screeningInfo.getMovie().getImgFilename();
            this.info = screeningInfo.getMovie().getInfo();
            this.screeningName = screeningInfo.getScreening().getName();
            this.movieTitle = screeningInfo.getMovie().getTitle();
            this.TheaterTitle = screeningInfo.getScreening().getTheater().getName();
            this.date = String.valueOf(screeningInfo.getDate());
            this.startTime = screeningInfo.getStartTime();
            this.endTime = screeningInfo.getEndTime();
            this.screeningRank = String.valueOf(screeningInfo.getScreening().getScreeningRank());
            this.seatDTOList = seatList.stream().map(SeatDTO::new).toList();
            this.bookedSeatDTOList = screeningInfo.getSeatBookList().stream().map(BookedSeatDTO::new).toList();

        }

        @Data
        private class BookedSeatDTO {
            int bookedSeatId;
            String bookedSeatCode;
            String bookedSeatType;

            public BookedSeatDTO(SeatBook seatBook) {
                this.bookedSeatId = seatBook.getSeat().getId();
                this.bookedSeatCode = seatBook.getSeat().getCode();
                this.bookedSeatType = String.valueOf(seatBook.getSeat().getType()).toLowerCase();
            }
        }

        @Data
        private class SeatDTO {
            private String seatCode;
            private String seatType;
            private int seatId;

            public SeatDTO(Seat seat) {
                this.seatId = seat.getId();
                this.seatCode = seat.getCode();
                this.seatType = String.valueOf(seat.getType()).toLowerCase();
            }
        }
    }


    @Data
    public static class BookDTO {

        private List<TheaterAreaDTO> theaterAreaDTOList;

        public BookDTO(List<Theater> theaterList) {

            this.theaterAreaDTOList = theaterList.stream()
                    .collect(Collectors.groupingBy(Theater::getAreaCode))
                    .entrySet().stream()
                    .map(entry -> new TheaterAreaDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        @Data
        private class TheaterAreaDTO {
            private String areaCode;
            private String areaName;

            public TheaterAreaDTO(String areaCode, List<Theater> theaters) {
                this.areaCode = areaCode;
                this.areaName = theaters.isEmpty() ? "" : theaters.get(0).getAreaName();
            }

        }
    }
}
