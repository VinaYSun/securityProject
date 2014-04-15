package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class client {

	private Socket socket = null;
	private String username;
	private int portForPeers;
	private String password;

	private HashMap<String, Peer> peerpool = null;
	private HashMap<String, String> clientPortDict = null;

	public client() throws IOException{
		
		
		HashMap<String, Peer> peerpool = new HashMap<String, Peer>();
		
		String hostAddress = "127.0.0.1";
		
		BufferedReader inputRead = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

		socket = new Socket(hostAddress, 8899);
		
		clientPortDict = new HashMap<String, String>();

		if(socket.isClosed()){
			System.out.println("Socket is closed.");
		}
		
		System.out.println("Please input the port # of client listener:");
		String str = inputRead.readLine();
		portForPeers = Integer.parseInt(str);
		setPortForPeers(portForPeers);
		ServerSocket serverSocket = new ServerSocket(portForPeers);
		
		System.out.println("Please input the username:");
		username = inputRead.readLine();
	
		System.out.println("Please input the password:");
		password = inputRead.readLine();
		
		//sending port with username 
	 	out.println( username + portForPeers);

		//listen to user input request
		InputListener userInput = new InputListener(this, socket);
		userInput.start();
		
		//communicate with server
		ServerListener serverIn = new ServerListener(this, socket);
		serverIn.start();
		
		//monitor incoming invitations from peers
		while(true){
			Socket peer;
			peer = serverSocket.accept();
			new peerThread(this, peer).start();
		}
		
	}

	public int getPortForPeers() {
		return portForPeers;
	}

	public void setPortForPeers(int portForPeers) {
		this.portForPeers = portForPeers;
	}

	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	public HashMap<String, String> getNameMap() {
		return clientPortDict;
	}


	public void setNameMap(HashMap<String, String> nameMap) {
		this.clientPortDict = nameMap;
	}


	public HashMap<String, Peer> getPeerpool() {
		return peerpool;
	}

	public void setPeerpool(HashMap<String, Peer> peerpool) {
		this.peerpool = peerpool;
	}
	
	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public static void main(String args[]) throws IOException {
		new client();
	}
}
