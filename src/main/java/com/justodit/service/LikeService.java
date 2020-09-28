package com.justodit.service;

import com.justodit.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞  并增加,减少用户的点赞数量
     *
     * @param userId       当前用户id
     * @param entityType   给点赞的类型 1为帖子   2为评论和回复
     * @param entityId     实体id
     * @param entityUserId 实体的作者id
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
       /* String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        //判断当前用户是否点赞  查看是否有userId
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember){
            //已点赞 取消
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        }else {
            //未点赞 点赞
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }*/
        redisTemplate.execute(new SessionCallback() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();//开启事务
                if (isMember) {
                    //已点赞  取消
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);//减少用户点赞数量
                } else {
                    //未点赞  点赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey); //增加用户点赞数量
                }


                return operations.exec();//执行事务
            }
        });

    }

    /**
     * 查询实体点赞的数量
     * @param entityType  给点赞的类型 1为帖子   2为评论和回复
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态  有没有点过赞
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1:0;  //1点赞  0未点赞
    }

    /**
     *  查询某个用户获得的赞
     * @param userId 用户id
     * @return
     */
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count  = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();

    }
}
