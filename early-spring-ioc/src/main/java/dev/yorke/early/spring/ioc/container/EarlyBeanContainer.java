package dev.yorke.early.spring.ioc.container;

import dev.yorke.early.spring.ioc.annotation.EarlyAutowired;
import dev.yorke.early.spring.ioc.annotation.EarlyComponent;
import dev.yorke.early.spring.ioc.annotation.EarlyConfiguration;
import dev.yorke.early.spring.ioc.exception.IocException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yorke
 */
public class EarlyBeanContainer implements BeanContainer {
    private static final Logger log = LoggerFactory.getLogger(BeanContainer.class);

    private static final String CLASS_FILE_POSTFIX = ".class";

    private Map<String, Object> iocBeanMap = new HashMap<>();

    public EarlyBeanContainer() throws IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException {
        loadBeans("");
    }

    public EarlyBeanContainer(String scanPackage) throws IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException {
        loadBeans(scanPackage);
    }

    @Override
    public Object getBean(String beanName) {
        return iocBeanMap.get(beanName);
    }

    @Override
    public boolean containsBean(String beanName) {
        return iocBeanMap.containsKey(beanName);
    }

    private void loadBeans(String scanPackage) throws IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException {
        // 扫描指定包下的类
        log.info("正在扫描类...");
        Set<String> classSet = scanClasses(scanPackage.replace(".", "/"));
        // 装载控制反转的类
        log.info("正在装载 bean...");
        loadIocBeans(classSet);
        // 依赖注入
        log.info("正在注入依赖...");
        dependencyInject();
        log.info("容器初始化完成");
    }

    /**
     * 扫描指定包下的类
     * @param classPath 根路径
     * @return 扫描到的类
     */
    private Set<String> scanClasses(String classPath) {
        Set<String> classSet = new HashSet<>();
        loadClasses(classPath, classSet);
        return classSet;
    }

    /**
     * 递归扫描目录下的类
     * @param currentPath 当前路径
     * @param classSet 类容器
     */
    private void loadClasses(String currentPath, Set<String> classSet) {
        try {
            URL url = Optional.ofNullable(this.getClass().getClassLoader().getResource(currentPath))
                    .orElseThrow(() -> new IocException("类路径加载失败！"));
            File file = new File(url.getFile());

            if (file.exists() && file.isDirectory()) {
                Arrays.stream(Optional.ofNullable(file.listFiles()).orElse(new File[] {})).forEach(f -> {
                    if (f.isDirectory()) {
                        loadClasses(buildClassPath(currentPath, f.getName()), classSet);
                    } else if (f.getName().endsWith(CLASS_FILE_POSTFIX)) {
                        classSet.add(buildClassPath(currentPath, f.getName().replace(CLASS_FILE_POSTFIX, "")).replace("/", "."));
                    }
                });
            }
        } catch (Exception e) {
            throw new IocException(e.getMessage());
        }
    }

    /**
     * 装载控制反转的类
     */
    private void loadIocBeans(Set<String> classSet) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        for (String className : classSet) {
            Class classZ = Class.forName(className);
            Annotation componentAnnotaion = classZ.getDeclaredAnnotation(EarlyComponent.class);
            if (componentAnnotaion instanceof EarlyComponent) {
                EarlyComponent earlyComponent = (EarlyComponent) componentAnnotaion;
                String beanName = toLowercaseIndex(StringUtils.isNotEmpty(earlyComponent.value()) ? earlyComponent.value() : classZ.getSimpleName());
                iocBeanMap.put(beanName, classZ.newInstance());
            }
            Annotation configurationAnnotaion = classZ.getDeclaredAnnotation(EarlyConfiguration.class);
            if (configurationAnnotaion instanceof EarlyConfiguration) {
                EarlyConfiguration earlyConfiguration = (EarlyConfiguration) configurationAnnotaion;
                String beanName = toLowercaseIndex(StringUtils.isNotEmpty(earlyConfiguration.value()) ? earlyConfiguration.value() : classZ.getSimpleName());
                iocBeanMap.put(beanName, classZ.newInstance());
                loadConfigBeans(classZ, beanName);
            }
        }
    }

    /**
     * 装载配置类中声明的 bean
     * @param classZ 配置类
     * @param classBeanName 配置类的 beanName
     */
    private void loadConfigBeans(Class classZ, String classBeanName) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = classZ.getDeclaredMethods();
        for (Method method : methods) {
            EarlyComponent earlyComponent = method.getAnnotation(EarlyComponent.class);
            if (earlyComponent != null) {
                String beanName = StringUtils.isNotEmpty(earlyComponent.value()) ? earlyComponent.value() : method.getName();
                iocBeanMap.put(beanName, method.invoke(iocBeanMap.get(classBeanName)));
            }
        }
    }

    /**
     * 依赖注入
     */
    private void dependencyInject() throws IllegalAccessException {
        for (Object obj : iocBeanMap.values()) {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                EarlyAutowired demoAutowired = field.getAnnotation(EarlyAutowired.class);
                if (demoAutowired != null) {
                    field.setAccessible(true);
                    field.set(obj, findBeanOfField(field, toLowercaseIndex(demoAutowired.value())));
                }
            }
        }
    }

    /**
     * 查找变量的 bean 实例
     * @param field 变量
     * @param beanName beanName
     * @return bean 实例
     */
    private Object findBeanOfField(Field field, String beanName) {
        // 如果指定了 beanName，直接查找
        if (StringUtils.isNotEmpty(beanName)) {
            return Optional.ofNullable(iocBeanMap.get(beanName))
                    .orElseThrow(() -> new RuntimeException("Bean " + beanName + " not found!"));
        }

        if (field.getType().isInterface()) {
            List<Object> instanceList = iocBeanMap.values().stream()
                    .filter(field.getType()::isInstance)
                    .collect(Collectors.toList());
            if (instanceList.isEmpty()) {
                throw new IocException("Bean of " + field.getName() + " interface not found!");
            } else if (instanceList.size() > 1) {
                throw new IocException("Too many beans of " + field.getName() + " interface!");
            } else {
                return instanceList.get(0);
            }
        } else {
            List<Map.Entry<String, Object>> entryList = iocBeanMap.entrySet().stream()
                    .filter(e -> field.getType().equals(e.getValue().getClass()))
                    .collect(Collectors.toList());
            if (entryList.isEmpty()) {
                throw new IocException("Bean of " + field.getName() + " type not found!");
            } else if (entryList.size() > 1) {
                return entryList.stream()
                        .filter(e -> toLowercaseIndex(field.getName()).equals(e.getKey()))
                        .findAny()
                        .orElseThrow(() -> new RuntimeException("Too many beans of " + field.getName() + " interface!"));
            } else {
                return entryList.get(0);
            }
        }
    }

    private static String buildClassPath(String parentPath, String fileName) {
        String classPath = String.join("/", parentPath, fileName);
        return classPath.startsWith("/") ? classPath.substring(1) : classPath;
    }

    /**
     * 首字母小写
     */
    private static String toLowercaseIndex(String name) {
        return StringUtils.isEmpty(name) ? name : name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}
