package com.justodit.service;

import com.justodit.dao.UserMapper;
import com.justodit.entity.LoginTicket;
import com.justodit.entity.User;
import com.justodit.util.Constant;
import com.justodit.util.JustDoItUtil;
import com.justodit.util.MailClient;
import com.justodit.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService  implements Constant {


    @Autowired
    private UserMapper userMapper;

    //邮件工具
    @Autowired
    private MailClient mailClient;

    //模板引擎  用于发送邮件
    @Autowired
    private TemplateEngine templateEngine;

    //登录凭证  不推荐使用,重构了登录凭证存放到Redis中
/*    @Autowired
    private LoginTicketMapper loginTicketMapper;*/

    @Value("${justdoit.path.domain}")
    private String domain;

    //@Value("${server.servlet.context-path}")  //没有配置contextPath,如果配置了 ,在发送邮件时加上
    //private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    //根据discuss_post 中的userid查询
    public User findUserById(int id){
       // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null){
            user = initCache(id);
        }
        return user;

    }


    /**
     * 注册
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String ,Object> map = new HashMap<>();

        //空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        //验证账号
        User user1 = userMapper.selectByName(user.getUsername());
        if (user1 != null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        //验证邮箱
        user1 = userMapper.selectByEmail(user.getEmail());
        if (user1 != null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(JustDoItUtil.generateUUId().substring(0,5));//生成随机的salt用于加密
        user.setPassword(JustDoItUtil.md5(user.getPassword() + user.getSalt()));//设置密码为MD5加密后的密码
        user.setType(0);    //用户类型  0为普通用户
        user.setStatus(0);   //用户激活状态   0表示未激活
        user.setActivationCode(JustDoItUtil.generateUUId());  //设置激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));//随机头像%d为随机数
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        //发送邮箱
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // http://localhost:8080/contextPath/activation/id/code
        String url = domain + "/activation/" + user.getId() + "/" + user.getActivationCode();  //传进来的user没有id,insertUser是id会返回,
                                                                // yml里配置了use-generated-keys,mapper里有keyProperty="id"
        context.setVariable("url",url);
        //设置邮件模板的位置
        String content = templateEngine.process("/mail/activation",context);
        mailClient.senderMail(user.getEmail(),"激活账号",content);

        return map;
    }


    /**
     * 激活邮件
     * @param userId 用户id
     * @param code  激活码
     * @return
     */
    public int activation(Integer userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);//清除缓存
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 登录
     * @param username 账号
     * @param password  密码  用户注册时使用md5进行加密,所以登陆时要对密码页进行md5加密进行比较
     * @param expiredSeconds  过期时间
     * @return
     */
    public Map<String,Object> login(String username,String password,int expiredSeconds){

        Map<String,Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        //验证状态
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        //验证密码
        password = JustDoItUtil.md5(password+user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(JustDoItUtil.generateUUId());
        loginTicket.setStatus(0);

        loginTicket.setExpired(new Date(new Date().getTime() + expiredSeconds*1000));
        System.out.println(loginTicket.getExpired());
       // loginTicketMapper.insertLoginTicket(loginTicket);

        //将登录凭证存放到Redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        //登录凭证发送给客户端,用于下次登录检测
        map.put("ticket",loginTicket.getTicket());

        return map;
    }

    /**
     *退出  将状态设置为1
     */
    public void logOut(String ticket){
        //loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
         LoginTicket loginTicket  = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
         loginTicket.setStatus(1);//状态1表示禁用
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }


    /**
     * 根据ticket查询登录凭证
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket){
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return  (LoginTicket)redisTemplate.opsForValue().get(redisKey);
    }


    /**
     * 更新用户头像
     * @param userId 用户id
     * @param headerUrl  头像Url
     * @return
     */
    public int updateHeader(int userId,String headerUrl){
       // return  userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);//清除缓存
        return rows;
    }

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     * 将用户信息存放到Redis中
     * 1.优先从缓存中取值
     */
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);


    }
       //2.取不到时就初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;

    }
    //3.当数据变更时,清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }


    //返回用户的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                       return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY__MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
