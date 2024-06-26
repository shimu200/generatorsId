package com.generatorsid.core.snowflake;

import com.generatorsid.util.SnowflakeIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public abstract class AbstractWorkIdChooseTemplate {

    @Value("${framework.generators.id.snowflake.is-use-system-clock:false}")
    private boolean isUseSystemClock;
    /*
        根据自定义策略获取 WorkId 生成器
     */
    protected abstract WorkIdWrapper chooseWorkId();

    public void chooseAndInit(){
        WorkIdWrapper workIdWrapper = chooseWorkId();
        long workId = workIdWrapper.getWorkId();
        long dataCenterId = workIdWrapper.getDataCenterId();
        Snowflake snowflake = new Snowflake(workId,dataCenterId,false);
        log.info("Snowflake type: {}, workId: {}, dataCenterId: {}", this.getClass().getSimpleName(), workId, dataCenterId);
        SnowflakeIdUtil.initSnowflake(snowflake);
    }
}
