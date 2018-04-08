package com.jack.test.myspringmvc;

import com.jack.test.myspringmvc.annotation.MyController;
import com.jack.test.myspringmvc.annotation.MyRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created by jie on 2018/4/5.
 * 本框架核心入口拦截类
 */
public class MyDispatcherServlet extends HttpServlet{

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();


    private Map<String,Object> controllerIOC = new HashMap<>();
    private Map<String,Object> controllerUrlBean = new HashMap<>();

    private Map<String, Method> handlerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //获得配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //扫描包
        doScanner(properties.getProperty("basepackage"));
        //实例化扫描包中的类即controller，反射机制
        doInstance();
        //路由URL与handler的关系
        doHandlerMapping();
    }

    /**
     * 缓存URL与方法映射
     */
    private void doHandlerMapping() {
        if(controllerIOC.size() > 0){
            for (Map.Entry<String, Object> controller : controllerIOC.entrySet()) {
                Class clazz = controller.getValue().getClass();
                if(clazz.isAnnotationPresent(MyController.class)){
                    String baseUrl = "";
                    if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                        MyRequestMapping myRequestMapping = (MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class);
                        baseUrl = myRequestMapping.value();
                    }

                    Method[] methods = clazz.getMethods();
                    String url = null;
                    for (Method method : methods) {
                        if(method.isAnnotationPresent(MyRequestMapping.class)){
                            MyRequestMapping methodRequestMapping = method.getAnnotation(MyRequestMapping.class);
                            url = (baseUrl + "/" + methodRequestMapping.value()).replaceAll("/+", "/");
                            handlerMap.put(url,method);
                            //TODO 这里可以配置单例还是多例
                            controllerUrlBean.put(url, controller.getValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * 将扫描到的文件反射实例化
     */
    private void doInstance() {
        if(classNames != null && classNames.size() > 0){
            Class aClass = null;
            for (String className : classNames) {
                try {
                    aClass = Class.forName(className);
                    //判断只有带注解的才给实例化
                    if(aClass.isAnnotationPresent(MyController.class)){
                        Object instance = aClass.newInstance();
                        //如果有别名,优先存别名
                        MyController annotation = (MyController) aClass.getAnnotation(MyController.class);
                        if(!"".equals(annotation.value())){
                            controllerIOC.put(annotation.value(), instance);
                        }else{
                            controllerIOC.put(firtWordLower(aClass.getSimpleName()), instance);
                        }
                    }else {
                        continue;
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String firtWordLower(String value){
        char[] chars = value.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    //扫描指定目录下的类，识别controller并缓存
    private void doScanner(String basepackage) {
        //将基础包路径读取成URL
        URL url = this.getClass().getClassLoader().getResource(basepackage.replace(".","/"));
        System.out.println("doScanner basepackage url:" + url.getFile());

        //循环
        File file = new File(url.getFile());
        for (File file1 : file.listFiles()) {
            if(file1.isDirectory()){
                doScanner(basepackage + "." + file1.getName());
            }else {
                String className = basepackage + "." + file1.getName().replace(".class", "");
                System.out.println("doScanner className : " + className);
                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置项
     * @param location
     */
    private void doLoadConfig(String location) {
        if(location.startsWith("classpath:")){
            location = location.substring(location.indexOf(":")+1);
        }

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(location)){
            this.properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setHeader("Content-type", "text/html;charset=UTF-8");
            doDispatch(req,  resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 服务器错误");
        }
    }

    /**
     * 路由并处理请求
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        url = url.replace(req.getContextPath(),"").replaceAll("/+","/");
        Method method = handlerMap.get(url);
        if(method == null){
            resp.getWriter().write("404 no route mapping for this url : " + url);
            return;
        }


        //装配方法参数
        Class[] paramTypes = method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];

        Map<String, String[]> requestparameterMap = req.getParameterMap();

        for (int i = 0; i < paramValues.length; i++) {
            if(paramTypes[i].getSimpleName().equals("HttpServletRequest")){
                paramValues[i] = req;
                continue;
            }
            if(paramTypes[i].getSimpleName().equals("HttpServletResponse")){
                paramValues[i] = resp;
                continue;
            }

            if (paramTypes[i].getSimpleName().equals("String")) {
                for (Map.Entry<String, String[]> param : requestparameterMap.entrySet()) {
                    System.out.println("param "+param);
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    System.out.println("value "+value);
                    paramValues[i] = value;
                }
            }
        }

        //反射调用方法
        method.invoke(controllerUrlBean.get(url), paramValues);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }
}
