package com.yyh.xfs.search.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.Date;

/**
 * @author yyh
 * @date 2024-01-24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "notes")
public class NotesEsDO {

    @Id
    @Field(type = FieldType.Long)
    private Long id;
    /**
     * 笔记标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String title;

    /**
     * 笔记内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String content;

    /**
     * 所属用户id
     */
    @Field(type = FieldType.Long)
    private Long belongUserId;

    /**
     * 所属分类
     */
    @Field(type = FieldType.Integer)
    private Integer belongCategory;

    /**
     * 笔记类型，0为图片笔记，1为视频笔记
     */
    @Field(type = FieldType.Integer)
    private Integer notesType;

    /**
     * 封面图片
     */
    @Field(type = FieldType.Keyword)
    private String coverPicture;

    /**
     * 笔记资源，图片或视频，视频只能一个，图片最多9个，格式为json序列化数组后的字符串
     */
    @Field(type = FieldType.Keyword)
    private String notesResources;

    /**
     * 赞数
     */
    @Field(type = FieldType.Integer)
    private Integer notesLikeNum;

    /**
     * 收藏数
     */
    @Field(type = FieldType.Integer)
    private Integer notesCollectionNum;

    /**
     * 评论数
     */
    @Field(type = FieldType.Integer)
    private Integer notesCommentNum;

    /**
     * 展示的地点，可以为空
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String address;

    @GeoPointField
    private GeoPoint location;
    /**
     * 是否公开，0为公开，1为私密
     */
    @Field(type = FieldType.Integer)
    private Integer authority;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
