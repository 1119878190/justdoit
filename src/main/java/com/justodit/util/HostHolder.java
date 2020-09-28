package com.justodit.util;

import com.justodit.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户的信息,用于代替session对象,考虑到并发的情况,session中的数据可能冲突
 * 通过ThreadLocal实现线程隔离  以线程为key存取值的
 */
@Component
public class HostHolder {


    private ThreadLocal<User> users = new ThreadLocal<>();

    /*
     存
     */
    public void setUser(User user){
        users.set(user);
    }

    /**
     * 取
     * @return
     */
    public User getUser(){
        return  users.get();
    }

    /*
    *删
     */
    public void clear(){
        users.remove();
    }



}
