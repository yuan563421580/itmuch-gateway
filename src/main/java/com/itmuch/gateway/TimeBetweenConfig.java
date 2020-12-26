package com.itmuch.gateway;

import lombok.Data;

import java.time.LocalTime;

/**
 * 自定义谓词工厂 泛型配置类进行承载
 * 实现 【配置类】 和 【配置文件】 的关系
 */
@Data
public class TimeBetweenConfig {
    private LocalTime start;
    private LocalTime end;
}
