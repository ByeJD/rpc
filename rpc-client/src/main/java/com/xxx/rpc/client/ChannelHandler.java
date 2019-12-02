package com.xxx.rpc.client;

import com.xxx.rpc.common.bean.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
/**
 * @author liuquanquan
 */
public class ChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private Map<String, FutureResponse> futureResponseMap;

    public ChannelHandler(Map<String, FutureResponse> futureResponseMap) {
        this.futureResponseMap = futureResponseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        System.out.println("response : " + msg.getRequestId());
        FutureResponse futureResponse = futureResponseMap.get(msg.getRequestId());
        futureResponse.setResponse(msg);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("channel  is inActive : "  + ctx.channel().isActive());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getCause());
        ctx.close();
    }
}
