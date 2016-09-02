package com.peaceful.task.core;

import com.google.common.base.Preconditions;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.peaceful.task.core.conf.TaskConfigOps;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Task系统入口
 * Created by wangjun on 16-8-28.
 */
public class Task {

    // 当前Task实例的重要上下文信息
    private TaskContext context;
    // Task代理类实例
    private Map<Class, Object> proxyInstance = new HashMap<Class, Object>();
    // 多个Task实例,一般一个程序中只有一个Task实例
    private static Map<String, TaskContext> contextMap = new HashMap<String, TaskContext>();

    /**
     * @return 创建一个新的Task实例,如果同名的Task实例已经存在,将抛出重复创建异常
     */
    public synchronized static Task create() {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new TaskModule());
        Task task = new Task();
        task.context = new NewTaskContext(injector);
        contextMap.put(injector.getInstance(TaskConfigOps.class).name, task.context);
        return task;
    }

    /**
     * 注册Java原生类到容器中,通过容器获取到的Java实例,可以通过Task调度器异步执行
     *
     * @param clszz
     * @param <T>
     * @return Task代理实例
     */
    public <T> T registASyncClass(Class<T> clszz) {
        if (proxyInstance.containsKey(clszz)) {
            return (T) proxyInstance.get(clszz);
        } else {
            proxyInstance.put(clszz, context.getTaskProxy().getProxyInstance(clszz));
        }
        return (T) proxyInstance.get(clszz);
    }

    /**
     * 根据Task实例名返回上下文,这是一个内部方法
     * @param name
     * @return
     */
    public static TaskContext getTaskContext(String name) {
        return contextMap.get(name);
    }


   /* public void scheduleOnce(long delay, TimeUnit timeUnit, Runnable runnable) {
        AKKA.scheduler().scheduleOnce(FiniteDuration.apply(delay, timeUnit), runnable, AKKA.dispatcher());
    }


    public void schedule(long delay, long interval, TimeUnit timeUnit, Runnable runnable) {
        AKKA.scheduler().schedule(FiniteDuration.apply(delay, timeUnit), FiniteDuration.apply(interval, timeUnit), runnable, AKKA.dispatcher());
    }*/
}
