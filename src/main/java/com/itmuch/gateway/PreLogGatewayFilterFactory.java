package com.itmuch.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * 编写一个自定义过滤器工厂 - 实现功能：记录打印日志
 *
 * 自定义过滤器工厂一定要以 GatewayFilterFactory 结尾
 * PreLog 与 application.yml 中的配置
 *      spring.cloud.gateway.routes.filters.-PreLog保持一致
 * 继承 AbstractNameValueGatewayFilterFactory ，实现 apply() 方法
 */
@Slf4j
@Component
public class PreLogGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
    @Override
    public GatewayFilter apply(NameValueConfig config) {

        // 使用 lambda 表达式方式实现 匿名内部类 逻辑
        return ((exchange, chain) -> {
            // 这里面才是实际处理业务的逻辑

            log.info("请求进来了...{},{}", config.getName(), config.getValue());

            // 获取 request , 修改请求 ，获取修改后的请求 （测试中没有修改）
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .build();

            // 修改 exchange
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            // 交给下一个过滤器
            return chain.filter(modifiedExchange);
        });

    }
}
