package com.justodit.dao;

import com.justodit.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {


    /**
     * 查询评论
     * @param entityType 评论类型
     * @param entityId  帖子的id
     * @param offset   开始
     * @param limit   结束
     * @return
     */
    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);


    /**
     * 帖子评论的数量
     * @param entityType  评论类型  评论or回复
     * @param entityId
     * @return
     */
    int selectCountByEntity(int entityType,int entityId);


    /**
     * 添加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
