package com.yyh.xfs.comment;

import com.yyh.xfs.comment.domain.CommentDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author yyh
 * @date 2024-02-05
 */
@SpringBootTest
public class CommentApplicationTest {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Test
    void test1() {
        CommentDO commentDO = new CommentDO();
        commentDO.setCommentUserId(234343242L);
        commentDO.setNotesId(234343242L);
        commentDO.setContent("test");
        mongoTemplate.insert(commentDO);
        System.out.println(commentDO.getId());
        System.out.println(commentDO);
    }
}
