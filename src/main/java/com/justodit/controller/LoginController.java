package com.justodit.controller;


import ch.qos.logback.core.util.TimeUtil;
import com.google.code.kaptcha.Producer;
import com.justodit.entity.User;
import com.justodit.service.UserService;
import com.justodit.util.Constant;
import com.justodit.util.JustDoItUtil;
import com.justodit.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements Constant {

    @Autowired
    private UserService userService;

    //kaptcha验证码
    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    //转到注册的页面
    @GetMapping(path = "/register")
    public String getRegisterPage(){

        return "/site/register";
    }

    //跳转到登录页面
    @GetMapping(path = "/login")
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 注册
     * @param model
     * @param user
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user){

        Map<String, Object> map = userService.register(user);

        //map里面没有任何错误信息  注册成功
        if (map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";  // 成功 跳转到成功页面
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("user",user);
            return "/site/register";  // 失败  返回注册页面 提示错误信息
        }

    }


    //激活邮件
    // http://localhost:8080/contextPath/activation/id/code
    @GetMapping("/activation/{userId}/{code}")
    public String activation(@PathVariable("userId") Integer userId,@PathVariable("code") String code,Model model){
        int result = userService.activation(userId, code);

        if (result == ACTIVATION_SUCCESS){ //成功
            model.addAttribute("msg","激活成功,您的账号可以正常使用了");
            model.addAttribute("target","/login");
        }else if (result == ACTIVATION_REPEAT){ //重复激活
            model.addAttribute("msg","无效的操作,该账号已经激活过了");
            model.addAttribute("target","/index");
        }else {//失败
            model.addAttribute("msg","激活失败,您提供的激活码不正确");
            model.addAttribute("target","/index");
        }

        return  "/site/operate-result";
    }




    /**kaptcha 生成验证码
     *
     * @param response
     * @param
     */
    @RequestMapping(value = "/kaptcha",method = RequestMethod.GET)
    public void  getKaptcha(HttpServletResponse response/*, HttpSession session*/){

        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存放到session中
        //session.setAttribute("kaptcha",text);

        //将验证码存到Redis中
        //验证码的归属
        String kaptchaOwner = JustDoItUtil.generateUUId();
        Cookie cookie  = new Cookie("kaptcharOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/");
        response.addCookie(cookie);

        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);


        //将图片输出给浏览器中
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败 :"+e.getMessage());
        }
    }


    /**
     *登录
     * @param username  用户名
     * @param password  密码
     * @param code   验证码
     * @param rememberme   记住我
     * //@param  session    用于和session中的验证码比较
     * @param response  将登录凭证发送到客户端保存
     * @param kaptcharOwner  登录重构后 用于从cookie中获取该用户的验证码
     * @return
     */
    @PostMapping("/login")//传进来分装成对象如User,model将自动存到model里,如果是基本类型,可以从request中取
    public String login(String username,String password,String code,Boolean rememberme,
                        Model model/*,HttpSession session*/,HttpServletResponse response,HttpServletRequest request,@CookieValue("kaptcharOwner") String kaptcharOwner){

        //判断验证码
        if (rememberme==null) rememberme=false;
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptcharOwner)){
            //从redis中获取验证码
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptcharOwner);
             kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        //检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String ,Object> map = userService.login(username,password,expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(request.getContextPath());
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    /**
     * 退出  将用户的登录状态设置为1  并且将SpringSecurity中的SecurityContextHolder中的用户信息清除
     * @param ticket
     * @return
     */
   @GetMapping("/logout")
   public String loginOut(@CookieValue("ticket")String ticket){
        userService.logOut(ticket);
       SecurityContextHolder.clearContext();//清楚vSecurity中的用户信息
        return "redirect:/login";
   }
}
