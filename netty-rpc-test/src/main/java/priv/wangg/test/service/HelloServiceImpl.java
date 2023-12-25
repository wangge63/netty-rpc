package priv.wangg.test.service;

import priv.wangg.rpc.annotation.NettyRpcService;

@NettyRpcService(value = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService{

    public HelloServiceImpl() {}

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
