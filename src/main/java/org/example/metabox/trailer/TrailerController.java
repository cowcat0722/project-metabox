package org.example.metabox.trailer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.metabox._core.errors.exception.Exception404;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class TrailerController {

    private final TrailerRepository trailerRepository;
    private final TrailerService trailerService;


    //마스터 m3u8 반환
    @GetMapping("/{trailerId}/_master.m3u8")
    public ResponseEntity<Resource> getMasterM3U8(@PathVariable int trailerId) throws IOException {
        // 트레일러 정보를 데이터베이스에서 조회
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new RuntimeException("트레일러를 찾을 수 없습니다."));

        // 마스터 M3U8 파일 이름을 가져옴
        String masterM3u8Filename = trailer.getMasterM3U8Filename();

        // 파일 이름을 URL 인코딩
        String encodedFilename = URLEncoder.encode(masterM3u8Filename, StandardCharsets.UTF_8.toString());


        // 파일 이름을 사용하여 리소스를 가져옴
        Resource resource = trailerService.getVideoRes(masterM3u8Filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    // 비트레이트별 m3u8파일 반환
    @GetMapping("/{trailerId}/{bitrate}.m3u8")
    public ResponseEntity<Resource> getBitrateM3U8(@PathVariable int trailerId, @PathVariable String bitrate) throws IOException {
        // 트레일러 정보를 데이터베이스에서 조회
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new RuntimeException("트레일러를 찾을 수 없습니다."));

        // 비트레이트별 M3U8 파일 이름 생성
        String bitrateM3u8Filename = bitrate + ".m3u8";

        // 파일 이름을 사용하여 리소스를 가져옴
        Resource resource = trailerService.getVideoRes(bitrateM3u8Filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }


    //ts 파일들 반환
    @GetMapping("/{trailerId}/{filename:.+}")
    public ResponseEntity<Resource> getSegmentFile(@PathVariable int trailerId, @PathVariable String filename) throws IOException {
        // 트레일러 정보를 데이터베이스에서 조회
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new RuntimeException("트레일러를 찾을 수 없습니다."));

        // 파일 이름을 URL 디코딩
        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());

        // 파일 이름을 사용하여 리소스를 가져옴
        Resource resource = trailerService.getVideoRes(decodedFilename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);
    }



}
