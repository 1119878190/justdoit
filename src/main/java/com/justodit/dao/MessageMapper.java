package com.justodit.dao;

import com.justodit.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//会话
@Mapper
public interface MessageMapper {


    /**
     * 查询当前用户的会话列表,针对每个会话只返回一条最新的私信
     * @param userId  用户id
     * @param offset   起始
     * @param limit   多少条,分页
     * @return  该用户的所有私信列表
     */
    List<Message> selectConversations(int userId,int offset,int limit);


    /**
     *  查询当前用户的会话数量
     * @param userId 用户id
     * @return
     */
    int selectConversationCount(int userId);

    /**
     *  查询某个会话所包含的私信列表
     * @param conversationId   会话id
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectLetters(String conversationId,int offset,int limit);


    /**
     *  查询某个会话所包含的私信数量
     * @param conversationId 会话id
     * @return
     */
    int selectLetterCount(String conversationId);


    /** 查询未读私信的数量
     *
     * @param userId  用户id
     * @param conversationId   会话id,有的话代表单个私信的未读数,没有代表整个会话列表的未读数
     * @return
     */
    int selectLetterUnreadCount(int userId,String conversationId);


    /**
     * 发送   新增 消息
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 修改消息的状态
     * @param ids  要修改已读消息的id
     * @param status  要修改成的状态 0已读   1未读   2删除
     * @return
     */
    int updateStatus(List<Integer> ids,int status);

//系统通知

    /**
     *  查询某个主题下的最新的通知
     * @param userId 用户id
     * @param topic  通知主题类型
     * @return
     */
    Message selectLatestNotice(int userId,String topic);

    /**
     * 查询某个主题所包含的通知的数量
     * @param userId 用户id
     * @param topic  通知主题类型
     * @return
     */
    int selectNoticeCount(int userId,String topic);

    /**
     * 查询未读的通知的数量
     * @param userId 用户id
     * @param topic  通知主题类型
     * @return
     */
    int selectNoticeUnreadCount(int userId,String topic);

//通知详情

    /**
     * 查询某个主题所包含的通知列表
     * @param userId 当前用户id
     * @param topic   主题类型
     * @param offset  分页
     * @param limit
     * @return
     */
    List<Message> selectNotices(int userId,String topic,int offset,int limit);

}
