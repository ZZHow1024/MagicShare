package com.zzhow.magicshare.pojo.entity;

/**
 * 接收文件记录实体类（桌面端接收历史）
 *
 * @param fileName     文件名
 * @param size         文件大小（字节）
 * @param receivedTime 接收时间
 * @author ZZHow
 * create 2026/11/22
 */
public record ReceivedFile(String fileName, long size, String receivedTime) {
}