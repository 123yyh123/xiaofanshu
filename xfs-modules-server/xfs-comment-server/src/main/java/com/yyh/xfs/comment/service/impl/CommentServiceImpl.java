package com.yyh.xfs.comment.service.impl;

import com.yyh.xfs.comment.domain.CommentDO;
import com.yyh.xfs.comment.feign.UserFeign;
import com.yyh.xfs.comment.service.CommentService;
import com.yyh.xfs.comment.vo.CommentVO;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
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
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2024-02-05
 */
@Service
public class CommentServiceImpl implements CommentService {
    private final HttpServletRequest request;
    private final MongoTemplate mongoTemplate;
    private final UserFeign userFeign;
    private final RedisCache redisCache;
    private final Executor asyncThreadExecutor;

    public CommentServiceImpl(MongoTemplate mongoTemplate,
                              HttpServletRequest request,
                              UserFeign userFeign,
                              RedisCache redisCache,
                              Executor asyncThreadExecutor) {
        this.mongoTemplate = mongoTemplate;
        this.request = request;
        this.userFeign = userFeign;
        this.redisCache = redisCache;
        this.asyncThreadExecutor = asyncThreadExecutor;
    }

    @Override
    public Result<CommentVO> addComment(CommentDO commentDO) {
        commentDO.setCommentLikeNum(0);
        commentDO.setIsTop(false);
        commentDO.setIsHot(false);
        commentDO.setCreateTime(System.currentTimeMillis());
        String ipAddr = IPUtils.getRealIpAddr(request);
        String addr = IPUtils.getAddrByIp(ipAddr);
        String address = IPUtils.provinceAddress(addr);
        if (StringUtils.hasText(address)) {
            commentDO.setProvince(address);
        } else {
            commentDO.setProvince("未知");
        }
        if (commentDO.getReplyUserId() != 0) {
//            <a href="#{&quot;userId&quot;:&quot;1675532564583455936&quot;}" rel="noopener noreferrer" target="_blank" style="color: rgb(69, 105, 215); text-decoration: none;">@测试用户1</a>
            String prefix = "回复 <a href=\"#{" +
                    "&quot;userId&quot;:&quot;" + commentDO.getReplyUserId() + "&quot;" +
                    "}\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: #7d7d7d; text-decoration: none;\">" + commentDO.getReplyUserName() + "</a>：";
            // 添加到content的<p>的最前面
            if (commentDO.getContent().startsWith("<p>")) {
                commentDO.setContent("<p>" + prefix + commentDO.getContent().substring(3));
            } else {
                commentDO.setContent("<p>" + prefix + commentDO.getContent());
            }
        }
        mongoTemplate.insert(commentDO);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(commentDO, commentVO);
        Result<?> result = userFeign.getUserInfo(commentDO.getCommentUserId());
        if (result.getCode() == 20010) {
            Map<String, Object> userInfo = (Map<String, Object>) result.getData();
            commentVO.setCommentUserName((String) userInfo.get("nickname"));
            commentVO.setCommentUserAvatar((String) userInfo.get("avatarUrl"));
        } else {
            commentVO.setCommentUserName("用户已注销");
            commentVO.setCommentUserAvatar("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/00001.jpg");
        }
        commentVO.setCommentReplyNum(0);
        commentVO.setIsLike(false);
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
            Query query1 = new Query();
            query1.addCriteria(new Criteria("notesId").is(notesId));
            query1.addCriteria(new Criteria("parentId").is("0"));
            query1.addCriteria(new Criteria("isTop").is(false));
            query1.addCriteria(new Criteria("isHot").is(true));
            query1.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
            List<CommentDO> hotComment = mongoTemplate.find(query1, CommentDO.class);
            if (!hotComment.isEmpty()) {
                commentDOList.addAll(hotComment);
            }
            // 如果redis中有更新热门评论标识，不更新，没有则更新
            boolean b = redisCache.sHasKey(RedisConstant.REDIS_KEY_NOTES_COMMENT_HOT, notesId.toString());
            if (!b) {
                // 6小时更新一次
                Long l = redisCache.sSetAndTime(RedisConstant.REDIS_KEY_NOTES_COMMENT_HOT, 60 * 60 * 6, notesId.toString());
                // 若l为1，说明redis中没有该key，需要更新，防止并发，只有一个线程更新
                if (l == 1) {
                    // 线程池异步更新热门评论
                    asyncThreadExecutor.execute(() -> {
                        // 先将所有热门评论标识置为false
                        Query query2 = new Query();
                        query2.addCriteria(new Criteria("notesId").is(notesId));
                        query2.addCriteria(new Criteria("parentId").is("0"));
                        query2.addCriteria(new Criteria("isHot").is(true));
                        List<CommentDO> hot = mongoTemplate.find(query2, CommentDO.class);
                        if (!hot.isEmpty()) {
                            hot.forEach(commentDO -> {
                                commentDO.setIsHot(false);
                                mongoTemplate.save(commentDO);
                            });
                        }
                        // 再将commentLikeNum最高的8条评论置为热门评论
                        Query query3 = new Query();
                        query3.addCriteria(new Criteria("notesId").is(notesId));
                        query3.addCriteria(new Criteria("parentId").is("0"));
                        query3.addCriteria(new Criteria("isTop").is(false));
                        query3.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
                        query3.limit(8);
                        List<CommentDO> hot1 = mongoTemplate.find(query3, CommentDO.class);
                        if (!hot1.isEmpty()) {
                            hot1.forEach(commentDO -> {
                                commentDO.setIsHot(true);
                                mongoTemplate.save(commentDO);
                            });
                        }
                    });
                }
            }
        }
        Query query = new Query();
        query.addCriteria(new Criteria("notesId").is(notesId));
        query.addCriteria(new Criteria("parentId").is("0"));
        query.addCriteria(new Criteria("isTop").is(false));
        query.addCriteria(new Criteria("isHot").is(false));
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
            Result<?> result = userFeign.getUserInfo(commentDO.getCommentUserId());
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                commentVO.setCommentUserName((String) userInfo.get("nickname"));
                commentVO.setCommentUserAvatar((String) userInfo.get("avatarUrl"));
            } else {
                commentVO.setCommentUserName("用户已注销");
                commentVO.setCommentUserAvatar("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/00001.jpg");
            }
            String key = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE, commentDO.getId());
            String countKey = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_COUNT, commentDO.getId());
            commentVO.setIsLike(redisCache.sHasKey(key, commentDO.getCommentUserId()));
            if (redisCache.get(countKey) == null) {
                redisCache.set(countKey, commentDO.getCommentLikeNum().longValue());
            } else {
                commentVO.setCommentLikeNum(Integer.parseInt(redisCache.get(countKey).toString()));
            }
            return commentVO;
        }).collect(Collectors.toList());
        return ResultUtil.successGet(commentVOList);
    }

    @Override
    public Result<List<CommentVO>> getCommentSecondList(Long notesId, String parentId, Integer page, Integer pageSize) {
        List<CommentDO> commentDOList = new ArrayList<>();
        if (page == 1) {
            Query query = new Query();
            query.addCriteria(new Criteria("notesId").is(notesId));
            query.addCriteria(new Criteria("parentId").is(parentId));
            query.addCriteria(new Criteria("isHot").is(true));
            query.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
            List<CommentDO> hotComment = mongoTemplate.find(query, CommentDO.class);
            if (!hotComment.isEmpty()) {
                commentDOList.addAll(hotComment);
            }
        }
        Query query = new Query();
        query.addCriteria(new Criteria("parentId").is(parentId));
        query.addCriteria(new Criteria("notesId").is(notesId));
        query.addCriteria(new Criteria("isHot").is(false));
        query.skip((long) (page - 1) * pageSize);
        query.limit(pageSize);
        query.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
        commentDOList.addAll(mongoTemplate.find(query, CommentDO.class));
        List<CommentVO> commentVOList = commentDOList.stream().map(commentDO -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(commentDO, commentVO);
            Result<?> result = userFeign.getUserInfo(commentDO.getCommentUserId());
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                commentVO.setCommentUserName((String) userInfo.get("nickname"));
                commentVO.setCommentUserAvatar((String) userInfo.get("avatarUrl"));
            } else {
                commentVO.setCommentUserName("用户已注销");
                commentVO.setCommentUserAvatar("https://pmall-yyh.oss-cn-chengdu.aliyuncs.com/00001.jpg");
            }
            String key = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE, commentDO.getId());
            String countKey = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_COUNT, commentDO.getId());
            commentVO.setIsLike(redisCache.sHasKey(key, commentDO.getCommentUserId()));
            if (redisCache.get(countKey) == null) {
                redisCache.set(countKey, commentDO.getCommentLikeNum().longValue());
            } else {
                commentVO.setCommentLikeNum(Integer.parseInt(redisCache.get(countKey).toString()));
            }
            return commentVO;
        }).collect(Collectors.toList());
        return ResultUtil.successGet(commentVOList);
    }

    @Override
    public Result<Boolean> praiseComment(String commentId, Long userId, Long targetUserId) {
        String key = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE, commentId);
        String count = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_COUNT, commentId);
        if (redisCache.sHasKey(key, userId)) {
            redisCache.setRemove(key, userId);
            redisCache.decr(count, 1);
        } else {
            // 设置过期时间为7天，7天后自动删除，评论点赞集合并没有什么价值，不把它放到数据库中，用户一般不会回头看自己的评论点赞记录
            redisCache.sSetAndTime(key, 60 * 60 * 24 * 7, userId);
            redisCache.incr(count, 1);
        }
        // TODO 定时任务，每天凌晨0点将redis中的点赞数更新到数据库
        // TODO rocketMQ异步通知targetUserId用户
        return ResultUtil.successPost(true);
    }
}
