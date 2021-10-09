package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//한명의 클라이언트
public class Client {
	
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	//클라이언트로부터 메시지를 전달 받는 메소드
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					//반복적으로 정보를 받기
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						if(length == -1)throw new IOException(); 
						System.out.println("[메시지 수신 성공]"
						+socket.getRemoteSocketAddress() // 현재 접속한 클라의 ip주소와 같은 주소정보 출력
						+": "+Thread.currentThread().getName());// 스레드의 고유 정보 출력(스레드 이름)
						
						String message = new String(buffer, 0, length, "UTF-8");
						
						//다른 클라한테 전체 보내주기
						for(Client client : Main.clients) {
							client.send(message);
						}
					}

					
				}catch (Exception e) {
					try {
						System.out.println("[메시지 수신 오류]"
							+socket.getRemoteSocketAddress()
							+": "+Thread.currentThread().getName());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);//스레드 풀에 넣어준다.
	}
	
	//클라이언트에게 메시지를 전송하는 메소드
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);//쓰고
					out.flush();//보내고
				} catch (Exception e) {
					try {
						System.out.println("[메시지 송신 오류]"
							+socket.getRemoteSocketAddress()
							+": "+Thread.currentThread().getName());
						//오류 발생하면 우리 서버안에서 해당 클라도 끊어줌
						Main.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
}
