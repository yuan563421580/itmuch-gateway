package com.itmuch.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }


    /**
     * Spring Cloud Gateway :
     * · 是Spring Cloud的网关（第二代），未来会取代Zuul（第一代）
     * · 基于 Netty 、 Reactor 以及 WebFlux 构建
     *     ~ Netty ：网络通信框架，可以实现高性能的服务端和客户端
     *     ~ Reactor ：是一个 Reactive 编程模型的实现，正在越来越流行
     *     ~ WebFlux ：是一个 Reactive 的 Web 框架
     * · 优点：
     *     ~ 性能强劲 ：是第一代网关Zuul 1.x的1.6倍！性能PK : https://www.imooc.com/article/285068
     *     ~ 功能强大 ：内置了很多实用功能，比如转发、监控、限流等
     *     ~ 设置优雅，易扩展
     * · 缺点：
     *     ~ 依赖 Netty 与 WebFlux , 不是 Servlet 编程模型，有一定的适应成本
     *     ~ 不能在 Servlet 容器下工作，也不能构建成 WAR 包
     *     ~ 不支持 Spring Boot 1.x
     * · 转发规则：
     *     访问 ${GATEWAY_URL}/{微服务X}/** 会转发到 微服务X的/**路径
     *
     *
     */

}
