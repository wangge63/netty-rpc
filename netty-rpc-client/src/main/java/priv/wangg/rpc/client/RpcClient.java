package priv.wangg.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import priv.wangg.rpc.client.connect.ConnectionManager;
import priv.wangg.rpc.client.discovery.ServiceDiscovery;
import priv.wangg.rpc.client.proxy.ObjectProxy;
import priv.wangg.rpc.client.proxy.RpcService;
import priv.wangg.rpc.util.ThreadPoolUtil;

import java.lang.reflect.Proxy;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcClient implements ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ServiceDiscovery serviceDiscovery;

    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.createThreadPool(RpcClient.class.getSimpleName(), 8, 16);

    public RpcClient(String registryAddress) {
        serviceDiscovery = new ServiceDiscovery(registryAddress);
    }

    // @SuppressWarnings("unchecked")
    public static <T, P> T createService(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T, P>(interfaceClass, version)
        );
    }

    public static <T, P> RpcService createAsyncService(Class<T> interfaceClass, String version) {
        return new ObjectProxy<T, P>(interfaceClass, version);
    }
    public void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectionManager.getInstance().stop();
    }

    @Override
    public void destroy() throws Exception {
        this.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

}
