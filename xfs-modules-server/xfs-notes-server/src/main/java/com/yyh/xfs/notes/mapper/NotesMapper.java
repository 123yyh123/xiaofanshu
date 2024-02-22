package com.yyh.xfs.notes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.xfs.notes.domain.NotesDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author yyh
 * @date 2024-01-19
 */
@Mapper
public interface NotesMapper extends BaseMapper<NotesDO> {

    List<NotesDO> selectPageByTime(Integer offset, Integer pageSize);

    List<NotesDO> selectPageByUserId(Integer offset, Integer pageSize, Long userId, Integer authority);

    List<NotesDO> selectPageByUserIdAndLike(Integer offset, Integer pageSize, Long userId);

    List<NotesDO> selectPageByUserIdAndCollect(Integer offset, Integer pageSize, Long userId);

    Integer getPraiseCountByUserId(Long userId);

    Integer getCollectCountByUserId(Long userId);

    List<NotesDO> selectPageByAttentionUserId(Integer offset, Integer pageSize, List<Long> attentionUserId);
}
