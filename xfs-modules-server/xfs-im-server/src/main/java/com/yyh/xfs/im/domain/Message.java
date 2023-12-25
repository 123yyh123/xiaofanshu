package com.yyh.xfs.im.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;

/**
 * @author yyh
 * @date 2023-12-25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "message")
public class Message implements Serializable {
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
