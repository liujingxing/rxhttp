package rxhttp.wrapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过该注解，会在RxHttp类中，生成一个方法，用于为某个请求指定单独的OkHttpClient对象，方法取名规则: set+注解上指定的名称
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface OkClient {

    String name() default "";

    //通过该方法将生成RxXxxHttp类，通过该类发请求，将默认使用指定的OkHttpClient对象
    String className() default "";
}
