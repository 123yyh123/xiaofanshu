package com.yyh.xfs.common.web.exception;

import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author yyh
 * @date 2023-12-23
 */
@Getter
@Setter
@ToString
public class OnlyWarnException extends RuntimeException{
    private ExceptionMsgEnum exceptionMsgEnum;
    public OnlyWarnException(ExceptionMsgEnum exceptionMsgEnum) {
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
    public OnlyWarnException(ExceptionMsgEnum exceptionMsgEnum,Throwable e){
        super(e);
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
}
