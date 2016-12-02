import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.wowtools.rmi.RmiPublisher;
import org.wowtools.rmi.RmiClient;
import org.wowtools.rmi.RmiClient.ZkServiceGetter;

public class RmiTest {
	public static void main(String[] args) throws Exception {
		int port = 1233;
		String ip = "127.0.0.1";
		String name = "helloService";
		String zkUrl = "127.0.0.1:2181";
		//启动服务端
		new RmiPublisher(ip,port,zkUrl,null).publish(name, new HelloServiceImpl());
		
		//远程调用
		ZkServiceGetter<HelloService> getter = RmiClient.getServiceGetter(zkUrl, name);
		HelloService hs = getter.getService();
		String s = hs.sayHello("World");
		System.out.println(s);
	}
}

interface HelloService extends Remote {

    String sayHello(String name);
}

class HelloServiceImpl implements HelloService,Serializable {
	private static final long serialVersionUID = 1L;

	@Override
    public String sayHello(String name){
        return String.format("Hello %s", name);
    }
}
