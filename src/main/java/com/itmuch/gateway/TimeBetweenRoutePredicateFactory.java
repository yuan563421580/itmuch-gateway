package com.itmuch.gateway;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 自定义谓词工厂 实现
 *
 *  ~ TimeBetween 与 application.yml 中配置的保持名称一致
 *  ~ 固定规则：一定要以 RoutePredicateFactory 结尾
 *  ~ 继承AbstractRoutePredicateFactory<?>
 *      泛型传入一个配置类进行承载 TimeBetweenConfig ，是 实现 【配置类】 和 【配置文件】 的关系
 *      需要实现2个方法：apply 和 shortcutFieldOrder
 *
 *  ~ 技巧：时间可使用 System.out.println(ZonedDateTime.now()); 打印，然后即可看到时区。
 *      例如：2019-08-10T16:50:42.579+08:00[Asia/Shanghai]
 *  ~ 时间格式的相关逻辑：
 *      默认时间格式：org.springframework.format.support.DefaultFormattingConversionService#addDefaultFormatters
 *      时间格式注册：org.springframework.format.datetime.standard.DateTimeFormatterRegistrar#registerFormatters
 *
 *  测试:http://localhost:8040/users/1
 */
@Component
public class TimeBetweenRoutePredicateFactory
    extends AbstractRoutePredicateFactory<TimeBetweenConfig> {

    // 必须创建构造方法，否则会报错，会提示创建，按照需求修改
    public TimeBetweenRoutePredicateFactory() {
        super(TimeBetweenConfig.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(TimeBetweenConfig config) {
        // 自定义谓词工厂的核心方法，控制路由的条件

        LocalTime start = config.getStart();
        LocalTime end = config.getEnd();

        /*return new Predicate<ServerWebExchange>() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                LocalTime now = LocalTime.now();
                return now.isAfter(start) && now.isBefore(end);
            }
        };*/

        // lambda 表达式实现 ，返回的就是一个 Predicate
        return exchange -> {
            LocalTime now = LocalTime.now();
            return now.isAfter(start) && now.isBefore(end);
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        // 实现映射：是 Spring Cloud Gateway 知道，实现了顺序
        // 写入配置类的名称 start 和 end
        return Arrays.asList("start", "end");
    }

    // 查看测试时间类型
    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        System.out.println(formatter.format(LocalTime.now()));
    }
}
