package com.justodit.controller;
//评论
import com.justodit.entity.Comment;
import com.justodit.entity.DiscussPost;
import com.justodit.entity.Event;
import com.justodit.event.EventProducer;
import com.justodit.service.CommentService;
import com.justodit.service.DiscussPostService;
import com.justodit.util.Constant;
import com.justodit.util.HostHolder;
import com.justodit.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements Constant  {


    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加评论
     * @param discussPostId 贴子id
     * @param comment  评论
     * @return
     */
    @RequestMapping(value = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){

        comment.setUserId(hostHolder.getUser().getId());//评论人的id
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        if (comment.getTargetId()==null){
            comment.setTargetId(0);
        }
        commentService.addComment(comment);

        //触发评论事件  系统通知 kafka
        Event event = new Event()
                    .setTopic(TOPIC_COMMENT)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(comment.getEntityType())
                    .setEntityId(comment.getEntityId())
                    .setData("postId",discussPostId);//设置postId是为了在页面上跳转到评论的帖子
        //如果评论的是帖子  event中作者id为帖子的作者
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            //评论的是评论 event中作者的id是评论的id
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);


        if (comment.getEntityType() == ENTITY_TYPE_POST){
            //触发发帖事件,将发布的贴子存放到ElasticSearch中
             event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);


            //将贴子存放到redis中,计算帖子的分数  热帖排行
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);


        }


        //
        //跳转到帖子详情页面
        return "redirect:/discuss/detail/" + discussPostId;


    }

}
