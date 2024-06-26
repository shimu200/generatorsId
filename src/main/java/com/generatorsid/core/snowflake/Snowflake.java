package com.generatorsid.core.snowflake;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.generatorsid.core.IdGenerator;

import java.io.Serializable;
import java.util.Date;

public class Snowflake implements Serializable, IdGenerator {

    private static final long serialVersionUID = 1L;

    /**
     * 默认的起始时间，为Thu, 04 Nov 2010 01:42:54 GMT
     */
    private static long DEFAULT_TWEPOCH = 1288834974657L;

    /**
     * 默认回拨时间，2S
     */
    private static long DEFAULT_TIME_OFFSET = 2000L;

    /**
     * workId 位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 最大支持机器节点数0~31，一共32个
     */
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);

    /**
     * dataCenterId 位数
     */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /**
     * 最大支持数据中心节点数0~31，一共32个
     */
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private static final long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);

    /**
     * 序列号12位表示支持的自增Id位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器节点左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心左移17位
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移22位
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 序列掩码，用于限定序列最大值不能超过4095
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 初始化时间点
     */
    private final long twepoch;

    private final long workerId;

    private final long dataCenterId;

    private final boolean useSystemClock;
    /**
     * 允许的时钟回拨毫秒数
     */
    private final long timeOffset;

    /**
     * 当在低频模式下时，序号始终为0，导致生成ID始终为偶数<br>
     * 此属性用于限定一个随机上限，在不同毫秒下生成序号时，给定一个随机数，避免偶数问题。<br>
     * 注意次数必须小于{@link #SEQUENCE_MASK}，{@code 0}表示不使用随机数。<br>
     * 这个上限不包括值本身。
     */
    private final long randomSequenceLimit;

    /**
     * 自增序号，当高频模式下时，同一毫秒内生成N个ID，则这个序号在同一毫秒下，自增以避免ID重复。
     */
    private long sequence = 0L;

    private long lastTimestamp = -1L;

    /**
     * 构造，使用自动生成的工作节点ID和数据中心ID
     */
    public Snowflake() {
        this(IdUtil.getWorkerId(IdUtil.getDataCenterId(MAX_DATA_CENTER_ID), MAX_WORKER_ID));
    }

    /**
     * @param workerId 终端ID
     */
    public Snowflake(long workerId) {
        this(workerId, IdUtil.getDataCenterId(MAX_DATA_CENTER_ID));
    }

    /**
     * @param workerId     终端ID
     * @param dataCenterId 数据中心ID
     */
    public Snowflake(long workerId, long dataCenterId) {
        this(workerId, dataCenterId, false);
    }

    /**
     * @param workerId         终端ID
     * @param dataCenterId     数据中心ID
     * @param isUseSystemClock 是否使用{@link SystemClock} 获取当前时间戳
     */
    public Snowflake(long workerId, long dataCenterId, boolean isUseSystemClock) {
        this(null, workerId, dataCenterId, isUseSystemClock);
    }

    /**
     * @param epochDate        初始化时间起点（null表示默认起始日期）,后期修改会导致id重复,如果要修改连workerId dataCenterId，慎用
     * @param workerId         工作机器节点id
     * @param dataCenterId     数据中心id
     * @param isUseSystemClock 是否使用{@link SystemClock} 获取当前时间戳
     * @since 5.1.3
     */
    public Snowflake(Date epochDate, long workerId, long dataCenterId, boolean isUseSystemClock) {
        this(epochDate, workerId, dataCenterId, isUseSystemClock, DEFAULT_TIME_OFFSET);
    }

    /**
     * @param epochDate        初始化时间起点（null表示默认起始日期）,后期修改会导致id重复,如果要修改连workerId dataCenterId，慎用
     * @param workerId         工作机器节点id
     * @param dataCenterId     数据中心id
     * @param isUseSystemClock 是否使用{@link SystemClock} 获取当前时间戳
     * @param timeOffset       允许时间回拨的毫秒数
     * @since 5.8.0
     */
    public Snowflake(Date epochDate, long workerId, long dataCenterId, boolean isUseSystemClock, long timeOffset) {
        this(epochDate, workerId, dataCenterId, isUseSystemClock, timeOffset, 0);
    }

    /**
     * @param epochDate           初始化时间起点（null表示默认起始日期）,后期修改会导致id重复,如果要修改连workerId dataCenterId，慎用
     * @param workerId            工作机器节点id
     * @param dataCenterId        数据中心id
     * @param isUseSystemClock    是否使用{@link SystemClock} 获取当前时间戳
     * @param timeOffset          允许时间回拨的毫秒数
     * @param randomSequenceLimit 限定一个随机上限，在不同毫秒下生成序号时，给定一个随机数，避免偶数问题，0表示无随机，上限不包括值本身。
     * @since 5.8.0
     */
    public Snowflake(Date epochDate, long workerId, long dataCenterId, boolean isUseSystemClock, long timeOffset, long randomSequenceLimit) {
        this.twepoch = (null != epochDate) ? epochDate.getTime() : DEFAULT_TWEPOCH;
        this.workerId = Assert.checkBetween(workerId, 0, MAX_WORKER_ID);
        this.dataCenterId = Assert.checkBetween(dataCenterId, 0, MAX_DATA_CENTER_ID);
        this.useSystemClock = isUseSystemClock;
        this.timeOffset = timeOffset;
        this.randomSequenceLimit = Assert.checkBetween(randomSequenceLimit, 0, SEQUENCE_MASK);
    }

    /**
     * 根据Snowflake的ID，获取机器id
     *
     * @param id snowflake算法生成的id
     * @return 所属机器的id
     */
    public long getWorkerId(long id) {
        return id >> WORKER_ID_SHIFT & ~(-1L << WORKER_ID_BITS);
    }

    /**
     * 根据Snowflake的ID，获取数据中心id
     *
     * @param id snowflake算法生成的id
     * @return 所属数据中心
     */
    public long getDataCenterId(long id) {
        return id >> DATA_CENTER_ID_SHIFT & ~(-1L << DATA_CENTER_ID_BITS);
    }

    /**
     * 根据Snowflake的ID，获取生成时间
     *
     * @param id snowflake算法生成的id
     * @return 生成的时间
     */
    public long getGenerateDateTime(long id) {
        return (id >> TIMESTAMP_LEFT_SHIFT & ~(-1L << 41L)) + twepoch;
    }

    /**
     * 生成下一个ID。
     *
     * @return 生成的ID。
     * @throws IllegalStateException 如果系统时间回拨超过允许的范围，则抛出此异常。
     */
    public synchronized long nextId() {
        long timestamp = genTime();

        // 检查时间是否回拨
        if (timestamp < this.lastTimestamp) {
            // 如果回拨时间小于允许的回拨时间，则使用上一次的时间戳
            if (this.lastTimestamp - timestamp < timeOffset) {
                timestamp = lastTimestamp;
            } else {
                // 如果回拨时间超过允许范围，则抛出异常
                // 如果服务器时间有问题(时钟后退) 报错。
                throw new IllegalStateException(StrUtil.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
            }
        }

        // 如果时间戳与上一次相同，需要递增序列号
        if (timestamp == this.lastTimestamp) {
            final long sequence = (this.sequence + 1) & SEQUENCE_MASK;
            // 如果序列号溢出，则等待下一个时间戳
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
            this.sequence = sequence;
        } else {
            // 如果时间戳不同，重置序列号
            // issue#I51EJY
            if (randomSequenceLimit > 1) {
                sequence = RandomUtil.randomLong(randomSequenceLimit);
            } else {
                sequence = 0L;
            }
        }

        this.lastTimestamp = timestamp;

        // 构造ID：时间戳、数据中心ID、工作机器ID和序列号组合
        return ((timestamp - twepoch) << TIMESTAMP_LEFT_SHIFT) | (dataCenterId << DATA_CENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }


    /**
     * 下一个ID（字符串形式）
     *
     * @return ID 字符串形式
     */
    public String nextIdStr() {
        return Long.toString(nextId());
    }

    /**
     * 循环等待下一个时间
     *
     * @param lastTimestamp 上次记录的时间
     * @return 下一个时间
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = genTime();
        // 循环直到操作系统时间戳变化
        while (timestamp == lastTimestamp) {
            timestamp = genTime();
        }
        if (timestamp < lastTimestamp) {
            // 如果发现新的时间戳比上次记录的时间戳数值小，说明操作系统时间发生了倒退，报错
            throw new IllegalStateException(StrUtil.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
        }
        return timestamp;
    }

    /**
     * 生成时间戳
     *
     * @return 时间戳
     */
    private long genTime() {
        return this.useSystemClock ? SystemClock.now() : System.currentTimeMillis();
    }

    /**
     * 解析雪花算法生成的 ID 为对象
     *
     * @param snowflakeId 雪花算法 ID
     * @return
     */
    public SnowflakeIdInfo parseSnowflakeId(long snowflakeId) {
        SnowflakeIdInfo snowflakeIdInfo = new SnowflakeIdInfo();
        snowflakeIdInfo.setSequence((int) (snowflakeId & ~(-1L << SEQUENCE_BITS)));
        snowflakeIdInfo.setWorkId((int) ((snowflakeId >> WORKER_ID_SHIFT) & ~(-1L << WORKER_ID_BITS)));
        snowflakeIdInfo.setDataCenterId((int) ((snowflakeId >> DATA_CENTER_ID_SHIFT) & ~(-1L << DATA_CENTER_ID_BITS)));
        snowflakeIdInfo.setTimestamp((snowflakeId >> TIMESTAMP_LEFT_SHIFT) + twepoch);
        return snowflakeIdInfo;
    }

}
