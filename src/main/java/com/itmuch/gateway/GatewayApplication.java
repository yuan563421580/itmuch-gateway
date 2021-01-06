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
     * ---
     *
     * · 路由谓词工厂（Route Predicate Factories）：
     *     ~ 路由谓词工厂作用是：符合Predicate的条件，就使用该路由的配置，否则就不管。
     *     ~ 例子在 resource/predicate/application-*-*.yml 中
     *     ~ 手记：https://www.imooc.com/article/290804
     *
     * · 自定义谓词工厂：例子：限制09:00-17:00才能访问（为了测试后续改成23:59）：
     *     ~ 01).application.yml 中配置 ：spring.cloud.gateway.routes （具体进入查看细节）
     *     ~ 02).编写自定义谓词工厂实现类 TimeBetweenRoutePredicateFactory （具体进入查看细节）
     *     ~ 03).编写自定义谓词工厂泛型配置类进行承载 TimeBetweenConfig （具体进入查看细节）
     *     ~ 04).测试：http://localhost:8040/users/1
     *
     * ---
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
     *
     * · 自定义过滤器工厂 - 核心API
     *     ~ exchange.getRequest().mutate().xxx：修改 request
     *     ~ exchange.mutate().xxx：修改 exchange
     *     ~ chain.filter(exchange)：传递给下一个过滤器处理
     *     ~ exchange.getResponse()：获取响应对象
     *     exchange 实际类型为 ServerWebExchange，chain 实际类型为 GatewayFilter
     *
     * · 编写一个自定义过滤器工厂 - 实现功能：记录打印日志
     *     ~ 01).编写自定义过滤器工厂实现类 PreLogGatewayFilterFactory （具体进入查看细节）
     *     ~ 02).application.yml 中配置 ：spring.cloud.gateway.routes.filters.-PreLog （具体进入查看细节）
     *
     * · 全局过滤器（Global Filters）：
     *     ~ 手记：https://www.imooc.com/article/290821
     *     LoadBalancerClient Filter 和 Netty Write Response Filter 特别重要 TODO 学习一下
     *
     * · 进阶：再谈过滤器的执行顺序
     *     ~ 结论1：Order 越小越靠前执行
     *     ~ 结论2：局部过滤器工厂的 Order 按配置顺序从 1 开始递增
     *     ~ 结论3：如果配置了默认过滤器，则先执行相同 Order 的默认过滤器，再执行相同 Order 的自定义过滤器
     *     ~ 结论4：如需自行控制 Order ，可返回 OrderGatewayFilter
     *              PreLogGatewayFilterFactory 实现了自行控制 Order
     *     ~ 核心源码：
     *         - org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator#loadGatewayFilters
     *              为过滤器设置了 Order 数值，从1开始
     *         - org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator#getFilters
     *              加载默认过滤器 & 路由过滤器，并对过滤器做了排序
     *         - org.springframework.web.server.handler.FilteringWebHandler#handle
     *              构建过滤器链并执行
     *
     * ---
     *
     * · Spring Cloud Gateway 整合 Sentinel (课程没有讲解，自己学习补充)
     *    ~ Sentinel 1.6.0 引入了 Sentinel API Gateway Adapter Common 模块，
     *      此模块中包含网关限流的规则和自定义 API 的实体和管理逻辑：
     *         - 01)、GatewayFlowRule：网关限流规则，这个根据网关的自身的路由场景设计的，
     *              可以针对不同 route 或自定义的 API 分组进行限流，支持针对请求中的参数、Header、来源 IP 等进行定制化的限流。
     *         - 02)、ApiDefinition：用户自定义的 API 定义分组，可以看做是一些 URL 匹配的组合。
     *              比如我们可以定义一个 API 叫 myapi，请求 path 模式为 /foo/** 和 /baz/** 的都归到 myapi 这个 API 分组下面。
     *              限流的时候可以针对这个自定义的 API 分组维度进行限流。
     *              网关限流规则 GatewayFlowRule
     *    ~ 字段解释如下：
     *         - 01)、resource：资源名称 : 网关中的 route 名称或者用户自定义的API 分组名称。
     *         - 02)、resourceMode : 规则是针对 API Gateway 的 route（RESOURCEMODEROUTEID）还是用户在 Sentinel 中
     *                              自定义的 API 分组（RESOURCEMODECUSTOMAPI_NAME），默认是route。
     *         - 03)、grade : 限流指标维度，同限流规则的grade字段。
     *         - 04)、count : 限流阈值
     *         - 05)、intervalSec : 统计时间窗口，单位是秒，默认是1秒（目前仅对参数限流生效）。
     *         - 06)、controlBehavior : 流量整形的控制效果，同限流规则的 controlBehavior 字段，
     *                                  目前支持快速失败和匀速排队两种模式，默认是快速失败。
     *         - 07)、burst : 应对突发请求时额外允许的请求数目（目前仅对参数限流生效）。
     *         - 08)、maxQueueingTimeoutMs : 匀速排队模式下的最长排队时间，单位是毫秒，仅在匀速排队模式下生效。
     *         - 09)、paramItem 参数限流配置。若不提供，则代表不针对参数进行限流，该网关规则将会被转换成普通流控规则；否则会转换成热点规则。
     *                         其中的字段：parseStrategy：从请求中提取参数的策略，
     *                         目前支持四种模式提取来源：
     *                              * IP（PARAMPARSESTRATEGYCLIENTIP）
     *                              * Host（PARAMPARSESTRATEGYHOST）
     *                              * 任意 Header（PARAMPARSESTRATEGYHEADER）
     *                              * 任意 URL 参数（PARAMPARSESTRATEGYURLPARAM）
     * 	             fieldName：若提取策略选择 Header 模式或 URL 参数模式，则需要指定对应的 header 名称或 URL 参数名称。
     * 	                        pattern 和 matchStrategy：为参数匹配特性预留。
     *              可以通过 GatewayRuleManager.loadRules(rules)手动加载网关规则，
     *              或通过 GatewayRuleManager.register2Property(property)注册动态规则源动态推送（推荐方式）
     *    ~ 网关流控实现原理：
     *      查看 GatewayFlowPrinciple.jpg , 整体流程如下：
     *         - 01)、外部请求进入 API Gateway 时会经过 Sentinel 实现的 filter，其中会依次进行 路由/API 分组匹配、请求属性解析和参数组装。
     *         - 02)、Sentinel 会根据配置的网关流控规则来解析请求属性，并依照参数索引顺序组装参数数组，最终传入 SphU.entry(res, args) 中。
     *         - 03)、Sentinel API Gateway Adapter Common 模块向 Slot Chain 中添加了一个 GatewayFlowSlot，专门用来做网关规则的检查。
     *         - 04)、GatewayFlowSlot 会从 GatewayRuleManager 中提取生成的热点参数规则，根据传入的参数依次进行规则检查。
     *                  若某条规则不针对请求属性，则会在参数最后一个位置置入预设的常量，达到普通流控的效果。
     *               注意：当通过 GatewayRuleManager 加载网关流控规则 GatewayFlowRule时，
     *                      无论是否针对请求属性进行限流，Sentinel 底层都会将网关流控规则转化为热点参数规则 ParamFlowRule，
     *                      存储在GatewayRuleManager 中，与正常的热点参数规则相隔离。
     *                      转换时 Sentinel 会根据请求属性配置，为网关流控规则设置参数索引 idx，并同步到生成的热点参数规则中。
     *    ~ 总结：从Sentinel的1.6.0版本开始，提供了Spring Cloud Gateway的适配模块，可以提供两种资源维度的限流：
     *         - 01)、route 维度：即在Spring配置文件中配置的路由条目，资源名为对应的routeId
     *         - 02)、自定义 API 维度：用户可以利用 Sentinel 提供的 API 来自定义一些 API 分组
     *    ~ 实现步骤：
     *         - 01)、pom.xml 中引入依赖：spring-cloud-alibaba-sentinel-gateway 和 spring-cloud-starter-alibaba-sentinel
     *         - 02)、application.yml 文件添加 sentinel 服务地址
     *                spring.cloud.sentinel.transport 和 spring.cloud.sentinel.eager
     *         - 03)、创建暴露接口实现类 ： RulesController
     *         - 04)、测试地址：http://localhost:8040/api
     *         保留参数博客地址：https://www.cnblogs.com/cloudxlr/p/11834902.html
     *
     * ---
     *
     * · 监控 Spring Cloud Gateway
     *     ~ 手记：https://www.imooc.com/article/290822
     *
     * · Spring Cloud Gateway 排错、调试技巧总结
     *     ~ 手记：https://www.imooc.com/article/290824
     *
     * · Spring Cloud Gateway 限流详解
     *     ~ 手记：https://www.imooc.com/article/290828
     *     ~ Spring Cloud Gateway内置的 RequestRateLimiterGatewayFilterFactory 提供限流的能力，基于令牌桶算法实现。
     *          目前，它内置的 RedisRateLimiter ，依赖Redis存储限流配置，以及统计数据。
     *          当然你也可以实现自己的RateLimiter，只需实现 org.springframework.cloud.gateway.filter.ratelimit.RateLimiter 接口，
     *                                      或者继承 org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter 。
     *     ~ 算法：包括 漏桶算法 和 令牌桶算法
     *         - 01)、漏桶算法 ：想象有一个水桶，水桶以一定的速度出水（以一定速率消费请求），当水流速度过大水会溢出（访问速率超过响应速率，就直接拒绝）。
     *                          漏桶算法的两个变量：水桶漏洞的大小：rate 、最多可以存多少的水：burst
     *         - 02)、令牌桶算法 ：系统按照恒定间隔向水桶里加入令牌（Token），如果桶满了的话，就不加了。
     *                          每个请求来的时候，会拿走1个令牌，如果没有令牌可拿，那么就拒绝服务。
     *    ~ 实现步骤：
     *         - 01)、pom.xml 中引入依赖：spring-boot-starter-data-redis-reactive
     *         - 02)、application.yml 文件添加 redis 和 RequestRateLimiter
     *                  spring.redis
     *                  spring.cloud.gateway.routes.filters:- name: RequestRateLimiter
     *         - 03)、编写配置类 ：RateConfiguration
     *
     * ---
     *
     * · JWT : JWT 全称是Json Web Token , 是一个开放标准（RFC 7519）, 用来在各方之间安全地传输信息。
     *          JWT 可被验证和信任，因为它是数字签名的。
     */

}
