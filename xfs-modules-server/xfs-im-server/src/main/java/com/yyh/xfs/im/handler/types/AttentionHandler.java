package com.yyh.xfs.im.handler.types;

import com.yyh.xfs.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static com.yyh.xfs.im.handler.IMServerHandler.USER_CHANNEL_MAP;

/**
 * @author yyh
 * @date 2024-01-16
 */
@Component
@Slf4j
public class AttentionHandler {
    private final ChatHandler chatHandler;

    public AttentionHandler(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    public void execute(MessageVO messageVO) {
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        chatHandler.sendMessage(channel, messageVO);
    }
}
