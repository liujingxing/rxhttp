package rxhttp.wrapper.utils;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UtilsTest {

    private static class GenericTypes<T extends Number> {
        List<? extends Number> upperBound;
        List<? super Integer> lowerBound;
        T[] genericArray;
    }

    @Test
    public void wrapTypeMatchesLegacyImplementation() throws Exception {
        List<Type> types = new ArrayList<>();
        Class<?>[] primitives = {
            boolean.class, char.class, byte.class, short.class, int.class,
            float.class, long.class, double.class, void.class
        };
        Class<?>[] wrappers = {
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class,
            Float.class, Long.class, Double.class, Void.class
        };
        for (int i = 0; i < primitives.length; i++) {
            types.add(primitives[i]);
            types.add(wrappers[i]);
            if (primitives[i] != void.class) {
                types.add(java.lang.reflect.Array.newInstance(primitives[i], 0).getClass());
            }
        }
        types.add(Object.class);
        types.add(String.class);
        types.add(List.class);
        types.add(kotlin.Unit.class);
        types.add(String[].class);
        types.add(int[][].class);
        types.add(GenericTypes.class.getTypeParameters()[0]);
        types.add(GenericTypes.class.getDeclaredField("genericArray").getGenericType());
        for (String field : new String[]{"upperBound", "lowerBound"}) {
            java.lang.reflect.ParameterizedType type = (java.lang.reflect.ParameterizedType)
                GenericTypes.class.getDeclaredField(field).getGenericType();
            types.add(type);
            types.add(type.getActualTypeArguments()[0]);
        }
        types.add(new Type() {});

        for (Type type : types) {
            Type actual = Utils.getWrapType(type);
            Assert.assertSame("Legacy result differs for " + type, legacyWrapType(type), actual);
            if (!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive()) {
                Assert.assertSame("Non-primitive Type must stay unchanged: " + type, type, actual);
            }
        }
    }

    // Preserve the original implementation as the reference for this regression test.
    private static Type legacyWrapType(Type type) {
        if (!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive()) return type;
        switch (((Class<?>) type).getName()) {
            case "boolean": return Boolean.class;
            case "char": return Character.class;
            case "byte": return Byte.class;
            case "short": return Short.class;
            case "int": return Integer.class;
            case "float": return Float.class;
            case "long": return Long.class;
            case "double": return Double.class;
            case "void": return Void.class;
            default: return type;
        }
    }
}
