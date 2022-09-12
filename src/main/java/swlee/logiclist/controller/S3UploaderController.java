package swlee.logiclist.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import swlee.logiclist.utils.S3UploaderService;

import java.io.IOException;
@Slf4j
@RequiredArgsConstructor
@Controller
public class S3UploaderController {
    private final S3UploaderService s3UploaderService;

    @GetMapping("/image")
    public String image() {
        return "image-upload";
    }
//
//    @GetMapping("/video")
//    public String video() {
//        return "video-upload";
//    }

    @PostMapping("/image-upload")
    @ResponseBody
    public String imageUpload(@RequestParam("data") MultipartFile multipartFile) throws IOException {
        log.info("imageUpload IN");
        return s3UploaderService.upload(multipartFile, "logiclist", "image");
    }

//    @PostMapping("/video-upload")
//    @ResponseBody
//    public String videoUpload(@RequestParam("data") MultipartFile multipartFile) throws IOException {
//        return s3UploaderService.upload(multipartFile, "logiclist", "video");
//    }
}