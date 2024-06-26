package com.generatorsid.core.snowflake;

import cn.hutool.core.collection.CollUtil;
import com.generatorsid.base.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LocalRedisWorkIdChoose extends AbstractWorkIdChooseTemplate implements InitializingBean {

    private RedisTemplate stringRedisTemplate;

    public LocalRedisWorkIdChoose() {
        this.stringRedisTemplate = ApplicationContextHolder.getBean(StringRedisTemplate.class);
    }

    /**
     * 选择工作ID。通过执行Redis Lua脚本来获取工作ID，以确保ID的唯一性和高效性。
     * 如果Lua脚本执行失败或返回空结果，则通过其他方式生成随机的工作ID。
     *
     * @return WorkIdWrapper 包含两个唯一工作ID的包装类。
     */
    @Override
    public WorkIdWrapper chooseWorkId() {
        // 初始化Redis脚本，设置脚本来源为classpath下的chooseWorkIdLua.lua文件。
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/chooseWorkIdLua.lua")));

        List<Long> luaResultList = null;
        try {
            // 设置脚本的返回类型为List类。
            redisScript.setResultType(List.class);
            // 执行Redis Lua脚本，获取结果。
            luaResultList = (ArrayList) this.stringRedisTemplate.execute(redisScript, null);
        } catch (Exception ex) {
            // 记录Lua脚本执行失败的日志。
            log.error("Redis Lua 脚本获取 WorkId 失败", ex);
        }
        // 如果Lua脚本执行结果不为空，返回包含工作ID的包装类；否则，通过随机方式选择工作ID。
        return CollUtil.isNotEmpty(luaResultList) ? new WorkIdWrapper(luaResultList.get(0), luaResultList.get(1)) : new RandomWorkIdChoose().chooseWorkId();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        chooseAndInit();
    }
}
