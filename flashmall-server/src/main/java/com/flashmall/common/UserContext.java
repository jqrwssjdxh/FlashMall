package com.flashmall.common;


public class UserContext {

    private static final ThreadLocal<Long> threadLocal =
            new ThreadLocal<>();

    public static void setUserId(Long userId) {

        threadLocal.set(userId);

    }

    public static Long getUserId() {

        return threadLocal.get();

    }

    public static void remove() {

        threadLocal.remove();

    }

}




