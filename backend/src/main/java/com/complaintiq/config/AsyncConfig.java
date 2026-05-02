package com.complaintiq.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
@Slf4j @Configuration @EnableAsync @EnableScheduling
public class AsyncConfig {
    @Value("${app.async.core-pool-size:4}") private int corePoolSize;
    @Value("${app.async.max-pool-size:10}") private int maxPoolSize;
    @Value("${app.async.queue-capacity:500}") private int queueCapacity;
    @Value("${app.async.thread-name-prefix:ComplaintIQ-Async-}") private String threadNamePrefix;
    @Bean(name="taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize); executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity); executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true); executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler((r, exec) -> log.error("Async task rejected — thread pool exhausted."));
        executor.initialize();
        return executor;
    }
}
