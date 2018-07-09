package com.denghui.doraemon.design.front;

/**
 * 调度器
 */
public class Dispatcher {
    private HomeView homeView;
    private StudentView studentView;

    public Dispatcher(){
        homeView = new HomeView();
        studentView = new StudentView();
    }

    public void dispatch(String str){
        if (str.equals("Home")){
            homeView.show();
        } else {
            studentView.show();
        }
    }

}
