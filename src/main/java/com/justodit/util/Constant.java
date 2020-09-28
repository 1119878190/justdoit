package com.justodit.util;

//常量接口
public interface Constant {


    /**
     * 邮箱激活成功
     */
    int ACTIVATION_SUCCESS  = 0;

    /**
     * 邮箱重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 邮箱激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     *默认状态的登录凭证超时时间 12hour
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间 3个月
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型:评论    帖子的评论
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型:回复    评论的评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型: 用户  关注
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题: 评论  系统通知
     */
    String  TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞  系统通知
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题:关注  系统通知
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题:  发帖  ElasticSearch
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题: 删帖   ElasticSearch
     */
    String TOPIC_DELETE = "delete";

    /**
     * 系统管理员用户Id
     */
    int SYSTEM_USER_ID = 1;


    /**
     * 权限: 普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限: 管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限: 版主
     */
    String AUTHORITY__MODERATOR = "moderator";



}
