package com.justodit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    //注入spring的JavaMailSender
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;    //发件人

    /**
     *
     * @param to  发送给谁
     * @param subject  邮件的标题
     * @param content  邮件的内容
     */
    public void senderMail(String to,String subject,String content){

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

            //发件人
            helper.setFrom(from);
            //收件人
            helper.setTo(to);
            //设置标题
            helper.setSubject(subject);
            //设置文本  true 表示支持html
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败"+e.getMessage());
        }

    }


}
