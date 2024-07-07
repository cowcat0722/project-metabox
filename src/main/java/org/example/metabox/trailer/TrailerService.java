package org.example.metabox.trailer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.example.metabox.movie.MovieQueryRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class TrailerService {

    private final Path videoLocation = Paths.get("./upload/");
    private final TrailerRepository trailerRepository;
    private final MovieQueryRepository movieQueryRepository;

    public Resource getVideoRes(String filename) {
        try {
            Path filePath = videoLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("파일을 불러오는 데 실패했습니다: " + filename, ex);
        }
    }


    //자동 재생
    @Transactional
    public Resource autoVideo() throws UnsupportedEncodingException {

        Integer topMovieId = movieQueryRepository.firstRankMovie();

        // 영화의 트레일러 찾기
        Trailer oneTrailer = trailerRepository.findById(topMovieId)
                .orElseThrow(() -> new RuntimeException("예매율 1위의 트레일러가 없습니다."));

        //DTO의 매핑하기
        TrailerResponse.TrailerDTO trailer = new TrailerResponse.TrailerDTO(oneTrailer.getMasterM3U8Filename(), oneTrailer.getId());

        // master.m3u8 파일 이름 가져오기
        String masterM3u8Filename = trailer.getMasterM3U8Filename();

        // URL 인코딩된 파일 이름 가져오기
        String encodedFilename = URLEncoder.encode(masterM3u8Filename, StandardCharsets.UTF_8.toString());

        // 리소스 가져오기
        Resource resource = getVideoRes(masterM3u8Filename);

        return resource;
    }


    public Trailer getTopTrailer() {
        Integer topMovieId = movieQueryRepository.firstRankMovie();

        // 영화의 트레일러 찾기
        Trailer oneTrailer = trailerRepository.findById(topMovieId)
                .orElseThrow(() -> new RuntimeException("예매율 1위의 트레일러가 없습니다."));

        return oneTrailer;
    }


}
