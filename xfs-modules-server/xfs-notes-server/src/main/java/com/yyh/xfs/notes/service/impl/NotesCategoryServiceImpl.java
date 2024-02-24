package com.yyh.xfs.notes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.notes.domain.NotesCategoryDO;
import com.yyh.xfs.notes.mapper.NotesCategoryMapper;
import com.yyh.xfs.notes.service.NotesCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yyh
 * @date 2024-02-23
 */
@Service
public class NotesCategoryServiceImpl implements NotesCategoryService {
    private final NotesCategoryMapper notesCategoryMapper;

    public NotesCategoryServiceImpl(NotesCategoryMapper notesCategoryMapper) {
        this.notesCategoryMapper = notesCategoryMapper;
    }

    @Override
    public Result<List<NotesCategoryDO>> getNotesCategory() {
        List<NotesCategoryDO> notesCategoryList = notesCategoryMapper.getNotesCategory();
        return ResultUtil.successGet(notesCategoryList);
    }
}
