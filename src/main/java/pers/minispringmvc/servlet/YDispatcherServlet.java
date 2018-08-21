package pers.minispringmvc.servlet;

import pers.minispringmvc.annotation.YController;
import pers.minispringmvc.annotation.YRequestMapping;

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
 * @author songbao.yang
 */
public class YDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Method> handlerMapping = new HashMap<>();

    private Map<String, Object> controllerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        loadConfig(config.getInitParameter("contextConfigLocation"));

        //2.初始化所有相关联的类,扫描用户设定的包下面所有的类
        scan(properties.getProperty("scanPackage"));

        //3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        instance();

        //4.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (handlerMapping.isEmpty()) {
            return;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }

        Method method = this.handlerMapping.get(url);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Map<String, String[]> parameterMap = req.getParameterMap();
        Object[] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            String requestParam = parameterTypes[i].getSimpleName();
            if (requestParam.equals("HttpServletRequest")) {
                paramValues[i] = req;
                continue;
            }
            if (requestParam.equals("HttpServletResponse")) {
                paramValues[i] = resp;
                continue;
            }
            if (requestParam.equals("String")) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("[|]", "").replaceAll(",s", ",");
                    paramValues[i] = value;
                }
            }
        }
        //利用反射机制来调用
        try {
            method.invoke(this.controllerMap.get(url), paramValues);//第一个参数是method所对应的实例 在ioc容器中
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConfig(String contextConfigLocation) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(contextConfigLocation)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scan(String packageName) {
        //把所有的.替换成/
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        if (url == null){
            return;
        }
        File dir = new File(url.getFile());
        File[] files = dir.listFiles();
        if (files == null || files.length == 0){
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scan(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    private void instance() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(YController.class)) {
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<? extends Object> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(YController.class)) {
                continue;
            }

            //拼url时,是controller头的url拼上方法上的url
            String baseUrl = "";
            if (clazz.isAnnotationPresent(YRequestMapping.class)) {
                YRequestMapping annotation = clazz.getAnnotation(YRequestMapping.class);
                baseUrl = annotation.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(YRequestMapping.class)) {
                    continue;
                }
                YRequestMapping annotation = method.getAnnotation(YRequestMapping.class);
                String url = annotation.value();

                url = (baseUrl + "/" + url).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                try {
                    controllerMap.put(url, clazz.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 把字符串的首字母小写
     *
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

}
