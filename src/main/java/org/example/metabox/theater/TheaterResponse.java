package org.example.metabox.theater;

import lombok.Builder;
import lombok.Data;
import org.example.metabox.screening.Screening;
import org.example.metabox.screening_info.ScreeningInfo;
import org.example.metabox.theater_scrap.TheaterScrap;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TheaterResponse {

    @Data
    public static class TheaterInfoDTO {
        private String theaterName;
        private int theaterId;
        private String theaterImgFilename;
        private List<ScrapDTO> scrapDTOList;
        private List<TheaterAreaDTO> theaterAreaDTOList;

        public TheaterInfoDTO(List<TheaterScrap> theaterScrapList, List<Theater> theaterList, Theater theater) {
            this.theaterName = theater.getName();
            this.theaterId = theater.getId();
            this.theaterImgFilename = theater.getImgFilename();
            this.scrapDTOList = theaterScrapList.stream().map(ScrapDTO::new).collect(Collectors.toList());
            this.theaterAreaDTOList = theaterList.stream()
                    .collect(Collectors.groupingBy(Theater::getAreaCode))
                    .entrySet().stream()
                    .map(entry -> new TheaterAreaDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            ;
        }

        @Data
        private class ScrapDTO {
            private String id;
            private String theaterId;
            private String theaterName;

            public ScrapDTO(TheaterScrap theaterScrap) {
                this.id = String.valueOf(theaterScrap.getId());
                this.theaterId = String.valueOf(theaterScrap.getTheater().getId());
                this.theaterName = theaterScrap.getTheater().getName();
            }
        }

        @Data
        private class TheaterAreaDTO {
            private String areaCode;
            private String areaName;
            private List<TheaterNameDTO> theaterNameDTOList;

            public TheaterAreaDTO(String areaCode, List<Theater> theaters) {
                this.areaCode = areaCode;
                this.areaName = theaters.isEmpty() ? "" : theaters.get(0).getAreaName();
                this.theaterNameDTOList = theaters.stream().map(TheaterNameDTO::new).collect(Collectors.toList());
            }

            @Data
            private class TheaterNameDTO {
                private int theaterId;
                private String theaterName;
                private String areaCode;

                public TheaterNameDTO(Theater theater) {
                    this.theaterId = theater.getId();
                    this.theaterName = theater.getName();
                    this.areaCode = theater.getAreaCode();
                }
            }
        }

    }


    @Data
    public static class TheaterDTO {
        private String theaterName;
        private int theaterId;
        private String theaterImgFilename;
        private List<ScrapDTO> scrapDTOList;
        private List<TheaterAreaDTO> theaterAreaDTOList;
        private List<ScreeningInfoDTO> screeningInfoDTOList;

        public TheaterDTO(List<TheaterScrap> theaterScrapList, List<Theater> theaterList, List<ScreeningInfo> screeningInfoList, Theater theater) {
            this.theaterName = theater.getName();
            this.theaterId = theater.getId();
            this.theaterImgFilename = theater.getImgFilename();
            this.scrapDTOList = theaterScrapList.stream().map(ScrapDTO::new).collect(Collectors.toList());
            this.theaterAreaDTOList = theaterList.stream()
                    .collect(Collectors.groupingBy(Theater::getAreaCode))
                    .entrySet().stream()
                    .map(entry -> new TheaterAreaDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            this.screeningInfoDTOList = screeningInfoList.stream()
                    .collect(Collectors.groupingBy(screeningInfo -> screeningInfo.getMovie().getTitle()))
                    .entrySet().stream()
                    .map(entry -> new ScreeningInfoDTO(entry.getValue()))
                    .collect(Collectors.toList());
        }

        @Data
        private class ScrapDTO {
            private String id;
            private String theaterId;
            private String theaterName;

            public ScrapDTO(TheaterScrap theaterScrap) {
                this.id = String.valueOf(theaterScrap.getId());
                this.theaterId = String.valueOf(theaterScrap.getTheater().getId());
                this.theaterName = theaterScrap.getTheater().getName();
            }
        }

        @Data
        private class TheaterAreaDTO {
            private String areaCode;
            private String areaName;
            private List<TheaterNameDTO> theaterNameDTOList;

            public TheaterAreaDTO(String areaCode, List<Theater> theaters) {
                this.areaCode = areaCode;
                this.areaName = theaters.isEmpty() ? "" : theaters.get(0).getAreaName();
                this.theaterNameDTOList = theaters.stream().map(TheaterNameDTO::new).collect(Collectors.toList());
            }

            @Data
            private class TheaterNameDTO {
                private int theaterId;
                private String theaterName;

                public TheaterNameDTO(Theater theater) {
                    this.theaterId = theater.getId();
                    this.theaterName = theater.getName();
                }
            }
        }

        @Data
        private class ScreeningInfoDTO {
            private String movieTitle;
            private String movieInfo;
            private List<ScreeningDTO> screeningList;

            public ScreeningInfoDTO(List<ScreeningInfo> screeningInfoList) {
                if (!screeningInfoList.isEmpty()) {
                    ScreeningInfo firstScreeningInfo = screeningInfoList.get(0);
                    this.movieTitle = firstScreeningInfo.getMovie().getTitle();
                    this.movieInfo = firstScreeningInfo.getMovie().getInfo();
                    this.screeningList = screeningInfoList.stream()
                            .collect(Collectors.groupingBy(ScreeningInfo::getScreening))
                            .entrySet().stream()
                            .map(entry -> new ScreeningDTO(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                }
            }

            @Data
            private class ScreeningDTO {
                private String screeningName;
                private Integer screeningSeatCount;
                private String screeningRank;
                private List<ScreeningTimeDTO> screeningTimeList;

                public ScreeningDTO(Screening screening, List<ScreeningInfo> screeningInfoList) {
                    this.screeningName = screening.getName();
                    this.screeningSeatCount = screening.getSeatCount();
                    this.screeningRank = screening.getScreeningRankKo();
                    this.screeningTimeList = screeningInfoList.stream()
                            .map(ScreeningTimeDTO::new)
                            .collect(Collectors.toList());
                }

                @Data
                private class ScreeningTimeDTO {
                    private String startTime;
                    private Integer currentSeatCount;
                    private Integer screeningInfoId;

                    public ScreeningTimeDTO(ScreeningInfo screeningInfo) {
                        this.startTime = screeningInfo.getStartTime();
                        this.currentSeatCount = screeningInfo.getScreening().getSeatCount() - screeningInfo.getSeatBookList().size();
                        this.screeningInfoId = screeningInfo.getId();
                    }
                }
            }
        }
    }

    @Data
    public static class TheaterAjaxDTO {
        private List<ScreeningInfoDTO> screeningInfoDTOList;

        public TheaterAjaxDTO(List<ScreeningInfo> screeningInfoList) {
            this.screeningInfoDTOList = screeningInfoList.stream()
                    .collect(Collectors.groupingBy(screeningInfo -> screeningInfo.getMovie().getTitle()))
                    .entrySet().stream()
                    .map(entry -> new ScreeningInfoDTO(entry.getValue()))
                    .collect(Collectors.toList());
        }

        @Data
        private class ScreeningInfoDTO {
            private String movieTitle;
            private String movieInfo;
            private List<ScreeningDTO> screeningList;

            public ScreeningInfoDTO(List<ScreeningInfo> screeningInfoList) {
                if (!screeningInfoList.isEmpty()) {
                    ScreeningInfo firstScreeningInfo = screeningInfoList.get(0);
                    this.movieTitle = firstScreeningInfo.getMovie().getTitle();
                    this.movieInfo = firstScreeningInfo.getMovie().getInfo();
                    this.screeningList = screeningInfoList.stream()
                            .collect(Collectors.groupingBy(ScreeningInfo::getScreening))
                            .entrySet().stream()
                            .map(entry -> new ScreeningDTO(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                }
            }

            @Data
            private class ScreeningDTO {
                private String screeningName;
                private Integer screeningSeatCount;
                private String screeningRank;
                private List<ScreeningTimeDTO> screeningTimeList;

                public ScreeningDTO(Screening screening, List<ScreeningInfo> screeningInfoList) {
                    this.screeningName = screening.getName();
                    this.screeningSeatCount = screening.getSeatCount();
                    this.screeningRank = screening.getScreeningRankKo();
                    this.screeningTimeList = screeningInfoList.stream()
                            .map(ScreeningTimeDTO::new)
                            .collect(Collectors.toList());
                }

                @Data
                private class ScreeningTimeDTO {
                    private String startTime;
                    private Integer currentSeatCount;
                    private Integer screeningInfoId;

                    public ScreeningTimeDTO(ScreeningInfo screeningInfo) {
                        this.startTime = screeningInfo.getStartTime();
                        this.currentSeatCount = screeningInfo.getScreening().getSeatCount() - screeningInfo.getSeatBookList().size();
                        this.screeningInfoId = screeningInfo.getId();
                    }
                }
            }
        }
    }

    // 영화관 매출 DTO
    @Data
    @Builder
    public static class TheaterSalesDTO {
        private String areaName;
        private String theaterName;
        private String address;
        private String theaterTel;
        private String totalTheaterSales;
        private List<MovieTotalSalesDTO> movieSalesList;

        public TheaterSalesDTO(String areaName, String theaterName, String address, String theaterTel, String totalTheaterSales, List<MovieTotalSalesDTO> movieSalesList) {
            this.areaName = areaName;
            this.theaterName = theaterName;
            this.address = address;
            this.theaterTel = theaterTel;
            this.totalTheaterSales = totalTheaterSales;
            this.movieSalesList = movieSalesList;
        }

        @Data
        @Builder
        public static class MovieTotalSalesDTO {
            private int id;
            private String movieName;
            private Date startDate;
            private Date endDate;
            private String totalMovieSales;
            private String viewerCount;

            public MovieTotalSalesDTO(int id, String movieName, Date startDate, Date endDate, String totalMovieSales, String viewerCount) {
                this.id = id;
                this.movieName = movieName;
                this.startDate = startDate;
                this.endDate = endDate;
                this.totalMovieSales = totalMovieSales;
                this.viewerCount = viewerCount;
            }
        }
    }

    @Data
    public static class ScreeningAjaxDTO {
        private int screeningId;
        private String screeningName;
        private String screeningRank;
    }

    @Data
    public static class MovieAjaxDTO {
        private int movieId;
        private String movieName;
        private String movieInfo;
    }
}
