package com.yyh.xfs.third.sevice.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.SystemException;
import com.yyh.xfs.third.config.AliyunOss;
import com.yyh.xfs.third.sevice.AliyunOssService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author yyh
 * @date 2023-12-21
 */
@Service
public class AliyunOssServiceImpl implements AliyunOssService {

    private final AliyunOss aliyunOss;

    public AliyunOssServiceImpl(AliyunOss aliyunOss) {
        this.aliyunOss = aliyunOss;
    }

    @Override
    public Result<String> uploadImg(MultipartFile file) {
        OSS ossClient = new OSSClientBuilder().build(aliyunOss.getEndpoint(), aliyunOss.getAccessKeyId(), aliyunOss.getAccessKeySecret());
        String replace = UUID.randomUUID().toString().replace("-", "");
        String fileName = replace + file.getOriginalFilename();
        // 以当前年月日作为文件夹
        String folder = LocalDateTime.now().toString().substring(0, 10).replace("-", "/");
        String url = "https://" + aliyunOss.getBucketName() + "." + aliyunOss.getEndpoint() + "/" + folder + "/" + fileName;
        try {
            ossClient.putObject(aliyunOss.getBucketName(), folder + "/" + fileName, file.getInputStream());
            return ResultUtil.successPost(url);
        } catch (Exception e) {
           throw new SystemException(ExceptionMsgEnum.ALIYUN_OSS_INIT_ERROR, e);
        } finally {
            ossClient.shutdown();
        }
    }
}
