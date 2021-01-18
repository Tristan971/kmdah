package moe.tristan.kmdah.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ThrottledExecutorService {

    private ThrottledExecutorService() {
    }

    public static ExecutorService from(int minActive, int maxActive, int maxQueued) {
        return new ThreadPoolExecutor(
            minActive,
            maxActive,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(maxQueued)
        );
    }

}
