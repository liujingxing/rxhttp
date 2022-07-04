package rxhttp.wrapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Parser {

    /**
     * @return parser name
     */
    String name();

    /**
     * 该参数生效条件：
     * 1、解析器onParse方法返回类型泛型数量有且仅有1个
     * 2、项目有依赖RxJava
     * <p>
     * 生效后，仅会在RxHttp类下生成asXxx方法
     *
     * @return Class数组
     */
    Class<?>[] wrappers() default {};
}
