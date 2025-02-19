package com.example.simpleOpenFeign;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author zhukaijie
 */
public class FeignClientFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware, InitializingBean {

    private Class<T> interfaceClass;

    private String url;

    private RestTemplate restTemplate;

    private ApplicationContext applicationContext;


    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String httpMethod = "GET";
                String path = "";

                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    if (mapping.value().length > 0) {
                        path = mapping.value()[0];
                    }
                    httpMethod = "GET";
                } else if (method.isAnnotationPresent(PostMapping.class)) {
                    PostMapping mapping = method.getAnnotation(PostMapping.class);
                    if (mapping.value().length > 0) {
                        path = mapping.value()[0];
                    }
                    httpMethod = "POST";
                } else if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                    if (mapping.value().length > 0) {
                        path = mapping.value()[0];
                    }
                    RequestMethod[] methods = mapping.method();
                    if (methods.length > 0) {
                        httpMethod = methods[0].name();
                    }
                }

                if (interfaceClass.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping interfaceMapping = interfaceClass.getAnnotation(RequestMapping.class);
                    if (interfaceMapping.value().length > 0) {
                        path = interfaceMapping.value()[0] + path;
                    }
                }

                String fullUrl = url + path;
                if ("GET".equalsIgnoreCase(httpMethod)) {
                    return restTemplate.getForObject(fullUrl, method.getReturnType());
                } else if ("POST".equalsIgnoreCase(httpMethod)) {
                    Object requestBody = (args != null && args.length > 0) ? args[0] : null;
                    return restTemplate.postForObject(fullUrl, requestBody, method.getReturnType());
                } else {
                    throw new UnsupportedOperationException("暂不支持HTTP方法: " + httpMethod);
                }
            }
        });
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }


    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.restTemplate = applicationContext.getBean(RestTemplate.class);
    }
}
