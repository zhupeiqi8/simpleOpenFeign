package com.example.simpleOpenFeign;

import com.example.simpleOpenFeign.annotation.EnableFeignClients;
import com.example.simpleOpenFeign.annotation.FeignClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author zhukaijie
 */
public class FeignClientsRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(EnableFeignClients.class.getName());
        String[] basePackages = (String[]) attrs.get("basePackages");
        if (basePackages == null || basePackages.length == 0) {
            String basePackage = ClassUtils.getPackageName(importingClassMetadata.getClassName());
            basePackages = new String[]{basePackage};
        }

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                // 将接口作为候选组件
                return beanDefinition.getMetadata().isInterface();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(FeignClient.class));
        Set<BeanDefinition> allBeanDefinition = new HashSet<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
            allBeanDefinition.addAll(candidates);
        }

        for (BeanDefinition candidate : allBeanDefinition) {
            String className = candidate.getBeanClassName();
            Class<?> interfaceClass = null;
            try {
                interfaceClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (interfaceClass.isInterface()) {
                FeignClient feignClient = interfaceClass.getAnnotation(FeignClient.class);
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class);
                builder.addPropertyValue("interfaceClass", interfaceClass);
                builder.addPropertyValue("url", feignClient.url());
                String beanName = ClassUtils.getShortNameAsProperty(interfaceClass);
                registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
            }
        }

    }

}
