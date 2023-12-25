package com.yyh.xfs.im.vo;
import lombok.*;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2023-12-25
 * 消息前后端传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO implements Serializable {
    /**
     * 发送者id
     */
    private String from;
    /**
     * 发送者名称
     */
    private String fromName;
    /**
     * 发送者头像
     */
    private String fromAvatar;
    /**
     * 接收者id
     */
    private String to;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 发送时间
     */
    private String time;
    /**
     * 0:连接信息
     * 1:系统信息
     * 2:聊天信息
     * 3:添加好友信息
     */
    private Integer messageType;
    /**
     * 0:不是聊天信息
     * 1:文本信息
     * 2:图片信息
     * 3:文件信息
     */
    private Integer chatType;
    /**
     * 0:好友消息
     * 1:陌生人消息
     */
    private Integer friendType;
}
