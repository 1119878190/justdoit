package com.justodit.controller;

import com.justodit.annotation.LoginRequired;
import com.justodit.entity.User;
import com.justodit.service.FollowService;
import com.justodit.service.LikeService;
import com.justodit.service.UserService;
import com.justodit.util.Constant;
import com.justodit.util.HostHolder;
import com.justodit.util.JustDoItUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.jws.WebParam;
import javax.print.attribute.standard.MediaSize;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


@Controller
@RequestMapping("/user")
public class UserController implements Constant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Value("${justdoit.path.upload}")
    private String uploadPath;

    @Value("${justdoit.path.domain}")
    private String domain;

   /* @Value("${server.servlet.context-path}")
    private String contextPath;*/

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    //跳转用户设置页面
    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }


    /**
     *上传头像
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));//后缀
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
        }
        //生成随机的文件名
        filename = JustDoItUtil.generateUUId() +suffix;
        //确定文件的存放路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
           logger.error("上传文件失败"+e.getMessage());
           throw new RuntimeException("上传文件失败,服务器发生异常"+e);
        }
      //更新当前用户的头像的路径web访问路径
        //http://localhost:8080/user/header/xxxxx.png
        User user = hostHolder.getUser();
        // String headerUrl = domain + contextPath + "/user/header/"+ filename;  如果设置了contextPath加上
        String headerUrl = domain + "/user/header/"+ filename;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    /**
     * 读取头像
     * @param fileName
     * @param response
     */
    @RequestMapping(value = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename")String fileName, HttpServletResponse response){

        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream outputStream = response.getOutputStream()
        ){

            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }


    }


    /**
     *个人主页
     * @param userId 用户id
     * @param model
     * @return
     */
    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfile(@PathVariable("userId")int userId, Model model){

        User user  = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }

        //用户
        model.addAttribute("user",user);
        //用户收到点赞的数量
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",userLikeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followerCount",followerCount);
        //当前登录用户是否对该用户关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }

        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";

    }

}
