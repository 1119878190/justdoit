package com.justodit;


import com.justodit.dao.DiscussPostMapper;
import com.justodit.dao.LoginTicketMapper;
import com.justodit.dao.MessageMapper;
import com.justodit.entity.DiscussPost;
import com.justodit.entity.LoginTicket;
import com.justodit.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapperTest {


    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void test(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10,0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

        int rows = discussPostMapper.selectDisCussPostRows(149);
        System.out.println(rows);
    }


    //登录凭证测试
    @Test
    public void testInsert(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("aksjf");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000 * 60 *10));//10分钟

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("aksjf");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("aksjf",1);
        loginTicket = loginTicketMapper.selectByTicket("aksjf");
        System.out.println(loginTicket);
    }


    @Test
    public void testDate(){
        Date date = new Date();
        System.out.println(date.getTime());
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis()+8640000*1000);
        System.out.println(new Date().getTime() + 8640000*1000);
    }


    //Message
    @Test
    public void testSelectLetters(){

        List<Message> messages = messageMapper.selectConversations(111,0,20);
        for (Message message : messages) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        List<Message> messages1 = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messages1) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);
    }
}
