package com.justodit.event;

import com.alibaba.fastjson.JSONObject;
import com.justodit.entity.DiscussPost;
import com.justodit.entity.Event;
import com.justodit.entity.Message;
import com.justodit.service.DiscussPostService;
import com.justodit.service.ElasticSearchService;
import com.justodit.service.MessageService;
import com.justodit.util.Constant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UnknownFormatConversionException;

//消费者
@Component
public class EventConsumer implements Constant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);


    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    /**
     * 系统通知
     * @param record
     */
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }

        //解析内容 转换为Event对象  event.setData
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式错误!!");
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setStatus(0);
        message.setConversationId(event.getTopic());//这里将会话id设置为主题类型   like  comment followee
        message.setCreateTime(new Date());


        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());//谁触发的事件
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if (!event.getData().isEmpty()){
            for (Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }


    /**
     * 消费发帖事件
     * @param record
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式有误");
            return;
        }

        //通过消息的id查询贴子,存放到es中
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticSearchService.saveDiscussPost(post);


    }

    /**
     * 消费删帖事件
     */
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeletePost(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式有误");
            return;
        }

        //通过消息的id查询贴子,存放到es中
       elasticSearchService.deleteDiscussPost(event.getEntityId());


    }

}
