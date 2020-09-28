package com.justodit.service;

import com.justodit.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 数据统计 站点数据同居
 * @author liuxu
 */
@Service
public class DataService {


    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    /**
     * //将指定的ip计入UV  HyperLogLOg
     * @param ip
     */
    public void recordUV(String ip){
        String redisKey  = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);

    }

    /**
     *  //统计指定日志范围内的UV   HyperLogLOg
     * @param start
     * @param end
     * @return
     */
    public long calculateUV(Date start,Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){//时间不晚于end的时间
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);//加一天;
        }
        //合并数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());

        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size((redisKey));

    }

    /**
     *  //将指定用户计入DAU  日活跃用户 以userId为索引,存布尔值
     * @param userId
     */
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    /**
     * //统计指定日期范围内的DAU  只要当天访问了网站就算活跃用户
     * @param start
     * @param end
     * @return
     */
    public long calculateDAU(Date start,Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){//时间不晚于end的时间
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);//加一天;
        }

        //进行or运算
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start),df.format(end));//将起始结束时间作为key
                connection.bitOp(RedisStringCommands.BitOperation.OR,redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());//返回结果
            }
        });
    }
}
