package org.example.metabox.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SessionAdmin {

    private Integer id;

    private String loginId;

    public SessionAdmin(Integer id, String loginId) {
        this.id = id;
        this.loginId = loginId;
    }
}