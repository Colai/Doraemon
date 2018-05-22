package com.denghui.doraemon.design;

/**
 * 如果一个对象需要频繁的创建和销毁，很影响性能，这时候可以使用 对象池
 */
public class ObtainDesign {

    private static final Object lock = new Object();

    //对象池
    public static int POOL_SIZE = 3;
    private static final ObtainDesign[] cached = new ObtainDesign[POOL_SIZE];

    /**
     * 一堆 成员变量
     * private XXX xx;
     * private XXX xx;
     * private XXX xx
     */

    /**
     * 1。首先从 cache 中查找已经被弃用的但是还没有被销毁的对象
     * 2。找到就直接返回
     * 3。没找到就重新new
     * @return
     */
    public static ObtainDesign obtain() {
        ObtainDesign od;
        synchronized (lock) {
            for (int i = cached.length; --i >= 0; ) {
                if (cached[i] != null) {
                    od = cached[i];
                    cached[i] = null;
                    return od;
                }
            }
        }
        od = new ObtainDesign();
        return od;
    }

    public static void recycle(ObtainDesign od) {
        od.finish();
        synchronized(lock) {
            for (int i = 0; i < cached.length; ++i) {
                if (cached[i] == null) {
                    cached[i] = od;
                    break;
                }
            }
        }
    }

    private void finish(){
        /**
         * 做成员变量置null的操作
         * xxx = null;
         * xxx = null;
         */
    }

}
