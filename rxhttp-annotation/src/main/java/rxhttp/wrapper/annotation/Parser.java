package rxhttp.wrapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Parser {

    /**
     * @return 解析器名称
     */
    String name();

    /**
     * 解析器泛型的包装类，通过该参数，可以生成任意个asXxx方法
     * @return Class数组
     */
    Class<?>[] wrappers() default {};
}
