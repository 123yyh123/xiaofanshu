package com.yyh.xfs.search.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author yyh
 * @date 2024-02-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user")
public class UserEsDO {
    @Id
    @Field(type = FieldType.Long)
    private Long id;

    /**
     * 用户名
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String nickname;

    /**
     * 用户头像
     */
    @Field(type = FieldType.Text)
    private String avatarUrl;

    /**
     * 用户小番薯号
     */
    @Field(type = FieldType.Text)
    private String uid;


}
