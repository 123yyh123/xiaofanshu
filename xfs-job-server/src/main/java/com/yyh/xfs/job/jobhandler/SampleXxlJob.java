package com.yyh.xfs.job.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yyh.xfs.common.redis.constant.RedisConstant;
import com.yyh.xfs.common.redis.utils.RedisCache;
import com.yyh.xfs.common.redis.utils.RedisKey;
import com.yyh.xfs.job.service.UserService;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2023-12-23
 * @desc xfs-job
 */
@Component
@Slf4j
public class SampleXxlJob {
    private final UserService userService;
    private final Executor jobThreadPool;

    private final RedisCache redisCache;

    public SampleXxlJob(RedisCache redisCache, Executor jobThreadPool, UserService userService) {
        this.redisCache = redisCache;
        this.jobThreadPool=jobThreadPool;
        this.userService = userService;
    }
    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("userInfoJobHandler")
    public void demoJobHandler() throws Exception {
        XxlJobHelper.log("start update userInfo");
        // 从redis中获取要更新用户的id
        long l = redisCache.zSetSize(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST);
        XxlJobHelper.log("update userInfo size:{}",l);
        BoundZSetOperations<String, Object> setOps = redisCache.boundZSetOps(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST);
        // 每个线程更新100个用户信息，更新完成后删除，放到线程池中执行，使用多线程更新
        for (int i = 0; i < l / 100 + 1; i++) {
            int finalI = i;
            jobThreadPool.execute(() -> {
                Set<Object> range = setOps.range(finalI, finalI + 100);
                if (range != null) {
                    List<String> collect = range.stream().map(o-> RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, o.toString())).collect(Collectors.toList());
                    List<Object> userInfoList = redisCache.pipelineGet(collect);
                    List<UserVO> userVos = userInfoList.stream().map(o -> {
                        Map<String, Object> map = (Map<String, Object>) o;
                        return new UserVO(map);
                    }).collect(Collectors.toList());
                    userService.updateByVos(userVos);
                    // 删除已经更新的用户
                    setOps.removeRange(finalI, finalI + 100);
                }
            });
        }
    }
}
