package com.yyh.xfs.comment.service.impl;

import com.yyh.xfs.comment.domain.CommentDO;
import com.yyh.xfs.comment.service.CommentService;
import com.yyh.xfs.comment.vo.CommentVO;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.utils.IPUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2024-02-05
 */
@Service
public class CommentServiceImpl implements CommentService {
    private final HttpServletRequest request;
    private final MongoTemplate mongoTemplate;

    public CommentServiceImpl(MongoTemplate mongoTemplate, HttpServletRequest request) {
        this.mongoTemplate = mongoTemplate;
        this.request = request;
    }

    @Override
    public Result<CommentVO> addComment(CommentDO commentDO) {
        commentDO.setCommentLikeNum(0);
        commentDO.setIsTop(false);
        commentDO.setCreateTime(System.currentTimeMillis());
        String ipAddr = IPUtils.getRealIpAddr(request);
        String addr = IPUtils.getAddrByIp(ipAddr);
        String address = IPUtils.provinceAddress(addr);
        if (StringUtils.hasText(address)) {
            commentDO.setProvince(address);
        } else {
            commentDO.setProvince("未知");
        }
        mongoTemplate.insert(commentDO);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(commentDO, commentVO);
        return ResultUtil.successPost(commentVO);
    }

    @Override
    public Result<Integer> getCommentCount(Long notesId) {
        Query query = new Query();
        query.addCriteria(new Criteria("notesId").is(notesId));
        long count = mongoTemplate.count(query, CommentDO.class);
        return ResultUtil.successGet((int) count);
    }

    @Override
    public Result<List<CommentVO>> getCommentFirstList(Long notesId, Integer page, Integer pageSize) {
        List<CommentDO> commentDOList = new ArrayList<>();
        // 若page为1，先找置顶评论
        if (page == 1) {
            Query query = new Query();
            query.addCriteria(new Criteria("notesId").is(notesId));
            query.addCriteria(new Criteria("isTop").is(true));
            query.addCriteria(new Criteria("parentId").is("0"));
            CommentDO topComment = mongoTemplate.findOne(query, CommentDO.class);
            if (topComment != null) {
                commentDOList.add(topComment);
            }
        }
        Query query = new Query();
        query.addCriteria(new Criteria("notesId").is(notesId));
        query.addCriteria(new Criteria("parentId").is("0"));
        query.addCriteria(new Criteria("isTop").is(false));
        query.skip((long) (page - 1) * pageSize);
        query.limit(pageSize);
        // 按commentLikeNum降序排序，若commentLikeNum相同则按createTime降序排序
        query.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
        commentDOList.addAll(mongoTemplate.find(query, CommentDO.class));
        List<CommentVO> commentVOList = commentDOList.stream().map(commentDO -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(commentDO, commentVO);
            Query query1 = new Query();
            query1.addCriteria(new Criteria("parentId").is(commentDO.getId()));
            query1.addCriteria(new Criteria("notesId").is(notesId));
            long count = mongoTemplate.count(query1, CommentDO.class);
            commentVO.setCommentReplyNum((int) count);
            return commentVO;
        }).collect(Collectors.toList());
        return ResultUtil.successGet(commentVOList);
    }
}
