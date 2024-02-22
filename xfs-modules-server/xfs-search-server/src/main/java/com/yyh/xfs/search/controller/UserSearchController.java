package com.yyh.xfs.search.controller;

import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.search.domain.UserEsDO;
import com.yyh.xfs.search.service.UserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-21
 */
@RestController
@RequestMapping("/search/user")
public class UserSearchController {
    private final UserSearchService userSearchService;

    public UserSearchController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping("/getUser")
    public Result<List<UserEsDO>> getUser(String keyword, Integer page, Integer pageSize){
        if (page == null || pageSize == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return userSearchService.getUser(keyword, page, pageSize);
    }
}
