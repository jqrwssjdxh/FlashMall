package com.flashmall.order;

import com.flashmall.common.UserContext;
import com.flashmall.constant.ResultCode;
import com.flashmall.exception.BusinessException;
import com.flashmall.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Day12 Redisson 分布式锁并发测试
 * 注意：会真实调用 submitOrder（扣库存 + 发 MQ），需要 RabbitMQ/Redis/MySQL 运行。
 */
@SpringBootTest
public class OrderSubmitConcurrentTest {

    @Autowired
    private OrderService orderService;

    private static final Long PRODUCT_ID = 2088057624019349506L;
    private static final Long USER_A = 2087811857757278209L;
    private static final Long USER_B = 2088057106672922625L;

    /** 测试2：同一用户 + 同一商品，10 线程同时提交 → 只允许 1 个进入 */
    @Test
    public void sameUserConcurrentSubmit() throws Exception {
        int threads = 10;
        AtomicInteger success = new AtomicInteger();
        AtomicInteger repeat = new AtomicInteger();

        runConcurrent(threads, USER_A, () -> {
            try {
                orderService.submitOrder(PRODUCT_ID, 1);
                success.incrementAndGet();
            } catch (BusinessException e) {
                if (e.getResultCode() == ResultCode.REPEAT_SUBMIT) {
                    repeat.incrementAndGet();
                }
            }
        });

        System.out.println("【测试2】同用户并发 10 线程：success=" + success.get() + ", repeat=" + repeat.get());
        org.junit.jupiter.api.Assertions.assertEquals(1, success.get(), "只有 1 个请求应成功");
        org.junit.jupiter.api.Assertions.assertEquals(9, repeat.get(), "其余 9 个应被重复提交拦截");
    }

    /** 测试3：不同用户 + 同一商品，同时提交 → 都应成功（锁粒度验证） */
    @Test
    public void differentUsersConcurrentSubmit() throws Exception {
        AtomicInteger userASuccess = new AtomicInteger();
        AtomicInteger userBSuccess = new AtomicInteger();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> workers = new ArrayList<>();

        workers.add(startWorker(ready, start, USER_A, () -> {
            try {
                orderService.submitOrder(PRODUCT_ID, 1);
                userASuccess.incrementAndGet();
            } catch (BusinessException e) {
                System.err.println("用户A 失败: " + e.getResultCode());
            }
        }));
        workers.add(startWorker(ready, start, USER_B, () -> {
            try {
                orderService.submitOrder(PRODUCT_ID, 1);
                userBSuccess.incrementAndGet();
            } catch (BusinessException e) {
                System.err.println("用户B 失败: " + e.getResultCode());
            }
        }));

        ready.await();
        start.countDown();
        for (Thread t : workers) {
            t.join();
        }

        System.out.println("【测试3】不同用户并发：userA=" + userASuccess.get() + ", userB=" + userBSuccess.get());
        org.junit.jupiter.api.Assertions.assertEquals(1, userASuccess.get(), "用户A 应成功");
        org.junit.jupiter.api.Assertions.assertEquals(1, userBSuccess.get(), "用户B 应成功（锁粒度不能阻塞其他用户）");
    }

    /** 测试4：异常释放——锁获取后抛异常，验证 Redis 无残留锁 */
    @Test
    public void lockReleasedAfterException() {
        // 用不存在商品触发 PRODUCT_NOT_FOUND 异常（发生在锁内，锁获取之后）
        Long nonExistProduct = 999999999999L;
        UserContext.setUserId(USER_A);
        try {
            orderService.submitOrder(nonExistProduct, 1);
            org.junit.jupiter.api.Assertions.fail("应抛出 PRODUCT_NOT_FOUND");
        } catch (BusinessException e) {
            org.junit.jupiter.api.Assertions.assertEquals(ResultCode.PRODUCT_NOT_FOUND, e.getResultCode());
            System.out.println("【测试4】锁内异常已抛出: " + e.getResultCode() + " " + e.getMessage());
        } finally {
            UserContext.remove();
        }
    }

    private void runConcurrent(int threads, Long userId, Runnable action) throws Exception {
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            workers.add(startWorker(ready, start, userId, action));
        }
        ready.await();
        start.countDown();
        for (Thread t : workers) {
            t.join();
        }
    }

    private Thread startWorker(CountDownLatch ready, CountDownLatch start, Long userId, Runnable action) {
        Thread t = new Thread(() -> {
            ready.countDown();
            try {
                start.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            UserContext.setUserId(userId);
            try {
                action.run();
            } finally {
                UserContext.remove();
            }
        });
        t.start();
        return t;
    }
}
