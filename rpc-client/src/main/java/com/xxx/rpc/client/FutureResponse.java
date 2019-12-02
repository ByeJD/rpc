package com.xxx.rpc.client;

import com.xxx.rpc.common.bean.RpcResponse;

import java.util.concurrent.*;

/**
 * @author liuquanquan
 */
public class FutureResponse implements Future<RpcResponse> {

    public RpcResponse getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
        countDownLatch.countDown();
    }

    private RpcResponse response;


    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout,unit);
        return response;
    }
}
