package com.justodit.controller;

import ch.qos.logback.core.joran.conditional.ThenOrElseActionBase;
import com.justodit.annotation.LoginRequired;
import com.justodit.entity.Event;
import com.justodit.entity.Page;
import com.justodit.entity.User;
import com.justodit.event.EventProducer;
import com.justodit.service.FollowService;
import com.justodit.service.UserService;
import com.justodit.util.Constant;
import com.justodit.util.HostHolder;
import com.justodit.util.JustDoItUtil;
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements Constant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注
     * @param entityType  关注的实体类型 这里常量接口中设置了默认为3
     * @param entityId  关注用户的id
     * @return Json
     */
    @RequestMapping(value = "/follow",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(Integer entityType,Integer entityId){
        User user = hostHolder.getUser();

        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件  系统通知
        Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityId(entityId)
                    .setEntityType(entityType)
                    .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return JustDoItUtil.getJsonString(0,"已关注");
    }

    /**
     * 取消关注
     * @param entityType  取消关注的实体类型
     * @param entityId  取消关注的实体id
     * @return  Json
     */
    @RequestMapping(value = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String unfollow(Integer entityType, Integer entityId){
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(),entityType,entityId);

        return JustDoItUtil.getJsonString(0,"已取消关注");
    }

    /**
     * 关注列表
     * @param userId  用户id
     * @param page   分页
     * @param model
     * @return
     */
    @RequestMapping(value = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存咋");
        }
        model.addAttribute("user",user);

        //分页信息
        page.setLimit(5);
        page.setPath("/followees/"+ userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        //查询关注列表
        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if (userList != null){
            for (Map<String,Object> map:userList){
                User u = (User) map.get("user");
                //判断当前登录用户是否关注该用户
                map.put("hasFollowed",hasFollowed(u.getId()));

            }
        }
        model.addAttribute("users",userList);

        return "/site/followee";
    }


    /**
     * 粉丝列表
     * @param userId  用户id
     * @param page   分页
     * @param model
     * @return
     */
    @RequestMapping(value = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存咋");
        }
        model.addAttribute("user",user);

        //分页信息
        page.setLimit(5);
        page.setPath("/followers/"+ userId);
        page.setRows((int) followService.findFollowerCount(userId,ENTITY_TYPE_USER));

        //查询关注列表
        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if (userList != null){
            for (Map<String,Object> map : userList){
                User u = (User) map.get("user");
                //判断当前登录用户是否关注该用户
                map.put("hasFollowed",hasFollowed(u.getId()));

            }
        }
        model.addAttribute("users",userList);

        return "/site/follower";
    }

    //判断当前用户关注的状态
    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);

    }

}
