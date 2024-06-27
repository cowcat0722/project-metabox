package org.example.metabox.seat;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.metabox.screening.Screening;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "seat_tb")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Screening screening;

    // 좌석번호 ex : A9
    private String code;

    // 장애인석, 라이트석은 -1000원 할인
    // 장애인석(HANDICAPPED), 라이트석(LIGHT), 일반석(GENERAL), 모션베드(SPECIAL)
    @Enumerated(EnumType.STRING)
    private SeatType type;

    @OneToMany(mappedBy = "seat", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<SeatBook> seatBookList;

    @Builder
    public Seat(int id, Screening screening, String code, SeatType type, List<SeatBook> seatBookList) {
        this.id = id;
        this.screening = screening;
        this.code = code;
        this.type = type;
        this.seatBookList = seatBookList;
    }

    private enum SeatType {
        HANDICAPPED, GENERAL, LIGHT, SPECIAL
    }

    public String getSeatTypeKo() {
        String seatType = String.valueOf(this.type);
        if (seatType == "HANDICAPPED") {
            return "장애인석";
        } else if (seatType == "LIGHT") {
            return "라이트석";
        } else if (seatType == "GENERAL") {
            return "일반석";
        } else {
            return "모션베드";
        }
    }

    public int getSeatPrice() {
        String seatType = String.valueOf(this.getType());
        int price = this.getScreening().getSeatPrice();
        if (seatType == "HANDICAPPED") {
            price = price - 5000;
            return price;
        } else if (seatType == "LIGHT") {
            price = price - 1000;
            return price;
        } else { // general, special
            return price;
        }
    }
}
