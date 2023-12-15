package com.yyh.xfs.third.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.third.sevice.AliyunSmsService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyh
 * @date 2023-12-12
 */
@RestController
@RequestMapping("/third")
public class MessageController {

    private final AliyunSmsService aliyunSmsService;

    public MessageController(AliyunSmsService aliyunSmsService) {
        this.aliyunSmsService = aliyunSmsService;
    }


    @GetMapping("/sendBindPhoneSms")
    public Result<?> sendBindPhoneSms(String phoneNumber) {
        if(!StringUtils.hasText(phoneNumber)){
            return ResultUtil.errorGet("手机号不能为空");
        }
        if(!phoneNumber.matches("^1[3-9]\\d{9}$")){
            return ResultUtil.errorGet("手机号格式不正确");
        }
        return aliyunSmsService.sendBindPhoneSms(phoneNumber);
    }
    @GetMapping("/sendResetPhoneSms")
    public Result<?> sendResetPhoneSms(String phoneNumber) {
        if(!StringUtils.hasText(phoneNumber)){
            return ResultUtil.errorGet("手机号不能为空");
        }
        if(!phoneNumber.matches("^1[3-9]\\d{9}$")){
            return ResultUtil.errorGet("手机号格式不正确");
        }
        return aliyunSmsService.sendResetPhoneSms(phoneNumber);
    }
}


//        java.util.List<String> args = java.util.Arrays.asList(args_);
//        // 请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
//        // 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例使用环境变量获取 AccessKey 的方式进行调用，仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
//        com.aliyun.dysmsapi20170525.Client client = Sample.createClient(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
//        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
//                .setSignName("阿里云短信测试")
//                .setTemplateCode("SMS_154950909")
//                .setPhoneNumbers("16696687008")
//                .setTemplateParam("{\"code\":\"1234\"}");
//        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
//        try {
//            // 复制代码运行请自行打印 API 的返回值
//            client.sendSmsWithOptions(sendSmsRequest, runtime);
//        } catch (TeaException error) {
//            // 错误 message
//            System.out.println(error.getMessage());
//            // 诊断地址
//            System.out.println(error.getData().get("Recommend"));
//            com.aliyun.teautil.Common.assertAsString(error.message);
//        } catch (Exception _error) {
//            TeaException error = new TeaException(_error.getMessage(), _error);
//            // 错误 message
//            System.out.println(error.getMessage());
//            // 诊断地址
//            System.out.println(error.getData().get("Recommend"));
//            com.aliyun.teautil.Common.assertAsString(error.message);
//        }
//}
