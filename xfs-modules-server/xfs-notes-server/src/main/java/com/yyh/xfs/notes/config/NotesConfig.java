package com.yyh.xfs.notes.config;

import com.yyh.xfs.common.redis.constant.BloomFilterMap;
import com.yyh.xfs.common.redis.utils.BloomFilterUtils;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.notes.mapper.NotesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yyh
 * @date 2024-02-24
 */
@Configuration
public class NotesConfig {

    private final BloomFilterUtils bloomFilterUtils;

    private final NotesMapper notesMapper;

    public NotesConfig(BloomFilterUtils bloomFilterUtils, NotesMapper notesMapper) {
        this.bloomFilterUtils = bloomFilterUtils;
        this.notesMapper = notesMapper;
    }

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() {
        List<String> list = notesMapper.selectList(null).stream().map(notesDO -> String.valueOf(notesDO.getId())).collect(Collectors.toList());
        // 先判断有没有该布隆过滤器，没有则初始化
        long bloomFilterSize = bloomFilterUtils.getBloomFilterSize(BloomFilterMap.NOTES_ID_BLOOM_FILTER);
        if(bloomFilterSize==list.size()){
            return;
        }
        bloomFilterUtils.initBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, list.isEmpty() ? 10000 : list.size() * 4L, 0.01);
        bloomFilterUtils.addAllBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, list);
    }
}
