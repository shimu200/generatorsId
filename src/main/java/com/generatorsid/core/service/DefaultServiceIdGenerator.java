package com.generatorsid.core.service;

import com.generatorsid.core.IdGenerator;
import com.generatorsid.core.snowflake.SnowflakeIdInfo;
import com.generatorsid.util.SnowflakeIdUtil;

/**
 * 默认业务Id生成器
 */
public final class DefaultServiceIdGenerator implements ServiceIdGenerator {

    private final IdGenerator idGenerator;

    private long maxBizIdBitsLen;

    public DefaultServiceIdGenerator() {
        this(SEQUENCE_BIZ_BITS);
    }

    public DefaultServiceIdGenerator(long serviceIdBitLen) {
        idGenerator = SnowflakeIdUtil.getInstance();
        this.maxBizIdBitsLen = (long) Math.pow(2, serviceIdBitLen);
    }

    @Override
    public long nextId(long serviceId) {
        long id = Math.abs(Long.valueOf(serviceId).hashCode()) % (this.maxBizIdBitsLen);
        long nextId = idGenerator.nextId();
        return nextId | id;
    }

    @Override
    public String nextIdStr(long serviceId) {
        return Long.toString(nextId(serviceId));
    }

    @Override
    public SnowflakeIdInfo parseSnowflakeId(long snowflakeId) {
        return SnowflakeIdInfo.builder().workId((int) ((snowflakeId >> WORKER_ID_SHIFT) & ~(-1L << WORKER_ID_BITS))).dataCenterId((int) ((snowflakeId >> DATA_CENTER_ID_SHIFT) & ~(-1L << DATA_CENTER_ID_BITS))).timestamp((snowflakeId >> TIMESTAMP_LEFT_SHIFT) + DEFAULT_TWEPOCH).sequence((int) ((snowflakeId >> SEQUENCE_BIZ_BITS) & ~(-1L << SEQUENCE_ACTUAL_BITS))).gene((int) (snowflakeId & ~(-1L << SEQUENCE_BIZ_BITS))).build();
    }

    /**
     * 工作 ID 5 bit
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心 ID 5 bit
     */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /**
     * 序列号 12 位，表示只允许 workerId 的范围为 0-4095
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 真实序列号 bit
     */
    private static final long SEQUENCE_ACTUAL_BITS = 8L;

    /**
     * 基因 bit
     */
    private static final long SEQUENCE_BIZ_BITS = 4L;

    /**
     * 机器节点左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 默认开始时间
     */
    private static long DEFAULT_TWEPOCH = 1288834974657L;

    /**
     * 数据中心节点左移 17 位
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间毫秒数左移 22 位
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
}