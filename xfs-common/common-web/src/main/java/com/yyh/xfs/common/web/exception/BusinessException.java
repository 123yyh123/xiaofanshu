package com.yyh.xfs.common.web.exception;

import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author yyh
 * @date 2023-12-15
 * @desc 业务异常类
 */
@Getter
@Setter
@ToString
public class BusinessException extends RuntimeException{
    private ExceptionMsgEnum exceptionMsgEnum;
    public BusinessException(ExceptionMsgEnum exceptionMsgEnum) {
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
    public BusinessException(ExceptionMsgEnum exceptionMsgEnum,Throwable e){
        super(e);
        this.exceptionMsgEnum=exceptionMsgEnum;
    }

}
