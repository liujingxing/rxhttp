package rxhttp.wrapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认域名使用该注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface DefaultDomain {
}
