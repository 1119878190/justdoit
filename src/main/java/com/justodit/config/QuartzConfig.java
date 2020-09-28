package com.justodit.config;

import com.justodit.quartz.AlphaJob;
import com.justodit.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//配置  ->  数据库  -> 调用(这个配置类quartz值使用1次,执行一次后,注释@Bean)
@Configuration
public class QuartzConfig {

    //FactoryBean可简化Bean的实例化过程:
    //1.通过FactoryBean分装了Bean的实例化过程
    //2.将FactoryBean装配到Spring容器里
    //3.将FactoryBean注入给其它的Bean
    //4该Bean得到的是FactoryBean所管理的对象实例


    //配置JobDetail
    //@Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);//持久保存
        factoryBean.setRequestsRecovery(true);//该任务是不是可恢复
        return factoryBean;
    }

    //配置Trigger(SimpleTriggerFactoryBean,CornTriggerFactoryBean)
   // @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);//多久执行一次任务
        factoryBean.setJobDataMap(new JobDataMap());//Job状态
        return factoryBean;
    }

    //配置JobDetail  刷新贴子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("justdoitJobGroup");
        factoryBean.setDurability(true);//持久保存
        factoryBean.setRequestsRecovery(true);//该任务是不是可恢复
        return factoryBean;
    }

    //配置Trigger(SimpleTriggerFactoryBean,CornTriggerFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("justdoitTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);//多久执行一次任务 5分钟
        factoryBean.setJobDataMap(new JobDataMap());//Job状态
        return factoryBean;
    }

}
