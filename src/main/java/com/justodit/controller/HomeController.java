package com.justodit.controller;


import com.justodit.entity.DiscussPost;
import com.justodit.entity.Page;
import com.justodit.entity.User;
import com.justodit.service.DiscussPostService;
import com.justodit.service.LikeService;
import com.justodit.service.UserService;
import com.justodit.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements Constant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 主页
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String getIndex(Model model, Page page,@RequestParam(name = "orderMode",defaultValue = "0") int orderMode){

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPost(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost post : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                //查询赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);

            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    //跳转错误页面
    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    //拒绝访问时的提示页面,权限不足
    @RequestMapping(path = "/denied", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDeniedPage(){
        return "/error/404";
    }
}
