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
     *
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
     *
     * · 转发规则：
     *     访问 ${GATEWAY_URL}/{微服务X}/** 会转发到 微服务X的/**路径
     *
     * · 核心概念：
     *     ~ Route(路由) : Spring Cloud Gateway 的基础元素，可简单理解成一条转发的规则。包含：ID、目标URL、Predicate集合以及Filter集合。
     *     ~ Predicate(谓词) : 即java.util.function.Predicate , Spring Cloud Gateway使用Predicate实现路由的匹配条件。
     *     ~ Filter(过滤器) : 修改请求及相响应（可以为路由添加业务逻辑）。
     *
     * · 工作流程：
     *     ~ 查看 gateway-how-it-works.jpg 工作流程图；解读如下：
     *          客户端向 Spring Cloud Gateway 发出请求。
     *          如果 Gateway Handler Mapping 中找到与请求相匹配的路由，将其发送到 Gateway Web Handler。
     *          Handler 再通过指定的过滤器链来将请求发送到我们实际的服务执行业务逻辑，然后返回。
     *          过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前（“pre”）或之后（“post”）执行业务逻辑。
     *
     * · 路由谓词工厂（Route Predicate Factories）：
     *     ~ 路由谓词工厂作用是：符合Predicate的条件，就使用该路由的配置，否则就不管。
     *     ~ 例子在 resource/predicate/application-*-*.yml 中
     *     ~ 手记：https://www.imooc.com/article/290804
     * · 自定义谓词工厂：例子：限制09:00-17:00才能访问（为了测试后续改成23:59）：
     *     ~ 01).application.yml 中配置 ：spring.cloud.gateway.routes （具体进入查看细节）
     *     ~ 02).编写自定义谓词工厂实现类 TimeBetweenRoutePredicateFactory （具体进入查看细节）
     *     ~ 03).编写自定义谓词工厂泛型配置类进行承载 TimeBetweenConfig （具体进入查看细节）
     *     ~ 04).测试：http://localhost:8040/users/1
     *
     * · 过滤器工厂详解 GatewayFilter Factories
     *     ~ 手记：https://www.imooc.com/article/290816
     *     ~ 01).application.yml 中配置 ：spring.cloud.gateway.routes.filters （具体进入查看细节）
     *     ~ 02).测试：http://localhost:8040/users/1
     *          技巧：断点打在 org.springframework.cloud.gateway.filter.NettyRoutingFilter#filter ，
     *              就可以调试Gateway转发的具体细节了。
     *
     * · Gateway 过滤器声命周期 ：Spring Cloud Gateway 以转发请求为边界
     *     ~ pre : Gateway 转发请求之前
     *     ~ post : Gateway 转发请求之后
     *
     * · 自定义过滤器工厂方式
     *     ~ 方式 1). 继承 ：AbstractGatewayFilterFactory ； 需要在 application.yml 中实现配置
     *            spring:
     *              cloud:
     *               gateway:
     *                routes:
     *                  filters:
     *                  # 过滤器工厂的名称
     *                  - name: RequestSize
     *                    # 该过滤器工厂的参数
     *                    args:
     *                      maxSize: 500000
     *          参考事例：org.springframework.cloud.gateway.filter.factory.RequestHeaderSizeGatewayFilterFactory
     *     ~ 方式 2). 继承 ：AbstractNameValueGatewayFilterFactory ； 需要在 application.yml 中实现配置
     *            spring:
     *              cloud:
     *               gateway:
     *                routes:
     *                  filters:
     *                  # 过滤器工厂的名称及参数以name-value的形式配置
     *                  - AddRequestHeader=S-Header, Bar
     *          参考事例：org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory
     * · 自定义过滤器工厂 - 核心API
     *     ~ exchange.getRequest().mutate().xxx：修改 request
     *     ~ exchange.mutate().xxx：修改 exchange
     *     ~ chain.filter(exchange)：传递给下一个过滤器处理
     *     ~ exchange.getResponse()：获取响应对象
     *     exchange 实际类型为 ServerWebExchange，chain 实际类型为 GatewayFilter
     * · 编写一个自定义过滤器工厂 - 实现功能：记录打印日志
     *     ~ 01).编写自定义过滤器工厂实现类 PreLogGatewayFilterFactory （具体进入查看细节）
     *     ~ 02).application.yml 中配置 ：spring.cloud.gateway.routes.filters.-PreLog （具体进入查看细节）
     * · 全局过滤器（Global Filters）：
     *     ~ 手记：https://www.imooc.com/article/290821
     *     LoadBalancerClient Filter 和 Netty Write Response Filter 特别重要 TODO 学习一下
     *
     * Spring Cloud Gateway 整合 Sentinel
     * https://blog.csdn.net/dsh153/article/details/105218152
     */

}
