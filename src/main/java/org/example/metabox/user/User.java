package org.example.metabox.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.metabox.book.Book;
import org.example.metabox.movie_scrap.MovieScrap;
import org.example.metabox.review.Review;
import org.example.metabox.theater_scrap.TheaterScrap;
import org.hibernate.annotations.CreationTimestamp;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Entity
@Data
@Table(name = "user_tb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Book book;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieScrap> movieScrap;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TheaterScrap> theaterScrapList;

    private String nickname;
    private String imgFilename;
    private String name;
    private String birthear;
    private int point;
    @CreationTimestamp
    private LocalDateTime createdAt;
}