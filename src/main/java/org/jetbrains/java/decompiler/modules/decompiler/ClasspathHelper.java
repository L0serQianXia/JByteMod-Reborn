package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClasspathHelper {
    private static final Map METHOD_CACHE = Collections.synchronizedMap(new HashMap());

    public static Method findMethod(String classname, String methodName, MethodDescriptor descriptor) {
        String targetClass = classname.replace('/', '.');
        String methodSignature = buildMethodSignature(targetClass + '.' + methodName, descriptor);
        Method method;
        if (METHOD_CACHE.containsKey(methodSignature)) {
            method = (Method)METHOD_CACHE.get(methodSignature);
        } else {
            method = findMethodOnClasspath(targetClass, methodSignature);
            METHOD_CACHE.put(methodSignature, method);
        }

        return method;
    }

    private static Method findMethodOnClasspath(String targetClass, String methodSignature) {
        try {
            Class cls = (new ClasspathHelper$1((ClassLoader)null)).loadClass(targetClass);
            Method[] var3 = cls.getMethods();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Method mtd = var3[var5];
                if (mtd.toString().contains(methodSignature)) {
                    return mtd;
                }
            }
        } catch (Exception var7) {
        }

        return null;
    }

    private static String buildMethodSignature(String name, MethodDescriptor md) {
        StringBuilder sb = new StringBuilder();
        appendType(sb, md.ret);
        sb.append(' ').append(name).append('(');
        VarType[] var3 = md.params;
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            VarType param = var3[var5];
            appendType(sb, param);
            sb.append(',');
        }

        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }

        sb.append(')');
        return sb.toString();
    }

    private static void appendType(StringBuilder sb, VarType type) {
        sb.append(type.value.replace('/', '.'));

        for(int i = 0; i < type.arrayDim; ++i) {
            sb.append("[]");
        }

    }
}

final class ClasspathHelper$1 extends ClassLoader {
    ClasspathHelper$1(ClassLoader x0) {
        super(x0);
    }
}
