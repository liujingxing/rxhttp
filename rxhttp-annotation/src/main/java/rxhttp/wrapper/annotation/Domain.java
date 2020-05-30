package rxhttp.wrapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Domain {

    String name() default "";

    //通过该方法将生成RxXxxHttp类，通过该类发请求，将默认使用指定的baseUrl
    String className() default "";
}
