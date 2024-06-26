package com.generatorsid.util;

import com.generatorsid.core.snowflake.Snowflake;
import com.generatorsid.core.snowflake.SnowflakeIdInfo;
import com.generatorsid.handler.IdGeneratorManager;
import org.springframework.stereotype.Component;

@SuppressWarnings("all")
@Component
public class SnowflakeIdUtil {
    /**
     * 雪花算法对象
     */
    private static Snowflake SNOWFLAKE;

    /**
     * 初始化雪花算法
     */
    public static void initSnowflake(Snowflake snowflake) {
        SnowflakeIdUtil.SNOWFLAKE = snowflake;
    }

    /**
     * 获取雪花算法实例
     */
    public static Snowflake getInstance() {
        return SNOWFLAKE;
    }

    /**
     * 获取雪花算法下一个 ID
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 获取雪花算法下一个字符串类型 ID
     */
    public static String nextIdStr() {
        return Long.toString(nextId());
    }

    /**
     * 解析雪花算法生成的 ID 为对象
     */
    public static SnowflakeIdInfo parseSnowflakeId(String snowflakeId) {
        return SNOWFLAKE.parseSnowflakeId(Long.parseLong(snowflakeId));
    }

    /**
     * 解析雪花算法生成的 ID 为对象
     */
    public static SnowflakeIdInfo parseSnowflakeId(long snowflakeId) {
        return SNOWFLAKE.parseSnowflakeId(snowflakeId);
    }

    /**
     * 根据 {@param serviceId} 生成雪花算法 ID
     */
    public static long nextIdByService(String serviceId) {
        return IdGeneratorManager.getDefaultServiceIdGenerator().nextId(Long.parseLong(serviceId));
    }
}
