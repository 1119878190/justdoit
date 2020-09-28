package com.justodit.quartz;

import com.justodit.entity.DiscussPost;
import com.justodit.service.DiscussPostService;
import com.justodit.service.ElasticSearchService;
import com.justodit.service.LikeService;
import com.justodit.util.Constant;
import com.justodit.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, Constant {


    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    //纪元
    private static final Date epoch;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;


    static{
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化纪元失败"+e);
        }
    }


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0){
            logger.info("任务取消,没有需要刷新的贴子");
            return;
        }

        logger.info("[任务开始,正在刷新贴子分数]:"+operations.size());

        while (operations.size() > 0){
            //刷新
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束,贴子分数刷新完毕]");
    }

    //刷新,更新数据库中score
    private void  refresh(int postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null){
            logger.error("该帖子不存在: id = "+post.getId());
            return;
        }

        //是否加精
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数  =  贴子权重   + 距离天数
        double score = Math.log10(Math.max(w,1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        //跟新贴子的分数
        discussPostService.updateScore(postId,score);
        //同步搜索数据  ElasticSearch
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }
}
