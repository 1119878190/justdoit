package com.justodit.service;

import com.justodit.dao.MessageMapper;
import com.justodit.entity.Message;
import com.justodit.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import java.util.List;

@Service
public class MessageService {


    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;


    //查询当前用户的所有会话列表
    public List<Message> findConversations(int  userId,int offset,int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    //查询当前用户会话数量
    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    //查询私信详情
    public List<Message> findLetters(String conversationId,int offset ,int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    //查询一个会话私信的数量
    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount((conversationId));
    }

    //查询未读消息数
    public int findLetterUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    //添加一条消息
    public int addMessage(Message message){

        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        //过滤敏感词
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    //更新消息为已读
    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

//系统通知
    /**
     * 查询最新的系统通知
     * @param userId
     * @param topic
     * @return
     */
    public Message findLatestNotice(int userId,String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }

    /**
     * 查询单个通知的数量
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeCount(int userId,String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }

    /**
     * 查询未读通知的数量
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeUnreadCount(int userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

//系统通知详情
    /**
     * 查询系统通知列表
     * @param userId 用户id
     * @param topic  通知类型
     * @param offset  分页
     * @param limit
     * @return
     */
    public List<Message> findNotices(int userId, String topic,int offset,int limit){
        return messageMapper.selectNotices(userId,topic,offset,limit);
    }
}
