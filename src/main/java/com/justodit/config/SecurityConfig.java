package com.justodit.config;

import com.justodit.util.Constant;
import com.justodit.util.JustDoItUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig  extends WebSecurityConfigurerAdapter implements Constant {


    /**
     * 忽略静态资源
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * 授权
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY__MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY__MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()//除了以上的请求需要权限,其它的不用
                .and().csrf().disable();//禁用csrf,由于该项目的ajax请求过多,不想改,故禁用csrf

        //权限不够时的处理
        http.exceptionHandling()
                //没登陆时,怎么处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //如果是AJAX请求
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf8");//声明数据返回类型
                            PrintWriter writer = response.getWriter();
                            writer.write(JustDoItUtil.getJsonString(403,"你还没有登录"));
                        }else {
                            //不是异步
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                //登录了,权限不足怎么处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //如果是AJAX请求
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf8");//声明数据返回类型
                            PrintWriter writer = response.getWriter();
                            writer.write(JustDoItUtil.getJsonString(403,"你没有访问此功能的权限"));
                        }else {
                            //不是异步
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });


        //Security底层会拦截/logout请求,进行退出处理,退出功能我们已经写好,不想用Security提供的退出
        //那么需要覆盖默认的逻辑,才能执行自己的退出代码
        http.logout().logoutUrl("/securitylogout");//这里的路径随便写的,只要项目里没有改路径就行



    }
}
