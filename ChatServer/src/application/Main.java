package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {
	
	//�������� �����带 ȿ�������� �������ִ� ���̺귯��(������ ���� ���� ����)
	public static ExecutorService threadPool;
	//���ʹ� �����ϰ� �迭
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//������ �������Ѽ� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
	public void startServer(String IP,int port) {
		try {
			serverSocket = new ServerSocket();
			//���� ���Ͱ� Ư�� Ŭ�� ����ϰ� �����.
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {//������ �ȴ����ٸ�
				stopServer();
			}
			return;
		}
		//Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� ������
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						//������ ���ο� ��ü�ν� �迭�� �߰�
						clients.add(new Client(socket));
						System.out.println("[Ŭ���̾�Ʈ ����]"
								+socket.getRemoteSocketAddress()
								+": "+Thread.currentThread().getName()
								);
					} catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}				
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	//������ �۵��� ������Ű�� �޼ҵ�
	public void stopServer() {
		try {
			//���� �۵� ���� ��� ������ �ݱ�
			Iterator<Client> iterator = clients.iterator();//�ϳ��ϳ��� Ŭ�� ����
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();//���ϵ� �����
				iterator.remove();//�迭������ �����
			}
			//���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();//�� �������� ���� ���� �ݰ�
			}
			//������ Ǯ ����
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();//������ Ǯ�� ����
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	//UI�� �����ϰ�, ���������� ���α׷��� ���۽�Ű�� �޼ҵ�
	@Override
	public void start(Stage primaryStage) {
		
	}
	
	// ���α׷��� �������Դϴ�.
	public static void main(String[] args) {
		launch(args);
	}
}
