package priv.wangg.rpc.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.client.RpcClient;
import priv.wangg.rpc.model.RpcRequest;
import priv.wangg.rpc.model.RpcResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

public class RpcFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);

    private Sync sync;
    private RpcRequest rpcRequest;
    private RpcResponse rpcResponse;
    private long startTime;
    private long reponseTimeThreshold = 5000;
    private List<AsyncRpcCallback> pendingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest rpcRequest) {
        this.sync = new Sync();
        this.rpcRequest = rpcRequest;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        if (this.rpcResponse != null) {
            return this.rpcResponse.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if(success) {
            if(this.rpcResponse != null) {
                return this.rpcResponse.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.rpcRequest.getRequestId()
                    + ". Request class name: " + this.rpcRequest.getClassName()
                    + ". Request method: " + this.rpcRequest.getMethodName());
        }
    }

    public void done(RpcResponse response) {

        this.rpcResponse = response;
        sync.release(1);
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.reponseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = " + rpcRequest.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback :
                    this.pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RpcFuture addCallback(AsyncRpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRpcCallback callback) {
        final RpcResponse res = this.rpcResponse;
        RpcClient.submit(new Runnable() {
            @Override
            public void run() {
                if (!res.isError()) {
                    callback.success(res.getResult());
                } else {
                    callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
                }
            }
        });
    }

    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -2913328282989303029L;

        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        protected boolean isDone() {
            return getState() == done;
        }
    }
}
