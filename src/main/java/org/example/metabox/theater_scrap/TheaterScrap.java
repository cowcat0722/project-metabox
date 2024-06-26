package org.example.metabox.theater_scrap;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.metabox.theater.Theater;
import org.example.metabox.user.User;

@Entity
@Data
@NoArgsConstructor
@Table(name = "theater_scrap_tb")
public class TheaterScrap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Theater theater;

    @Builder
    public TheaterScrap(int id, User user, Theater theater) {
        this.id = id;
        this.user = user;
        this.theater = theater;
    }
}
