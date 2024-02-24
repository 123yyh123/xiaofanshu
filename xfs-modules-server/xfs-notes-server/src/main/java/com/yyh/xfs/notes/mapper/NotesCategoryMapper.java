package com.yyh.xfs.notes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.notes.domain.NotesCategoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Mapper
public interface NotesCategoryMapper extends BaseMapper<NotesCategoryDO> {

    /**
     * 获取笔记分类，去除id为1的默认分类，按照category_sort排序
     * @return 笔记分类
     */
    @Select("select * from notes_category where id != 1 order by category_sort")
    List<NotesCategoryDO> getNotesCategory();
}
