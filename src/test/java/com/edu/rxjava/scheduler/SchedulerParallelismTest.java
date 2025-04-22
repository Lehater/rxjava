package com.edu.rxjava.scheduler;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SchedulerParallelismTest {

    @Test
    void testIOThreadSchedulerParallelExecution() throws InterruptedException {
        IOThreadScheduler scheduler = new IOThreadScheduler();
        int tasks = 5;
        CountDownLatch startLatch = new CountDownLatch(tasks);
        CountDownLatch doneLatch  = new CountDownLatch(tasks);
        Set<String> threadNames = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < tasks; i++) {
            scheduler.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                startLatch.countDown();
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                doneLatch.countDown();
            });
        }

        // ждем, что все задачи запустятся почти одновременно
        assertTrue(startLatch.await(1, TimeUnit.SECONDS), "Tasks did not all start");
        doneLatch.await(2, TimeUnit.SECONDS);

        // должно быть более одного потока
        assertTrue(threadNames.size() > 1,
                "IOThreadScheduler должен использовать несколько потоков, но использовал: " + threadNames);
    }

    @Test
    void testComputationSchedulerParallelExecution() throws InterruptedException {
        int cpus = Runtime.getRuntime().availableProcessors();
        ComputationScheduler scheduler = new ComputationScheduler();
        int tasks = cpus + 2;  // чуть больше CPU
        CountDownLatch startLatch = new CountDownLatch(tasks);
        CountDownLatch doneLatch  = new CountDownLatch(tasks);
        Set<String> threadNames = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < tasks; i++) {
            scheduler.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                startLatch.countDown();
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                doneLatch.countDown();
            });
        }

        assertTrue(startLatch.await(1, TimeUnit.SECONDS), "Tasks did not all start");
        doneLatch.await(2, TimeUnit.SECONDS);

        assertTrue(threadNames.size() > 1,
                "ComputationScheduler должен использовать несколько потоков, но использовал: " + threadNames);
    }

    @Test
    void testSingleThreadSchedulerSequentialExecution() throws InterruptedException {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();
        int tasks = 5;
        CountDownLatch doneLatch = new CountDownLatch(tasks);
        List<String> names = Collections.synchronizedList(new ArrayList<>());
        List<Long>   times = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < tasks; i++) {
            scheduler.execute(() -> {
                names.add(Thread.currentThread().getName());
                times.add(System.nanoTime());
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                doneLatch.countDown();
            });
        }

        assertTrue(doneLatch.await(1, TimeUnit.SECONDS), "Не все задачи завершились");

        // 1) Все задачи — в одном потоке
        long distinct = names.stream().distinct().count();
        assertEquals(1, distinct,
                "SingleThreadScheduler должен использовать ровно один поток, но нашёл: " + names);

        // 2) Задачи выполнены последовательно: времена строго возрастают
        for (int i = 1; i < times.size(); i++) {
            assertTrue(times.get(i) > times.get(i - 1),
                    "Задачи SingleThreadScheduler выполняются не последовательно");
        }
    }
}
