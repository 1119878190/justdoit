package com.justodit.dao;
/**
 * 这个Mapper已废弃  重构了登录凭证 存放到Redis中  @Deprecated
 */

import com.justodit.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId} ,#{ticket} ,#{status} ,#{expired} )"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket ticket);

    //ticket是唯一的
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket} "
    })
    LoginTicket selectByTicket(String ticket);


    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket}",
            "<if test=\"ticket!=null\">",
            "and 1 =1",
            "</if>",
            "</script>"
    })//这里写动态sql是为了演示写法,不写也可以
    int updateStatus(String ticket,int status);

}
