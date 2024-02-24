package com.yyh.xfs.job.mapper.user;

import com.yyh.xfs.user.domain.UserDO;
import io.lettuce.core.ScanIterator;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-09
 */
public interface UserMapper{
    void updateUser(UserDO user);

    List<Long> getAllUserId();
}




