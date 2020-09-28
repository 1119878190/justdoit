package com.justodit.service;


import com.justodit.controller.DiscussPostController;
import com.justodit.dao.DiscussPostMapper;
import com.justodit.entity.DiscussPost;
import com.justodit.util.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;


import java.util.List;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    //#缓存15个数据
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    //   存活时间,自动清除
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    //查询文章
    public List<DiscussPost> findDiscussPost(Integer userId , Integer offset, Integer limit,int orderMode){

        return discussPostMapper.selectDiscussPosts(userId,offset,limit, orderMode);
    }

    //查询总行数
    public  int findDiscussPostRows(Integer userId){
        return  discussPostMapper.selectDisCussPostRows(userId);
    }

    /**
     * 添加帖子
     * @param discussPost
     * @return
     */
    public int addDiscussPost(DiscussPost discussPost){
        if (discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //如果title和content中含有html标签,要转义
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //敏感词过滤
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        //插入数据
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 帖子详情
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(Integer id){
        return discussPostMapper.selectDiscussPostById(id);
    }


    /**
     * 更新帖子的评论数量
     * @param id  帖子的id
     * @param commentCount  评论数量
     * @return
     */
    public int updateCommentCount(Integer id,Integer commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    /**
     * 更新贴子类型
     * @param id
     * @param type   0-普通; 1-置顶;
     * @return
     */
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    /**
     * 更新贴子状态
     * @param id
     * @param status  0-正常; 1-精华; 2-拉黑
     * @return
     */
    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id, status);
    }


    /**
     * 更新贴子分数
     * @param id
     * @param score
     * @return
     */
    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }
}
