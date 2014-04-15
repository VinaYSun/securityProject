package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.HashMap;

public class Client {

	private Socket sktToServer = null;
	private ServerSocket peerListener = null;

	private String username;
	private String password;
	private int portForPeers;

	private HashMap<String, String> clientPortDict = null;
	private HashMap<String, Key> peerSecretKeyDict = null;
	
	//parameters for client future use
	private Key PublicKey;
	private byte[] gamodp;
	private byte[] gsmodp;
	private byte[] gasmodp;
	private Key secretKey;
	private byte[] secretKeyBytes;
	private byte[] R1;
	private byte[] R2;
	private byte[] R3;
	private byte[] R4;
	private byte[] R5;
	private byte[] pwdKey;
	private byte[] ticketToPeer;
	private byte[] peerName;
	
	public Client() {
		try{
			String hostAddress = "127.0.0.1";
			sktToServer = new Socket(hostAddress, 8989);
	
			BufferedReader inputRead = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter out = new PrintWriter(sktToServer.getOutputStream(), true);
	
			clientPortDict = new HashMap<String, String>();
			peerSecretKeyDict = new HashMap<String, Key>();
			
			if(sktToServer.isClosed()){
				System.out.println("Socket is closed.");
			}
			
			initPeerListener(inputRead);
			
			System.out.println("Please input the username:");
			username = inputRead.readLine();
		
			System.out.println("Please input the password:");
			password = inputRead.readLine();
			
			initInputListener();
			
			//communicate with server
			ServerListener serverIn = new ServerListener(this, sktToServer);
			serverIn.start();
			
			acceptPeerRequest();
	
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * handle incoming invitations from peers
	 */
	private void acceptPeerRequest() {
		try{
			while(true){
				Socket peer;
				peer = getPeerListener().accept();
				new peerThread(this, peer).start();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void initInputListener() {
		//listen to user input request
		InputListener userInput = new InputListener(this, sktToServer);
		userInput.start();
	}

	private void initPeerListener(BufferedReader inputRead) {
		try{
			System.out.println("Please input the port # of client listener:");
			String str = inputRead.readLine();
			int port = Integer.parseInt(str);
			setPortForPeers(port);
			ServerSocket serverSocket = new ServerSocket(port);
			setPeerListener(serverSocket);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public ServerSocket getPeerListener() {
		return peerListener;
	}

	public void setPeerListener(ServerSocket peerListener) {
		this.peerListener = peerListener;
	}

	public HashMap<String, Key> getPeerSecretKeyDict() {
		return peerSecretKeyDict;
	}

	public void setPeerSecretKeyDict(HashMap<String, Key> peerSecretKeyDict) {
		this.peerSecretKeyDict = peerSecretKeyDict;
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

	public HashMap<String, String> getClientPortDict() {
		return clientPortDict;
	}

	public void setClientPortDict(HashMap<String, String> clientPortDict) {
		this.clientPortDict = clientPortDict;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public static void main(String args[]) throws IOException {
		new Client();
	}
}
