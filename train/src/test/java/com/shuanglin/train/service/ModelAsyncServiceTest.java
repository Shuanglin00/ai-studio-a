package com.shuanglin.train.service;

import com.shuanglin.train.TrainApplication;
import com.shuanglin.train.config.ModelExecutorConfig;
import com.shuanglin.train.exception.ModelException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelAsyncService 测试
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
public class ModelAsyncServiceTest {

    @Autowired
    private ModelAsyncService asyncService;

    @Autowired
    private ModelExecutorConfig executorConfig;

    @Test
    void testGetQueueStatus() {
        var status = asyncService.getQueueStatus();

        assertNotNull(status);
        assertTrue(status.queueSize() >= 0);
        assertTrue(status.activeCount() >= 0);
        assertTrue(status.remainingCapacity() >= 0);
    }

    @Test
    void testExecuteSync() {
        String modelId = "test-model";

        String result = asyncService.executeSync(modelId, mid -> {
            return "result-" + mid;
        }, 1);

        assertEquals("result-" + modelId, result);
    }

    @Test
    void testExecuteSyncWithRetry() {
        String modelId = "retry-test-model";
        AtomicInteger attempts = new AtomicInteger(0);

        String result = asyncService.executeSync(modelId, mid -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new ModelException(mid, "temporary failure",
                        ModelException.ErrorType.RESOURCE_EXHAUSTED);
            }
            return "success-after-retry";
        }, 3);

        assertEquals("success-after-retry", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void testExecuteSyncMaxRetriesExceeded() {
        String modelId = "max-retry-test";

        assertThrows(ModelException.class, () -> {
            asyncService.executeSync(modelId, mid -> {
                throw new ModelException(mid, "always fail",
                        ModelException.ErrorType.RESOURCE_EXHAUSTED);
            }, 2);
        });
    }

    @Test
    void testExecuteAsync() throws ExecutionException, InterruptedException {
        String modelId = "async-test-model";

        CompletableFuture<String> future = asyncService.executeAsync(
                modelId,
                mid -> "async-result-" + mid,
                1
        );

        String result = future.get();
        assertEquals("async-result-" + modelId, result);
    }

    @Test
    void testExecuteBatch() throws ExecutionException, InterruptedException {
        String modelId = "batch-test-model";

        List<CompletableFuture<String>> futures = asyncService.executeBatch(
                modelId,
                List.of(
                        mid -> "result-1",
                        mid -> "result-2",
                        mid -> "result-3"
                ),
                1
        );

        assertEquals(3, futures.size());

        for (int i = 0; i < futures.size(); i++) {
            String result = futures.get(i).get();
            assertEquals("result-" + (i + 1), result);
        }
    }

    @Test
    void testExecuteAsyncWithCallback() throws ExecutionException, InterruptedException {
        String modelId = "callback-test-model";
        AtomicInteger callbackInvoked = new AtomicInteger(0);
        String[] callbackResult = new String[1];
        Throwable[] callbackError = new Throwable[1];

        asyncService.executeAsync(
                modelId,
                mid -> "callback-result",
                1,
                (result, error) -> {
                    callbackInvoked.incrementAndGet();
                    callbackResult[0] = result;
                    callbackError[0] = error;
                }
        );

        // Wait for async completion
        Thread.sleep(200);

        assertEquals(1, callbackInvoked.get());
        assertEquals("callback-result", callbackResult[0]);
        assertNull(callbackError[0]);
    }

    @Test
    void testExecuteAsyncWithError() throws ExecutionException, InterruptedException {
        String modelId = "error-test-model";

        CompletableFuture<String> future = asyncService.executeAsync(
                modelId,
                mid -> {
                    throw new ModelException(mid, "test error",
                            ModelException.ErrorType.PREDICTION_FAILED);
                },
                0
        );

        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    void testGetExecutor() {
        String modelId = "dedicated-executor-test";

        var executor1 = executorConfig.getDedicatedExecutor(modelId);
        var executor2 = executorConfig.getDedicatedExecutor(modelId);

        assertNotNull(executor1);
        assertNotNull(executor2);
        // Should return the same executor for the same modelId
        assertEquals(executor1, executor2);
    }

    @Test
    void testRemoveExecutor() {
        String modelId = "remove-executor-test";

        var executor = executorConfig.getDedicatedExecutor(modelId);
        assertNotNull(executor);

        // Should not throw
        assertDoesNotThrow(() -> executorConfig.removeDedicatedExecutor(modelId));
    }
}
