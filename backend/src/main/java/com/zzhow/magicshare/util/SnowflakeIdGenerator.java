package com.zzhow.magicshare.util;

/**
 * 雪花算法（Snowflake）ID 生成器
 */
public class SnowflakeIdGenerator {

    // 配置参数
    private final long epoch = 1640995200000L; // 自定义起始时间戳（2022-01-01 00:00:00）
    private final long machineIdBits = 10L; // 机器 ID 部分的位数
    private final long sequenceBits = 12L; // 序列号部分的位数
    private final long machineIdMax = -1L ^ (-1L << machineIdBits); // 机器 ID 最大值
    private final long sequenceMask = -1L ^ (-1L << sequenceBits); // 序列号掩码

    // 位移量
    private final long machineIdShift = sequenceBits; // 机器 ID 左移的位数
    private final long timestampShift = sequenceBits + machineIdBits; // 时间戳左移的位数
    private final long epochShift = 22; // 基准时间（自定义起始时间戳）

    private long lastTimestamp = -1L; // 上一次生成 ID 的时间戳
    private long sequence = 0L; // 序列号
    private final long machineId; // 机器 ID

    public SnowflakeIdGenerator(long machineId) {
        if (machineId > machineIdMax || machineId < 0) {
            throw new IllegalArgumentException("机器 ID 必须在 0 到 " + machineIdMax);
        }
        this.machineId = machineId;
    }

    // 生成唯一的 ID
    public synchronized long generateId() {
        long timestamp = timeGen(); // 当前时间戳
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("系统时钟出现回退，无法生成 ID");
        }

        if (timestamp == lastTimestamp) {
            // 同一毫秒内，序列号递增
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // 序列号溢出，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // 组装雪花算法生成的唯一 ID
        return ((timestamp - epoch) << timestampShift) | (machineId << machineIdShift) | sequence;
    }

    // 获取当前时间戳（毫秒）
    private long timeGen() {
        return System.currentTimeMillis();
    }

    // 等待下一毫秒
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
}
