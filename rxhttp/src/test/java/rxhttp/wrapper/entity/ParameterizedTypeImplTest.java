package rxhttp.wrapper.entity;


import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * User: ljx
 * Date: 2023/7/3
 * Time: 15:54
 */
public class ParameterizedTypeImplTest {

    @Test
    public void testGet() {
        //boolean.class
        ParameterizedType parameterizedType = ParameterizedTypeImpl.get(List.class, boolean.class);
        Type[] types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Boolean.class, types[0]);
        Assert.assertEquals(List.class, parameterizedType.getRawType());

        //char.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, char.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Character.class, types[0]);

        //byte.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, byte.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Byte.class, types[0]);

        //short.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, short.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Short.class, types[0]);

        //int.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, int.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Integer.class, types[0]);

        //long.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, long.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Long.class, types[0]);

        //float.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, float.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Float.class, types[0]);

        //double.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, double.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Double.class, types[0]);

        //void.class
        parameterizedType = ParameterizedTypeImpl.get(List.class, void.class);
        types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(Void.class, types[0]);
    }

    @Test
    public void getParameterized() {
        ParameterizedType parameterizedType = ParameterizedTypeImpl.getParameterized(Map.class, int.class, long.class);
        Type[] types = parameterizedType.getActualTypeArguments();
        Assert.assertEquals(2, types.length);
        Assert.assertEquals(Integer.class, types[0]);
        Assert.assertEquals(Long.class, types[1]);
        Assert.assertEquals(Map.class, parameterizedType.getRawType());
    }
}