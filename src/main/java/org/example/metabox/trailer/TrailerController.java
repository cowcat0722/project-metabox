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


//    // 자동재생 (예매율 1위 영상)
//    @GetMapping("/api/getTopTrailerUrl")
//    public ResponseEntity<Resource> getTopTrailerUrl() throws IOException {
//        System.out.println("API 호출됨");
//
//        // 예매율 1위의 영화 가져오기
//        Resource resource = trailerService.autoVideo();
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
//                .body(resource);
//    }
//
//    // 비트레이트별 m3u8 파일 반환 (예매율 1위)
//    @GetMapping("/{bitrate}.m3u8")
//    public ResponseEntity<Resource> getTopTrailerBitrateM3U8(@PathVariable String bitrate) throws IOException {
//        System.out.println("비트레이트별 m3u8 API 호출됨: " + bitrate);
//
//        // 예매율 1위의 트레일러 정보 가져오기
//        Trailer topTrailer = trailerService.getTopTrailer();
//        System.out.println("트레일러 정보 가져오기 성공: " + topTrailer.getMasterM3U8Filename());
//
//        // 마스터 M3U8 파일 이름을 가져옴
//        String masterM3u8Filename = topTrailer.getMasterM3U8Filename();
//
//        // "_master.m3u8" 부분을 제거하여 파일 이름의 기본 부분만 가져옴
//        String baseFilename = masterM3u8Filename.replace("_master.m3u8", "");
//        System.out.println("기본 파일 이름: " + baseFilename);
//
//        // 비트레이트별 M3U8 파일 이름 생성
//        String bitrateM3u8Filename = baseFilename + "." + bitrate +"m3u8";
//        System.out.println("비트레이트별 파일 이름: " + bitrateM3u8Filename);
//
//        // 파일 이름을 사용하여 리소스를 가져옴
//        Resource resource = trailerService.getVideoRes(bitrateM3u8Filename);
//        System.out.println("리소스 가져오기 시도");
//
//        // 리소스가 존재하는지 확인
//        if (!resource.exists()) {
//            throw new RuntimeException("파일을 찾을 수 없습니다: " + bitrateM3u8Filename);
//        }
//
//        System.out.println("리소스 가져오기 성공: " + bitrateM3u8Filename);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
//                .body(resource);
//    }
//
//    // TS 파일들 반환 (예매율 1위)
//    @GetMapping("/segments/{filename:.+}")
//    public ResponseEntity<Resource> getTopTrailerSegmentFile(@PathVariable String filename) throws IOException {
//        // 예매율 1위의 트레일러 정보 가져오기
//        Trailer topTrailer = trailerService.getTopTrailer();
//
//        // 파일 이름을 URL 디코딩
//        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());
//        System.out.println("Decoded Filename: " + decodedFilename);
//
//        // 파일 이름을 사용하여 리소스를 가져옴
//        Resource resource = trailerService.getVideoRes(decodedFilename);
//
//        // 리소스가 존재하는지 확인
//        if (!resource.exists()) {
//            throw new RuntimeException("파일을 찾을 수 없습니다: " + decodedFilename);
//        }
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
//                .body(resource);
//    }





    //마스터 m3u8 반환
    @GetMapping("/{trailerId}/_master.m3u8")
    public ResponseEntity<Resource> getMasterM3U8(@PathVariable int trailerId) throws IOException {
        // 트레일러 정보를 데이터베이스에서 조회
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new RuntimeException("트레일러를 찾을 수 없습니다."));
        System.out.println(1);
        // 마스터 M3U8 파일 이름을 가져옴
        String masterM3u8Filename = trailer.getMasterM3U8Filename();
        System.out.println(2);

        // 파일 이름을 URL 인코딩
        String encodedFilename = URLEncoder.encode(masterM3u8Filename, StandardCharsets.UTF_8.toString());
        System.out.println(3);

        // 파일 이름을 사용하여 리소스를 가져옴
        Resource resource = trailerService.getVideoRes(masterM3u8Filename);
        System.out.println(4);

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
    public ResponseEntity<Resource> getTrailerSegmentFile(@PathVariable int trailerId, @PathVariable String filename) throws IOException {
        // 트레일러 정보를 데이터베이스에서 조회
        Trailer topTrailer = trailerService.getTopTrailer();

        // 파일 이름을 URL 디코딩
        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());
        System.out.println("Decoded Filename: " + decodedFilename);

        // 파일 이름을 사용하여 리소스를 가져옴
        Resource resource = trailerService.getVideoRes(decodedFilename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);
    }



}
