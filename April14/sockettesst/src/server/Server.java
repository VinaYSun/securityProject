package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.HashMap;

public class Server {
	
	private ServerSocket serverSocket = null;
	//dictionary for clients' ports for listening request
	private HashMap<String, String> clientPortDict = null;
	private HashMap<String, Key> clientSecretKeyDict = null;
	private Key SecretKey;
	
	public Server() {
		try{
			clientPortDict = new HashMap<String, String>();
			clientSecretKeyDict = new HashMap<String, Key>();
			
			System.out.println("Please input the port # of server:");
		    InputStreamReader is_reader = new InputStreamReader(System.in);
	        
		    String str = new BufferedReader(is_reader).readLine();
	        int port = Integer.parseInt(str);
			
	        serverSocket = new ServerSocket(port);

	        acceptClient(serverSocket);
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}	
	
	private void acceptClient(ServerSocket ss) {
		try{
			while (true) {
				Socket clientSocket = new Socket();
				clientSocket = ss.accept();
				new ServerThread(this, clientSocket).start();
			}
		}catch(IOException e){
			e.printStackTrace();
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

	public HashMap<String, String> getClientPortDict() {
		return clientPortDict;
	}

	public void setClientPortDict(HashMap<String, String> nameaddr) {
		this.clientPortDict = nameaddr;
	}
	
	public HashMap<String, Key> getClientSecretKeyDict() {
		return clientSecretKeyDict;
	}

	public void setClientSecretKeyDict(HashMap<String, Key> clientSecretKeyDict) {
		this.clientSecretKeyDict = clientSecretKeyDict;
	}
}
