package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	//여러개의 스레드를 효율적으로 관리해주는 라이브러리(서버의 성능 저하 방지)
	public static ExecutorService threadPool;
	//백터는 간단하게 배열
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드
	public void startServer(String IP,int port) {
		try {
			serverSocket = new ServerSocket();
			//서버 컴터가 특정 클라를 대기하게 만든다.
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {//서버가 안닫혔다면
				stopServer();
			}
			return;
		}
		//클라이언트가 접속할 때까지 계속 기다리는 쓰레드
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						//들어오면 새로운 객체로써 배열에 추가
						clients.add(new Client(socket));
						System.out.println("[클라이언트 접속]"
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
	//서버의 작동을 중지시키는 메소드
	public void stopServer() {
		try {
			//현재 작동 중인 모든 소켓을 닫기
			Iterator<Client> iterator = clients.iterator();//하나하나의 클라에 접근
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();//소켓도 지우고
				iterator.remove();//배열에서도 지우기
			}
			//서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();//다 지웠으니 서버 소켓 닫고
			}
			//쓰레드 풀 종료
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();//스레드 풀도 종료
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	//UI를 생성하고, 실질적으로 프로그램을 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();//레이아웃
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";//자기 ip주소를 가리킴
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP,port);
				Platform.runLater(() -> {
					String message = String.format("[서버 시작]\n", IP,port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			}else {
				stopServer();
				Platform.runLater(()->{
					String message = String.format("[서버 종료]\n", IP,port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}
		});
		
		Scene scene = new Scene(root,400,400);
		primaryStage.setTitle("[채팅 서버]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	// 프로그램의 진입점입니다.
	public static void main(String[] args) {
		launch(args);
	}
}
