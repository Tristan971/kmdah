package moe.tristan.kmdah.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.thread.ThreadPool;

public final class JettyLoomThreadPool implements ThreadPool {

    ExecutorService executorService = Executors.newVirtualThreadExecutor();

    JettyLoomThreadPool() {
    }

    @Override
    public void join() throws InterruptedException {
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Override
    public int getThreads() {
        return 1;
    }

    @Override
    public int getIdleThreads() {
        return 1;
    }

    @Override
    public boolean isLowOnThreads() {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        executorService.submit(command);
    }

}
