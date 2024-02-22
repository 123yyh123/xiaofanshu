package com.yyh.xfs.search.service;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.search.domain.UserEsDO;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-21
 */
public interface UserSearchService {
    Result<List<UserEsDO>> getUser(String keyword, Integer page, Integer pageSize);

    void addUser(UserEsDO userEsDO);

    void updateUser(UserEsDO userEsDO);
}
