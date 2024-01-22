package com.yyh.xfs.notes;

import com.yyh.xfs.notes.domain.NotesCategoryDO;
import com.yyh.xfs.notes.mapper.NotesCategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author yyh
 * @date 2024-01-22
 */
@SpringBootTest
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
}
