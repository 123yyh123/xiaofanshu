package com.yyh.xfs.notes;

import com.yyh.xfs.notes.domain.NotesCategoryDO;
import com.yyh.xfs.notes.mapper.NotesCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yyh
 * @date 2024-01-22
 */
@SpringBootTest
@Slf4j
public class NotesApplicationTest {
    @Autowired
    private NotesCategoryMapper notesCategoryMapper;

    @Test
    void test1() {
/*        1、国际 2、体育 3、娱乐 4、社会 5、财经 6、时事 7、科技 8、情感 9、汽车 10、教育 11、时尚 12、游戏 13、军事 14、旅游 15、美食
        16、文化 17、健康养生 18、搞笑 19、家居 20、动漫 21、宠物 22、母婴育儿 23、星座运势 24、历史 25、音乐 26、综合*/
/*        String[] categoryNames = {"国际", "体育", "娱乐", "社会", "财经", "时事", "科技", "情感", "汽车", "教育", "时尚", "游戏", "军事", "旅游", "美食", "文化", "健康养生", "搞笑", "家居", "动漫", "宠物", "母婴育儿", "星座运势", "历史", "音乐", "综合"};
        for (int i = 0; i < categoryNames.length; i++) {
            NotesCategoryDO notesCategoryDO = new NotesCategoryDO();
            notesCategoryDO.setId(i + 1);
            notesCategoryDO.setCategoryName(categoryNames[i]);
            notesCategoryDO.setCategorySort(0);
            notesCategoryMapper.insert(notesCategoryDO);
        }*/
    }

    @Test
    void test2() {
        String htmlContent = "<p>在一起的蛋仔打卡地图<a href=\"#{&quot;topicname&quot;:&quot;蛋仔派对&quot;}\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: rgb(69, 105, 215); text-decoration: none;\">#蛋仔派对#</a> <a href=\"#{&quot;userId&quot;:&quot;1675532564583455936&quot;}\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: rgb(69, 105, 215); text-decoration: none;\">@测试用户1</a> <img src=\"https://gitee.com/yyh12345678/image/raw/master/image/墨镜笑脸.gif\" alt=\"[墨镜笑脸XFS]\" height=\"25px\" width=\"25px\"></p>";
        List<String> topics = new ArrayList<>();
        Document document = Jsoup.parse(htmlContent);
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (href.contains("topicname")) {
                String topicName = href.substring(href.indexOf("topicname") + 12, href.indexOf("}") - 1);
                topics.add(topicName);
            }
        }
        System.out.println(topics);
    }

    @Test
    void test3() {
        List<Long> userIds = new ArrayList<>();
        String content = "<p>在一起的蛋仔打卡地图<a href=\"#"
                + "{&quot;topicname&quot;:&quot;蛋仔派对&quot;}"
                + "\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: rgb(69, 105, 215); text-decoration: none;\">#蛋仔派对#</a> "
                + "<a href=\"#"
                + "{&quot;userId&quot;:&quot;1675532564583455936&quot;}"
                + "\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: rgb(69, 105, 215); text-decoration: none;\">@测试用户1</a> "
                + "<img src=\"https://gitee.com/yyh12345678/image/raw/master/image/墨镜笑脸.gif\" alt=\"[墨镜笑脸XFS]\" height=\"25px\" width=\"25px\"></p>";
        Document document = Jsoup.parse(content);
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (href.contains("userId")) {
                String userId = href.substring(href.indexOf("userId") + 9, href.indexOf("}") - 1);
                userIds.add(Long.valueOf(userId));
            }
        }
        System.out.println(userIds);
    }

    @Test
    void test4() {
        long userId1 = 1675532564583455936L;
        long userId2 = 1000187117221975680L;
        log.info("userId1.hashCode() = {}", Long.hashCode(userId1));
        log.info("userId2.hashCode() = {}", Long.hashCode(userId2));
        log.info("userId1.hashCode() = {}", Long.hashCode(userId1) % 10);
        log.info("userId2.hashCode() = {}", Long.hashCode(userId2) % 10);
    }
}
