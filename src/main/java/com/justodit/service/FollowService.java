package com.justodit.service;

import com.justodit.entity.User;
import com.justodit.util.Constant;
import com.justodit.util.RedisKeyUtil;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Redefinable;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanProperty;
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;


//关注 取关
@Service
public class FollowService implements Constant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     * @param userId   用户id
     * @param entityType  用户关注实体的类型 用户类型为3  Constant常量接口中设置了
     * @param entityId  用户关注实体的id
     */
    public void follow(Integer userId, Integer entityType, Integer entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);

                operations.multi();//启用事务

                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                return operations.exec();
            }
        });

    }


    /**
     * 取消关注
     * @param userId  用户id
     * @param entityType    实体类型  用户是3
     * @param entityId   实体id
     */
    public void unfollow(Integer userId,Integer entityType,Integer entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);

                operations.multi();//启用事务

                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerKey,userId);

                return operations.exec();
            }
        });

    }


    /**
     *  查询用户关注的实体的数量
     * @param userId  用户id
     * @param entityType  实体类型 用户类型为3
     * @return
     */
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询某个实体的粉丝数量
     * @param entityId  实体id
     * @param entityType  实体类型  用户为3
     * @return
     */
    public long findFollowerCount(int entityId,int entityType){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);

        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否已关注该实体  这里通过查询是否有score判断是否关注
     * @param userId  用户id
     * @param entityType   实体类型
     * @param entityId   实体id
     * @return
     */
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }

    /**
     * 查询某个用户关注的人 关注列表 分页
     * @param userId  用户id
     * @param offset  起始
     * @param limit  结束
     * @return
     */
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        Set<Integer> targetIds =  redisTemplate.opsForZSet().reverseRange(followeeKey,offset,offset+limit-1);//倒叙查询
        if (targetIds==null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);//原先存的score为当前时间
            map.put("followTime",new Date(score.longValue()));
            list.add(map);

        }
        return list;

    }

    /**
     * 查询某用户的粉丝 粉丝列表  分页
     * @param userId 用户id
     * @param offset  起始
     * @param limit  结束
     * @return
     */
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId:targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
