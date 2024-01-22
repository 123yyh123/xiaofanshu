package com.yyh.xfs.notes.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.xfs.common.domain.Result;
import com.yyh.xfs.common.myEnum.ExceptionMsgEnum;
import com.yyh.xfs.common.utils.ResultUtil;
import com.yyh.xfs.common.web.exception.BusinessException;
import com.yyh.xfs.notes.domain.NotesCategoryDO;
import com.yyh.xfs.notes.domain.NotesDO;
import com.yyh.xfs.notes.domain.NotesTopicDO;
import com.yyh.xfs.notes.domain.NotesTopicRelationDO;
import com.yyh.xfs.notes.mapper.NotesCategoryMapper;
import com.yyh.xfs.notes.mapper.NotesMapper;
import com.yyh.xfs.notes.mapper.NotesTopicMapper;
import com.yyh.xfs.notes.mapper.NotesTopicRelationMapper;
import com.yyh.xfs.notes.service.NotesService;
import com.yyh.xfs.notes.utils.NotesUtils;
import com.yyh.xfs.notes.vo.NotesVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yyh
 * @date 2024-01-22
 */
@Service
@Slf4j
public class NotesServiceImpl extends ServiceImpl<NotesMapper, NotesDO> implements NotesService {
    private final RocketMQTemplate rocketMQTemplate;

    private final NotesCategoryMapper notesCategoryMapper;
    private final NotesTopicRelationMapper notesTopicRelationMapper;
    private final NotesTopicMapper notesTopicMapper;

    public NotesServiceImpl(NotesTopicMapper notesTopicMapper, NotesTopicRelationMapper notesTopicRelationMapper, NotesCategoryMapper notesCategoryMapper, RocketMQTemplate rocketMQTemplate) {
        this.notesTopicMapper = notesTopicMapper;
        this.notesTopicRelationMapper = notesTopicRelationMapper;
        this.notesCategoryMapper = notesCategoryMapper;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public Result<?> addNotes(NotesVO notesVO) {
        log.info("notesVO:{}", notesVO);
        List<String> notesResources = JSON.parseObject(notesVO.getNotesResources(), List.class);
        log.info("notesResources:{}", notesResources);
        notesParameterCheck(notesVO, notesResources);
        // 设置封面图片
        if (notesVO.getNotesType() == 0) {
            // 图片笔记
            notesVO.setCoverPicture(notesResources.get(0));
        } else {
            if (!StringUtils.hasText(notesVO.getCoverPicture())) {
                notesVO.setCoverPicture(notesResources.get(0) + "?x-oss-process=video/snapshot,t_0,f_jpg,w_0,h_0,m_fast");
            }
        }
        log.info("coverPicture:{}", notesVO.getCoverPicture());
        NotesDO notesDO = new NotesDO();
        BeanUtils.copyProperties(notesVO, notesDO);
        // 利用百度AI接口进行文章分类
        String category = NotesUtils.createCategory(notesVO.getTitle(), notesVO.getRealContent());
        log.info("category:{}", category);
        NotesCategoryDO notesCategoryDO = notesCategoryMapper.selectOne(new QueryWrapper<NotesCategoryDO>().lambda().eq(NotesCategoryDO::getCategoryName, category));
        if (Objects.isNull(notesCategoryDO)) {
            // 默认分类
            notesDO.setBelongCategory(27);
        } else {
            notesDO.setBelongCategory(notesCategoryDO.getId());
        }
        // 保存笔记
        this.baseMapper.insert(notesDO);
        // TODO 利用rocketMQ异步将笔记保存到ES中

        // 找到所有的"#话题#"
        List<String> topics = findTopic(notesVO);
        log.info("topics:{}", topics);
        // 查询话题是否存在，不存在则创建
        topics.forEach(topic -> {
            NotesTopicDO notesTopicDO = notesTopicMapper.selectOne(new QueryWrapper<NotesTopicDO>().lambda().eq(NotesTopicDO::getTopicName, topic));
            if (Objects.isNull(notesTopicDO)) {
                notesTopicDO = new NotesTopicDO();
                notesTopicDO.setTopicName(topic);
                notesTopicDO.setCreateUser(notesVO.getBelongUserId());
                notesTopicMapper.insert(notesTopicDO);
                log.info("notesTopicDO:{}", notesTopicDO);
            }
            NotesTopicRelationDO notesTopicRelationDO = new NotesTopicRelationDO();
            notesTopicRelationDO.setNotesId(notesDO.getId());
            notesTopicRelationDO.setTopicId(notesTopicDO.getId());
            notesTopicRelationMapper.insert(notesTopicRelationDO);
        });
        // 找到所有的"@人"，并提取出人的id，发送通知
        List<Long> userIds = findUserId(notesVO);
        log.info("userIds:{}", userIds);
        // TODO 利用rocketMQ异步发送通知
        userIds.forEach(userId -> {
            Map<String,Object> map=new HashMap<>();
            map.put("belongUserId",notesVO.getBelongUserId().toString());
            map.put("toUserId",userId.toString());
            map.put("coverPicture",notesVO.getCoverPicture());
            rocketMQTemplate.asyncSend("notes-remind-target-topic", JSON.toJSONString(map), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("发送通知成功");
                }
                @Override
                public void onException(Throwable throwable) {
                    log.error("发送通知失败", throwable);
                }
            });
        });
        return ResultUtil.successPost(null);
    }

    private List<Long> findUserId(NotesVO notesVO) {
        List<Long> userIds = new ArrayList<>();
        Document document = Jsoup.parse(notesVO.getContent());
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (href.contains("userId")) {
                String userId = href.substring(href.indexOf("userId") + 9, href.indexOf("}") - 1);
                userIds.add(Long.valueOf(userId));
            }
        }
        return userIds;
    }

    private List<String> findTopic(NotesVO notesVO) {
        List<String> topics = new ArrayList<>();
        Document document = Jsoup.parse(notesVO.getContent());
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (href.contains("topicname")) {
                String topicName = href.substring(href.indexOf("topicname") + 12, href.indexOf("}") - 1);
                topics.add(topicName);
            }
        }
        return topics;
    }

    private void notesParameterCheck(NotesVO notesVO, List<String> notesResources) {
        if (notesResources.isEmpty()) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (notesVO.getNotesType() == 0 && notesResources.size() > 9) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (notesVO.getNotesType() == 1 && notesResources.size() > 1) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (!StringUtils.hasText(notesVO.getTitle())) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
    }
}
