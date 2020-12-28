package com.itmuch.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 按照X限流，就写一个针对X的KeyResolver。
 */
@Configuration
public class RateConfiguration {

    /**
     * 按照Path限流
     *      限流规则即可作用在路径上。
     *
     * 例如：
     * # 访问：http://${GATEWAY_URL}/users/1，对于这个路径，它的redis-rate-limiter.replenishRate = 1，redis-rate-limiter.burstCapacity = 2；
     * # 访问：http://${GATEWAY_URL}/shares/1，对这个路径，它的redis-rate-limiter.replenishRate = 1，redis-rate-limiter.burstCapacity = 2；
     *
     * 测试：
     * # 持续高速访问某个路径，速度过快时，返回 HTTP ERROR 429 。
     * @return key
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getPath()
                        .toString()
        );
    }

    /**
     * 实现针对用户的限流
     * @return
     */
    /*@Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getQueryParams()
                        .getFirst("user")
        );
    }*/

    /**
     * 针对来源IP的限流
     * @return
     */
    /*@Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-Forwarded-For")
        );
    }*/

}
