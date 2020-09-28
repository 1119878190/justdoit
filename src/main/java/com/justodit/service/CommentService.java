package com.justodit.service;

import com.justodit.dao.CommentMapper;
import com.justodit.entity.Comment;
import com.justodit.entity.DiscussPost;
import com.justodit.util.Constant;
import com.justodit.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService  implements Constant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 查询帖子的评论
     * @param entityType  类型 帖子的评论1or评论的回复2
     * @param entityId   帖子id
     * @param offset    起始
     * @param limit      多少条
     * @return
     */
    public List<Comment> findCommentByEntity(Integer entityType,Integer entityId,Integer offset ,Integer limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    /**
     *  评论的数量
     * @param entityType  类型  评论or回复
     * @param entityId   帖子id
     * @return
     */
    public int findCommentCount(Integer entityType,Integer entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    /**
     * 添加评论
     * @param comment  评论实体
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){

        if (comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //添加评论,过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        int rows = commentMapper.insertComment(comment);

        //更新帖子评论数量,只有评论给帖子的时候才更新,回复不更新
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }


        return rows;


    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
