package org.example.metabox.user;

import lombok.Data;

public class UserRequest {

    // 유저 스크랩
    @Data
    public static class TheaterScrapDTO {
        private Integer theaterNameId;
        private Integer userId;

    }


    //비회원 회원가입
    @Data
    public static class JoinDTO {
        private String birth;
        private String password;
        private String phone;

        public JoinDTO(String birth, String password, String phone) {
            this.birth = birth;
            this.password = password;
            this.phone = phone;
        }
    }
}
