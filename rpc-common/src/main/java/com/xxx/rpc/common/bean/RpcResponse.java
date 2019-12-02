package com.xxx.rpc.common.bean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 封装 RPC 响应
 *
 * @author huangyong
 * @since 1.0.0
 */
public class RpcResponse {

    private String requestId;
    private Exception exception;
    private Object result;
    private CountDownLatch countDownLatch;

    public RpcResponse(){
        this.countDownLatch = new CountDownLatch(1);
    }

    public boolean hasException() {
        return exception != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        try {
            countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
        countDownLatch.countDown();
    }
}
