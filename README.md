# rpc
 基于黄老师(原文地址：https://gitee.com/huangyong/rpc/) 的rpc,实现长连接的同步调用方式,复用channel: 2019年12月2日完成


通过黄老师在rpc-sample-client这个模块中的调用对比,使用长连接的方式要比短链接的方式性能有较大提升。

注意：  
短链接情况,在server端RpcServerHandler中每次处理完一个请求之后,会关闭该channel
长连接情况,在server端RpcServerHandler不要关闭channel

自己学习使用,如有错误,大家交流。

