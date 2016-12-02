import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.wowtools.rmi.Publisher;
import org.wowtools.rmi.RmiClient;

public class RmiTest {
	public static void main(String[] args) throws RemoteException {
		int port = 1233;
		String ip = "127.0.0.1";
		String name = "helloService";
		//启动服务端
		new Publisher(ip,port).publish(name, new HelloServiceImpl());
		
		//远程调用
		HelloService hs = RmiClient.getService(ip, port, name);
		String s = hs.sayHello("world");
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
