package priv.wangg.test.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.server.RpcServer;
import priv.wangg.test.service.HelloService;
import priv.wangg.test.service.HelloServiceImpl;

public class RpcServerBootstrap2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerBootstrap2.class);

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1:18877";
        String registryAddress = "127.0.0.1:2181";

        RpcServer rpcServer = new RpcServer(serverAddress, registryAddress);
        HelloService helloService = new HelloServiceImpl();
        rpcServer.addService(HelloService.class.getName(), "1.0", helloService);

        try {
            rpcServer.start();
        } catch (Exception e) {
            LOGGER.error("RpcServer start error", e);
        }
    }
}
