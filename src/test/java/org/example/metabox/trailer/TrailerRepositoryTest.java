package org.example.metabox.trailer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class TrailerRepositoryTest {

    @Autowired
    private TrailerRepository trailerRepository;


    @Test
    public void findByTrailerId_test() {
        // given
        int trailerId = 1;

        // when
        Optional<Trailer> trailer = trailerRepository.findById(trailerId);

        // then
        trailer.ifPresent(value -> System.out.println("트레일러 데이터: " + value));
    }

    @Test
    public void findTopBookingRateMovieId_test(){
        // given

        // when
        Integer trailer = trailerRepository.findTopBookingRateMovieId();

        // then
        System.out.println("트레일러 " + trailer);

    }
}
