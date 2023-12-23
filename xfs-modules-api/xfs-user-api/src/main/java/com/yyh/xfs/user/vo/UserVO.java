package com.yyh.xfs.user.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yyh
 * @date 2023-12-11
 */
@Getter
@Setter
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String uid;
    private String nickname;
    private String avatarUrl;
    private Integer age;
    private Integer sex;
    private String area;
    private String birthday;
    private String selfIntroduction;
    private String homePageBackground;
    private String phoneNumber;

    public UserVO() {
    }
    /**
     * 从map中构造UserVO
     * @param map map
     */
    public UserVO(Map<String,Object> map){
        Object o = map.get("id");
        if(o!=null){
            this.id = Long.parseLong(o.toString());
        }
        o = map.get("uid");
        if(o!=null){
            this.uid = o.toString();
        }
        o = map.get("nickname");
        if(o!=null){
            this.nickname = o.toString();
        }
        o = map.get("avatarUrl");
        if(o!=null){
            this.avatarUrl = o.toString();
        }
        o = map.get("age");
        if(o!=null){
            this.age = Integer.parseInt(o.toString());
        }
        o=map.get("sex");
        if(o!=null){
            this.sex=Integer.parseInt(o.toString());
        }
        o=map.get("area");
        if(o!=null){
            this.area=o.toString();
        }
        o=map.get("birthday");
        if(o!=null){
            this.birthday=o.toString();
        }
        o=map.get("selfIntroduction");
        if(o!=null){
            this.selfIntroduction=o.toString();
        }
        o=map.get("homePageBackground");
        if(o!=null){
            this.homePageBackground=o.toString();
        }
        o=map.get("phoneNumber");
        if(o!=null){
            this.phoneNumber=o.toString();
        }
    }
    /**
     * 将UserVO转换为map
     * @param userVO userVO
     * @return map
     */
    public static Map<String, Object> toMap(UserVO userVO) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", userVO.getId());
        map.put("uid", userVO.getUid());
        map.put("nickname", userVO.getNickname());
        map.put("avatarUrl", userVO.getAvatarUrl());
        map.put("age", userVO.getAge());
        map.put("sex", userVO.getSex());
        map.put("area", userVO.getArea());
        map.put("birthday", userVO.getBirthday());
        map.put("selfIntroduction", userVO.getSelfIntroduction());
        map.put("homePageBackground", userVO.getHomePageBackground());
        map.put("phoneNumber", userVO.getPhoneNumber());
        return map;
    }
}
