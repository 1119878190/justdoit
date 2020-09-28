package com.justodit.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JustDoItUtil {

    //生成随机字符串
    public static String generateUUId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密   数据库中有salt字段
    //hello  ->  abc123def456
    //hello  +   随机字符串  ->
    public static String md5(String key){
        //key为空或者为空格
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());

    }


    /**
     *获取Json格式的字符串  fastJson
     * @param code 编号
     * @param msg  提示
     * @param map  数据
     * @return
     */
    public static String getJsonString(int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null){
            for (String key : map.keySet()){
                json.put(key,map.get(key));
            }

        }
        return json.toJSONString();
    }

    public static String getJsonString(int code,String msg){
        return getJsonString(code,msg,null);
    }

    public static String getJsonString(int code){
        return getJsonString(code,null,null);
    }


    public static void main(String[] args) {

        Map<String,Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",25);
        System.out.println(getJsonString(0,"ok",map));
    }
}
