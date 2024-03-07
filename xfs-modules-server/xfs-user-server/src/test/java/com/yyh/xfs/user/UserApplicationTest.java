package com.yyh.xfs.user;

import com.alibaba.fastjson.JSON;
import com.yyh.xfs.user.domain.UserDO;
import com.yyh.xfs.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yyh
 * @date 2024-01-18
 */
@SpringBootTest
@Slf4j
public class UserApplicationTest {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private UserService userService;

    @Test
    void test3() {
        List<String> list = new ArrayList<>();
        list.add("你好");
        list.add("大家好");
        list.add("我们好");
        String jsonString = JSON.toJSONString(list);
        System.out.println(jsonString);
    }

    @Test
    void test4() {
//        List<UserDO> list = userService.list();
//        list.forEach(userDO -> {
//            rocketMQTemplate.convertAndSend("user-add-es-topic", JSON.toJSONString(userDO));
//            log.info("发送消息：{}", userDO);
//        });
    }

    @Test
    void test5() {
//        List<UserDO> list = userService.list();
//        List<String> avatarList = new ArrayList<>();
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%281%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%282%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%283%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%284%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%285%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%286%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%287%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%288%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%289%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%2810%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP-C%20%2811%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/OIP%20%281%29.jpg");
//        avatarList.add("https://xiaofanshu.oss-cn-hangzhou.aliyuncs.com/2023/12/23/%E5%A4%B4%E5%83%8F/%E4%B8%8B%E8%BD%BD.jpg");
//        ThreadLocalRandom random = ThreadLocalRandom.current();
//        list.forEach(userDO -> {
//            if (userDO.getId() == 1675532564583455936L || userDO.getId() == 1735294666611408897L) {
//                return;
//            }
//            UserDO userDO1 = new UserDO();
//            userDO1.setId(userDO.getId());
//            userDO1.setAvatarUrl(avatarList.get(random.nextInt(avatarList.size())));
//            userService.updateById(userDO1);
//            rocketMQTemplate.convertAndSend("user-update-es-topic", JSON.toJSONString(userDO1));
//            log.info("发送消息：{}", userDO1);
//        });
    }

    @Test
    void test6() {
//        List<UserDO> list = userService.list();
//        // 北京市下的所有区
//        List<String> districtList = new ArrayList<>();
//        Collections.addAll(districtList, "东城区", "西城区", "朝阳区", "丰台区", "石景山区", "海淀区", "门头沟区", "房山区", "通州区", "顺义区", "昌平区", "大兴区", "怀柔区", "平谷区", "密云区", "延庆区");
//        ThreadLocalRandom random = ThreadLocalRandom.current();
//        list.forEach(userDO -> {
//            if (userDO.getId() == 1675532564583455936L || userDO.getId() == 1735294666611408897L) {
//                return;
//            }
//            UserDO userDO1 = new UserDO();
//            userDO1.setId(userDO.getId());
//            String area = "北京市 "+ districtList.get(random.nextInt(districtList.size()));
//            userDO1.setArea(area);
//            userService.updateById(userDO1);
//        });
    }
}
