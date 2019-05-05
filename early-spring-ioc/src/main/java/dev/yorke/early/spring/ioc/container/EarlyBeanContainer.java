package dev.yorke.early.spring.ioc.container;

import dev.yorke.early.spring.ioc.annotation.EarlyAutowired;
import dev.yorke.early.spring.ioc.annotation.EarlyComponent;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yorke
 */
public class EarlyBeanContainer implements BeanContainer {
    private static final String CLASS_FILE_POSTFIX = ".class";

    private Map<String, Object> iocBeanMap = new HashMap<>();

    public EarlyBeanContainer(String scanPackage) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
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

    private void loadBeans(String scanPackage) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        // 扫描指定包下的类
        Set<String> classSet = scanClasses(scanPackage.replace(".", "/"));
        // 装载控制反转的类
        loadIocBeans(classSet);
        // 依赖注入
        dependencyInject();
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
            URL url = this.getClass().getClassLoader().getResource(currentPath);
            Path path = Paths.get(Objects.requireNonNull(url).toURI());

            if (Files.exists(path) && Files.isDirectory(path)) {
                Files.list(path).forEach(subPath -> {
                    if (Files.isDirectory(subPath)) {
                        loadClasses(String.join("/", currentPath, subPath.getFileName().toString()), classSet);
                    } else if (subPath.getFileName().toString().endsWith(CLASS_FILE_POSTFIX)) {
                        classSet.add(String.join(".", currentPath.replace("/", "."), subPath.getFileName().toString().replace(".class", "")));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 装载控制反转的类
     */
    private void loadIocBeans(Set<String> classSet) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (String className : classSet) {
            Class classZ = Class.forName(className);
            Annotation a = classZ.getDeclaredAnnotation(EarlyComponent.class);
            if (a instanceof EarlyComponent) {
                EarlyComponent demoComponent = (EarlyComponent) a;
                String beanName = toLowercaseIndex(StringUtils.isNotEmpty(demoComponent.value()) ? demoComponent.value() : classZ.getSimpleName());
                iocBeanMap.put(beanName, classZ.newInstance());
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

                    if (StringUtils.isNotEmpty(demoAutowired.value())) {
                        // 对于指定了 beanName 的变量，直接注入对应的 bean
                        String beanName = toLowercaseIndex(demoAutowired.value());
                        Object bean = Optional.ofNullable(iocBeanMap.get(beanName))
                                .orElseThrow(() -> new RuntimeException("Bean " + beanName + " not fount!"));
                        field.set(obj, bean);
                    } else {
                        // 未指定 beanName 的变量，按照约定注入对应的 bean
                        Class<?> fieldType = field.getType();
                        if (fieldType.isInterface()) {
                            // 以接口声明的变量，注入其唯一的实现类 bean
                            List<Object> instanceList = iocBeanMap.values().stream()
                                    .filter(fieldType::isInstance)
                                    .collect(Collectors.toList());
                            if (instanceList.isEmpty()) {
                                throw new RuntimeException("Bean of " + field.getName() + " interface not found!");
                            } else if (instanceList.size() > 1) {
                                throw new RuntimeException("Too many beans of " + field.getName() + " interface!");
                            } else {
                                field.set(obj, instanceList.get(0));
                            }
                        } else {
                            // 以类声明的变量，根据类名注入对应的 bean
                            String beanName = toLowercaseIndex(field.getName());
                            Object bean = Optional.ofNullable(iocBeanMap.get(beanName))
                                    .orElseThrow(() -> new RuntimeException("Bean " + beanName + " not found!"));
                            field.set(obj, bean);
                        }
                    }
                }
            }
        }
    }

    /**
     * 首字母小写
     */
    private static String toLowercaseIndex(String name) {
        return StringUtils.isEmpty(name) ? name : name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}
