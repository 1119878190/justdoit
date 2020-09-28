package com.justodit.controller;

import com.justodit.entity.DiscussPost;
import com.justodit.entity.Page;
import com.justodit.service.ElasticSearchService;
import com.justodit.service.LikeService;
import com.justodit.service.UserService;
import com.justodit.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements Constant {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;



    /**
     * 关键词搜索  es
     * search?keyword=xxxx
     * @param keyword 关键词
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(value = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        //搜索贴子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                            elasticSearchService.searchDiscussPost(keyword,page.getCurrent()-1,page.getLimit());

        //聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (searchResult != null){
            for(DiscussPost post : searchResult){
              Map<String,Object> map = new HashMap<>();
              //贴子
                map.put("post",post);
                //作者
                map.put("user",userService.findUserById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        //分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchResult == null ? 0 : (int)searchResult.getTotalElements());

        return "/site/search";
    }



}
