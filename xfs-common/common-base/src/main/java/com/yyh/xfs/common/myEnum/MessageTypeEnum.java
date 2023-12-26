package com.yyh.xfs.common.myEnum;

import lombok.Getter;
import lombok.ToString;

/**
 * @author yyh
 * @date 2023-12-25
 */
@Getter
@ToString
public class MessageTypeEnum {
    /**
     * 0:连接信息
     * 1:心跳信息
     * 2:系统信息
     * 3:聊天信息
     * 4:添加好友信息
     */
    public static final Integer CONNECT_MESSAGE = 0;
    public static final Integer HEART_MESSAGE = 1;
    public static final Integer SYSTEM_MESSAGE = 2;
    public static final Integer CHAT_MESSAGE = 3;
    public static final Integer ADD_FRIEND_MESSAGE = 4;
    /**
     * 0:不是聊天信息
     * 1:文本信息
     * 2:图片信息
     * 3:文件信息
     */
    public static final Integer NOT_CHAT_MESSAGE = 0;
    public static final Integer TEXT_MESSAGE = 1;
    public static final Integer IMAGE_MESSAGE = 2;
    public static final Integer FILE_MESSAGE = 3;
    /**
     * 0:好友消息
     * 1:陌生人消息
     */
    public static final Integer FRIEND_MESSAGE = 0;
    public static final Integer STRANGER_MESSAGE = 1;
}
