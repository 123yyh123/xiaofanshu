package com.yyh.xfs.third.sevice.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.common.web.exception.SystemException;
import com.yyh.xfs.third.config.AliyunOss;
import com.yyh.xfs.third.sevice.AliyunOssService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        OSS ossClient = getOssClient(file);
        try {
            String s = uploadAndCreateUrl(ossClient, file,".png");
            return ResultUtil.successPost(s);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.ALIYUN_OSS_INIT_ERROR, e);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public Result<List<String>> uploadImgs(MultipartFile[] file) {
        if(Objects.isNull(file)){
            return ResultUtil.errorPost("文件不能为空");
        }
        long sizeNum = 0;
        for (MultipartFile multipartFile : file) {
            long size = multipartFile.getSize();
            sizeNum += size;
            // aliyunOss.getImgMaxSize() 10MB
            String maxImageSize = aliyunOss.getMaxFileSize().substring(0, aliyunOss.getMaxFileSize().length() - 2);
            if(size > (long) Integer.parseInt(maxImageSize) * 1024 * 1024){
                throw new BusinessException(ExceptionMsgEnum.FILE_SIZE_TOO_LARGE);
            }
        }
        if(sizeNum > (long) Integer.parseInt(aliyunOss.getMaxRequestSize().substring(0, aliyunOss.getMaxRequestSize().length() - 2)) * 1024 * 1024){
            throw new BusinessException(ExceptionMsgEnum.FILE_SIZE_TOO_LARGE);
        }
        OSS ossClient = new OSSClientBuilder().build(aliyunOss.getEndpoint(), aliyunOss.getAccessKeyId(), aliyunOss.getAccessKeySecret());
        try {
            List<String> list = new ArrayList<>();
            for (MultipartFile multipartFile : file) {
                String s = uploadAndCreateUrl(ossClient, multipartFile,".png");
                list.add(s);
            }
            return ResultUtil.successPost(list);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.ALIYUN_OSS_INIT_ERROR, e);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public Result<String> uploadAudio(MultipartFile file) {
        OSS ossClient = getOssClient(file);
        try {
            String s = uploadAndCreateUrl(ossClient, file,".mp3");
            return ResultUtil.successPost(s);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.ALIYUN_OSS_INIT_ERROR, e);
        } finally {
            ossClient.shutdown();
        }
    }

    private String uploadAndCreateUrl(OSS ossClient,MultipartFile multipartFile,String suffix) {
        try {
            String replace = UUID.randomUUID().toString().replace("-", "");
            String substring = "";
            if(StringUtils.hasText(multipartFile.getOriginalFilename())&&multipartFile.getOriginalFilename().lastIndexOf(".") != -1){
                substring = multipartFile.getOriginalFilename().substring(0, multipartFile.getOriginalFilename().lastIndexOf("."));
            }
            String fileName = replace + substring+suffix;
            // 以当前年月日作为文件夹
            String folder = LocalDateTime.now().toString().substring(0, 10).replace("-", "/");
            String url = "https://" + aliyunOss.getBucketName() + "." + aliyunOss.getEndpoint() + "/" + folder + "/" + fileName;
            ossClient.putObject(aliyunOss.getBucketName(), folder + "/" + fileName, multipartFile.getInputStream());
            return url;
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.ALIYUN_OSS_INIT_ERROR, e);
        }
    }

    private OSS getOssClient(MultipartFile file){
        if(Objects.isNull(file)){
            throw new BusinessException(ExceptionMsgEnum.FILE_NOT_NULL);
        }
        long size = file.getSize();
        // aliyunOss.getImgMaxSize() 10MB
        String maxImageSize = aliyunOss.getMaxFileSize().substring(0, aliyunOss.getMaxFileSize().length() - 2);
        if(size > (long) Integer.parseInt(maxImageSize) * 1024 * 1024){
            throw new BusinessException(ExceptionMsgEnum.FILE_SIZE_TOO_LARGE);
        }
        return new OSSClientBuilder().build(aliyunOss.getEndpoint(), aliyunOss.getAccessKeyId(), aliyunOss.getAccessKeySecret());
    }
}
