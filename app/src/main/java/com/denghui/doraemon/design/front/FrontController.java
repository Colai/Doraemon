package com.denghui.doraemon.design.front;

/**
 * 前端控制器模式 ：FrontControl 用来提供一个集中的亲求处理机制，所有的请求将由一个单一的处理程序处理
 * <p>
 * 该处理可以做 认证，授权记录日志，跟踪请求，调度请求给相应的处理器处理
 */
public class FrontController {
    private Dispatcher dispatcher;

    public FrontController() {
        dispatcher = new Dispatcher();
    }

    private boolean conditionA() {
        return true;
    }

    private boolean conditionB() {
        return false;
    }

    private void track(String condition) {

    }

    public void dispatchRequest(String request) {
        track(request);
        if (conditionA() || conditionB()) {
            dispatcher.dispatch(request);
        }
    }

}
