package com.yyh.xfs.common.web.exception;

import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author yyh
 * @date 2023-12-15
 */
@Getter
@Setter
@ToString
public class SystemException extends RuntimeException{
    private ExceptionMsgEnum exceptionMsgEnum;
    public SystemException(ExceptionMsgEnum exceptionMsgEnum) {
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
    public SystemException(ExceptionMsgEnum exceptionMsgEnum,Throwable e){
        super(e);
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
}
