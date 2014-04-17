package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import utils.CryptoUtils;

public class Server {
	
	private ServerSocket serverSocket = null;
	//dictionary for clients' ports for listening request
	private HashMap<String, String> clientPortDict = null;
	private HashMap<String, Key> clientSecretKeyDict = null;
	private Key SecretKey;
	public Map<String, String> passwordBook = null;
	private Key privateKey = null;

	public Server() {
		try{
			clientPortDict = new HashMap<String, String>();
			clientSecretKeyDict = new HashMap<String, Key>();
			passwordBook = new HashMap<String, String>();
			loadPassword("password.txt");
			privateKey = CryptoUtils.getPrivateKey("private.der");

/*	
			System.out.println("Please input the port # of server:");
		    InputStreamReader is_reader = new InputStreamReader(System.in);
	        
		    String str = new BufferedReader(is_reader).readLine();
	        int port = Integer.parseInt(str);
	*/		
	        serverSocket = new ServerSocket(8989);
	        System.out.println("server is running");
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

	private void loadPassword(String passwordfile) {
        try {
        	FileReader reader = new FileReader(passwordfile);
            BufferedReader br = new BufferedReader(reader);
            String s1 = null;
			while((s1 = br.readLine()) != null) {
				String[] section = s1.split(":");
			    String name = section[0];
			    String pwdsalt = section[1];
			    String pwd = section[2];
//			    System.out.println(name);
//			    System.out.println(pwdsalt);
//			    System.out.println(pwd);
			    passwordBook.put(name, pwdsalt+":"+pwd);
			}
			reader.close();
			br.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	public Map<String, String> getPasswordBook() {
		return passwordBook;
	}

	public void setPasswordBook(Map<String, String> passwordBook) {
		this.passwordBook = passwordBook;
	}
	
	public Key getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(Key privateKey) {
		this.privateKey = privateKey;
	}
}
