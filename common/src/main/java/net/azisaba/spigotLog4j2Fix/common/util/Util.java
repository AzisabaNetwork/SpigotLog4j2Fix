package net.azisaba.spigotLog4j2Fix.common.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.azisaba.spigotLog4j2Fix.common.SpigotLog4j2Fix;
import net.blueberrymc.native_util.ClassDefinition;
import net.blueberrymc.native_util.NativeUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Util {
    private static final Map<String, ThrowableFunction<CtClass, byte[]>> queue = new HashMap<>();

    static {
        NativeUtil.registerClassLoadHook((classLoader, s, aClass, protectionDomain, bytes) -> {
            String cn = s.replace('/', '.');
            if (queue.containsKey(cn)) {
                try {
                    byte[] b = queue.remove(cn).apply(ClassPool.getDefault().get(cn));
                    SpigotLog4j2Fix.getLogger().info("Transformed " + cn);
                    return b;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }

    /**
     * @param clazz class name which contains dot, not slash.
     */
    public static void redefineClass(String clazz, ThrowableFunction<CtClass, byte[]> function) {
        Class<?> loaded = Arrays.stream(NativeUtil.getLoadedClasses()).filter(c -> c.getTypeName().equals(clazz)).findFirst().orElse(null);
        if (loaded != null) {
            try {
                NativeUtil.redefineClasses(new ClassDefinition[] {new ClassDefinition(loaded, function.apply(ClassPool.getDefault().get(clazz)))});
                SpigotLog4j2Fix.getLogger().info("Redefined " + clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            queue.put(clazz, function);
        }
    }

    public static void redefineClass(Class<?> clazz, ThrowableFunction<CtClass, byte[]> function) {
        try {
            NativeUtil.redefineClasses(new ClassDefinition[] { new ClassDefinition(clazz, function.apply(ClassPool.getDefault().get(clazz.getTypeName()))) });
            SpigotLog4j2Fix.getLogger().info("Redefined " + clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void transformClass(String clazz, ThrowableFunction<CtClass, byte[]> function) {
        Class<?> loaded = Arrays.stream(NativeUtil.getLoadedClasses()).filter(c -> c.getTypeName().equals(clazz)).findFirst().orElse(null);
        if (loaded != null) {
            throw new RuntimeException(clazz + " is already loaded");
        } else {
            queue.put(clazz, function);
        }
    }

    @NotNull
    public static CtConstructor getCtConstructor(@NotNull Constructor<?> constructor) throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get(constructor.getDeclaringClass().getTypeName());
        return cc.getConstructor(toBytecodeSignature(constructor));
    }

    @NotNull
    public static CtMethod getCtMethod(@NotNull Method method) throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get(method.getDeclaringClass().getTypeName());
        return cc.getMethod(method.getName(), toBytecodeSignature(method));
    }

    @NotNull
    public static CtField getCtField(@NotNull Field field) throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get(field.getDeclaringClass().getTypeName());
        return cc.getField(field.getName(), toBytecodeSignature(field));
    }

    @NotNull
    public static String toBytecodeSignature(@NotNull Member member) {
        if (member instanceof Executable) {
            Executable executable = (Executable) member;
            StringBuilder sig = new StringBuilder("(");
            for (Class<?> clazz : executable.getParameterTypes()) {
                sig.append(toBytecodeTypeName(clazz));
            }
            sig.append(")");
            if (executable instanceof Method) {
                sig.append(toBytecodeTypeName(((Method) executable).getReturnType()));
            } else {
                // Constructor
                sig.append("V");
            }
            return sig.toString();
        } else if (member instanceof Field) {
            Field field = (Field) member;
            return toBytecodeTypeName(field.getType());
        } else {
            throw new RuntimeException("Unsupported member type: " + member.getClass().getTypeName());
        }
    }

    @NotNull
    public static String buildBytecodeSignature(@NotNull Class<?> returnValue, @NotNull Class<?>@NotNull... params) {
        StringBuilder sig = new StringBuilder("(");
        for (Class<?> clazz : params) {
            sig.append(toBytecodeTypeName(clazz));
        }
        sig.append(")");
        sig.append(toBytecodeTypeName(returnValue));
        return sig.toString();
    }

    @NotNull
    public static String toBytecodeTypeName(@NotNull Class<?> clazz) {
        return toBytecodeTypeName("", clazz);
    }

    @NotNull
    public static String toBytecodeTypeName(@NotNull String prefix, @NotNull Class<?> clazz) {
        if (clazz.isArray()) {
            // resolve array type recursively
            return toBytecodeTypeName(prefix + "[", clazz.getComponentType());
        }
        String name = clazz.getTypeName();
        switch (name) {
            case "boolean": return prefix + "Z";
            case "byte": return prefix + "B";
            case "char": return prefix + "C";
            case "double": return prefix + "D";
            case "float": return prefix + "F";
            case "int": return prefix + "I";
            case "long": return prefix + "J";
            case "short": return prefix + "S";
            case "void": return prefix + "V";
            default: return prefix + "L" + name.replace(".", "/") + ";";
        }
    }

    @NotNull
    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    @NotNull
    public static String getImplVersion() {
        String v = getServerVersion();
        if (v.equals("v1_12_R1")) return v;
        if (v.equals("v1_17_R1") || v.equals("v1_18_R1")) return "v1_17";
        throw new RuntimeException("Unsupported version: " + v);
    }
}
