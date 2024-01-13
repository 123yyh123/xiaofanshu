package com.yyh.xfs.third.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.third.sevice.AliyunOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author yyh
 * @date 2023-12-21
 */
@RestController
@RequestMapping("/third")
public class UploadFileController {

    private final AliyunOssService aliyunOssService;

    public UploadFileController(AliyunOssService aliyunOssService) {
        this.aliyunOssService = aliyunOssService;
    }

    @PostMapping("/uploadImg")
    public Result<String> uploadImg(@RequestParam("file") MultipartFile file) {
        return aliyunOssService.uploadImg(file);
    }

    @PostMapping("/uploadImgs")
    public Result<List<String>> uploadImgs(@RequestParam("file") MultipartFile[] file) {
        return aliyunOssService.uploadImgs(file);
    }

    @PostMapping("/uploadAudio")
    public Result<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        return aliyunOssService.uploadAudio(file);
    }
}
