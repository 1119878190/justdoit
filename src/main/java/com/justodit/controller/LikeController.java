package com.justodit.controller;
//点赞
import com.justodit.entity.Event;
import com.justodit.entity.User;
import com.justodit.event.EventProducer;
import com.justodit.service.LikeService;
import com.justodit.util.Constant;
import com.justodit.util.HostHolder;
import com.justodit.util.JustDoItUtil;
import com.justodit.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements Constant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 点赞
     * @param entityType
     * @param entityId
     * @param entityUserId
     * @param postId
     * @return
     */
    @RequestMapping(value = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = hostHolder.getUser();

        //点赞 并增加,减少用户的点赞数量
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        //返回结果
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        //触发点赞事件  点赞才通知  取消赞不通知
        if (likeStatus == 1){
            Event event = new Event()
                        .setTopic(TOPIC_LIKE)
                        .setUserId(hostHolder.getUser().getId())
                        .setEntityId(entityId)
                        .setEntityType(entityType)
                        .setEntityUserId(entityUserId)
                        .setData("postId",postId);//设置PostId是为了页面上跳转到被点赞的帖子详情页面
            eventProducer.fireEvent(event);
        }

        //对贴子点赞
        if (entityType == ENTITY_TYPE_POST){
            //将贴子存放到redis中,计算帖子的分数  热帖排行
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        return JustDoItUtil.getJsonString(0,null,map);
    }

}
