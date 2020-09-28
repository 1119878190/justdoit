package com.justodit.dao;

import com.justodit.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //查询帖子 分页查询    传入userid是为了以后可以根据id查询用户发了哪些帖子
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    //查询一共有多少条帖子
    //@Param 注解用于给参数取别名  如果只有一个参数,并且在<if>里使用,则必须加别名
    int selectDisCussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    //帖子详情
    DiscussPost selectDiscussPostById(Integer id);

    /**
     * 再插入评论的时候要更新帖子详情的评论数量
     * @param id  帖子的id
     * @param commentCount  要更新的评论数量
     * @return
     */
    int updateCommentCount(int id,int commentCount);

    /**
     * 修改帖子的类型
      * @param id
     * @param type  0-普通; 1-置顶
     * @return
     */
    int updateType(int id,int type);

    /**
     * 修改帖子的状态
     * @param id
     * @param status  0-正常; 1-精华; 2-拉黑;
     * @return
     */
    int updateStatus(int id,int status);

    /**
     *更新贴子分数
     * @param id
     * @param score
     * @return
     */
    int updateScore(int id,double score);
}
