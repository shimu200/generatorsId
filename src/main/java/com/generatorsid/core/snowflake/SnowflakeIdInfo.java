package com.generatorsid.core.snowflake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnowflakeIdInfo {
    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 工作机器节点 ID
     */
    private Integer workId;

    /**
     * 数据中心 ID
     */
    private Integer dataCenterId;

    /**
     * 自增序号，同一毫秒生产的第N个自增ID
     */
    private Integer sequence;

    /**
     *  通过基因法生成的序号
     */
    private Integer gene;
}
