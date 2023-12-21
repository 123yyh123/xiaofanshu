package com.yyh.xfs.third.sevice;

import com.yyh.xfs.common.domain.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yyh
 * @date 2023-12-21
 */
public interface AliyunOssService {
    Result<String> uploadImg(MultipartFile file);
}
