package com.shuanglin.train.service;

import com.shuanglin.train.TrainApplication;
import com.shuanglin.train.exception.ModelException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelRetryService 测试
 */
@SpringBootTest(classes = TrainApplication.class)
@TestPropertySource(properties = {
        "milvus.enable=false",
        "spring.autoconfigure.exclude=org.dromara.milvus.plus.service.MilvusInit",
        "train.model.cache-enabled=false",
        "train.model.retry.max-attempts=3",
        "train.model.retry.initial-delay-ms=10",
        "train.model.retry.max-delay-ms=100"
})
public class ModelRetryServiceTest {

    @Autowired
    private ModelRetryService retryService;

    @Test
    void testExecuteWithRetrySuccess() {
        String modelId = "success-test";
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retryService.executeWithRetry(
                () -> {
                    attempts.incrementAndGet();
                    return "success";
                },
                3,
                modelId
        );

        assertEquals("success", result);
        assertEquals(1, attempts.get());
    }

    @Test
    void testExecuteWithRetryWithFailures() {
        String modelId = "fail-then-success-test";
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retryService.executeWithRetry(
                () -> {
                    attempts.incrementAndGet();
                    if (attempts.get() < 2) {
                        throw new ModelException(modelId, "temp failure",
                                ModelException.ErrorType.RESOURCE_EXHAUSTED);
                    }
                    return "success-after-fail";
                },
                3,
                modelId
        );

        assertEquals("success-after-fail", result);
        assertEquals(2, attempts.get());
    }

    @Test
    void testExecuteWithRetryNonRetryableError() {
        String modelId = "non-retryable-test";

        assertThrows(ModelException.class, () -> {
            retryService.executeWithRetry(
                    () -> {
                        throw new ModelException(modelId, "not found",
                                ModelException.ErrorType.MODEL_NOT_FOUND);
                    },
                    3,
                    modelId
            );
        });
    }

    @Test
    void testExecuteWithRetryMaxAttemptsExceeded() {
        String modelId = "max-attempts-test";
        AtomicInteger attempts = new AtomicInteger(0);

        assertThrows(ModelException.class, () -> {
            retryService.executeWithRetry(
                    () -> {
                        attempts.incrementAndGet();
                        throw new ModelException(modelId, "always fails",
                                ModelException.ErrorType.RESOURCE_EXHAUSTED);
                    },
                    2, // max 2 retries
                    modelId
            );
        });

        // Should have attempted 3 times (1 initial + 2 retries)
        assertEquals(3, attempts.get());
    }

    @Test
    void testExecuteWithRetryAsync() throws ExecutionException, InterruptedException {
        String modelId = "async-test";

        CompletableFuture<String> future = retryService.executeWithRetryAsync(
                () -> "async-result",
                3,
                modelId
        );

        assertEquals("async-result", future.get());
    }

    @Test
    void testExecuteWithRetryIOException() {
        String modelId = "io-exception-test";
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retryService.executeWithRetry(
                () -> {
                    attempts.incrementAndGet();
                    if (attempts.get() < 2) {
                        throw new IOException("connection refused");
                    }
                    return "success";
                },
                3,
                modelId
        );

        assertEquals("success", result);
        assertEquals(2, attempts.get());
    }

    @Test
    void testGetRetryStats() {
        var stats = retryService.getRetryStats();

        assertNotNull(stats);
        // Stats should be empty initially or contain recent entries
        assertTrue(stats instanceof java.util.concurrent.ConcurrentHashMap);
    }

    @Test
    void testClearStats() {
        // Clear stats should not throw
        assertDoesNotThrow(() -> retryService.clearStats());
    }

    @Test
    void testRetryTracker() {
        ModelRetryService.RetryTracker tracker = new ModelRetryService.RetryTracker();

        assertEquals(0, tracker.getTotalAttempts());
        assertEquals(0, tracker.getSuccessCount());
        assertEquals(0, tracker.getFailCount());

        tracker.record(3, null); // 3 attempts, success
        assertEquals(3, tracker.getTotalAttempts());
        assertEquals(1, tracker.getSuccessCount());

        tracker.record(2, new RuntimeException("error")); // 2 attempts, failure
        assertEquals(5, tracker.getTotalAttempts());
        assertEquals(1, tracker.getFailCount());
    }
}
