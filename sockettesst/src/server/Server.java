package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
	
	private ServerSocket serverSocket = null;
	private HashMap<String, String> namePortDict = null;

	public Server() throws IOException {
		
			namePortDict = new HashMap<String, String>();
			
			System.out.println("Please input the port # of server:");
		    InputStreamReader is_reader = new InputStreamReader(System.in);
	        
		    String str = new BufferedReader(is_reader).readLine();
	        int port = Integer.parseInt(str);
	        
	        System.out.println("Connecting server...");
			
	        serverSocket = new ServerSocket(port);
			while (true) {
				Socket clientSocket;
				clientSocket = serverSocket.accept();
				new ServerThread(this, clientSocket).start();
			}
	}	
	
	public static void main(String args[]) throws IOException{
		new Server();
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public HashMap<String, String> getNameaddr() {
		return namePortDict;
	}

	public void setNameaddr(HashMap<String, String> nameaddr) {
		this.namePortDict = nameaddr;
	}
}
