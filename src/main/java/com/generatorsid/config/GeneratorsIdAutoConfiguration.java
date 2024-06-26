package com.generatorsid.config;

import com.generatorsid.base.ApplicationContextHolder;
import com.generatorsid.core.snowflake.LocalRedisWorkIdChoose;
import com.generatorsid.core.snowflake.RandomWorkIdChoose;
import com.generatorsid.util.SnowflakeIdUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Import(ApplicationContextHolder.class)
public class GeneratorsIdAutoConfiguration {

    @Bean
    @ConditionalOnProperty("spring.data.redis.host")
    public LocalRedisWorkIdChoose redisWorkIdChoose(){
        return new LocalRedisWorkIdChoose();
    }

    @Bean
    @ConditionalOnMissingBean(LocalRedisWorkIdChoose.class)
    public RandomWorkIdChoose randomWorkIdChoose(){
        return new RandomWorkIdChoose();
    }
    @Bean
    public SnowflakeIdUtil snowflakeIdUtil(){
        return new SnowflakeIdUtil();
    }
}
