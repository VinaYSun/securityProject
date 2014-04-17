package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import utils.Message;
import utils.MessageReader;

public class Client {

	private Socket sktToServer = null;
	private ServerSocket peerListener = null;

	private String username;
	private String password;
	private String portForPeers;
	private String listRequestTime;
	private String logoutRequestTime;

	private HashMap<String, String> clientPortDict = null;
	private HashMap<String, Key> peerSecretKeyDict = null;
	private HashMap<String, Socket> clientSocketDict = null;
	private HashMap<String, byte[]> peerTicketDict = null;
	private HashMap<String, Map<String, byte[]>> peerTicketRequestDict = null;
	//parameters for client future use
	private Key PublicKey;
	private byte[] gamodp;
	private byte[] gsmodp;
	private byte[] gasmodp = null;
	private Key secretKeyKas = null;
	private byte[] R1;
	private byte[] R2;
	private byte[] R3;
	private byte[] R4;
	private byte[] R5;
	private byte[] R6;
	private byte[] pwdKey;
	private byte[] ticketToPeer;
	private byte[] peerName;
	
	public Client() {

			initConnection();
	
			clientPortDict = new HashMap<String, String>();
			peerSecretKeyDict = new HashMap<String, Key>();
			clientSocketDict = new HashMap<String, Socket>();
			peerTicketDict = new HashMap<String,  byte[]>();
			peerTicketRequestDict = new HashMap<String,  Map<String, byte[]>>();
			
			BufferedReader inputRead = new BufferedReader(new InputStreamReader(System.in));
			initPeerListener(inputRead);
			getNameAndPwd(inputRead);

			//respond to user input
			initInputListener();
			
			initServerListener();
			
			acceptPeerRequest();
		
	}
	
	private void initServerListener() {
		//respond to server message
		ServerListener serverListener = new ServerListener(this, sktToServer);
		serverListener.start();
	}

	private void getNameAndPwd(BufferedReader input) {
		try {
			System.out.println("Please input the username:");
			username = input.readLine();
			System.out.println("Please input the password:");
			password = input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initConnection() {
		String hostAddress = "127.0.0.1";
		try {
			sktToServer = new Socket(hostAddress, 8989);
			PrintWriter out = new PrintWriter(sktToServer.getOutputStream(), true);
			Message msgInit = new Message(1,1,"login");
			String str= MessageReader.messageToJson(msgInit);
			out.println(str);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
			System.out.println("Please input the port # of client listener: 9000 - 9095");
			String str = inputRead.readLine();
			int port = Integer.parseInt(str);
			setPortForPeers(str);
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

	public String getPortForPeers() {
		return portForPeers;
	}

	public void setPortForPeers(String portForPeers) {
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

	public Key getSecretKeyKas() {
		return secretKeyKas;
	}

	public void setSecretKeyKas(Key secretKeyKas) {
		this.secretKeyKas = secretKeyKas;
	}

	public byte[] getGasmodp() {
		return gasmodp;
	}

	public void setGasmodp(byte[] gasmodp) {
		this.gasmodp = gasmodp;
	}

	public byte[] getTicketToPeer() {
		return ticketToPeer;
	}

	public void setTicketToPeer(byte[] ticketToPeer) {
		this.ticketToPeer = ticketToPeer;
	}
	
	public static void main(String args[]) throws IOException {
		new Client();
	}

	public String getListRequestTime() {
		return listRequestTime;
	}

	public void setListRequestTime(String listRequestTime) {
		this.listRequestTime = listRequestTime;
	}

	public HashMap<String, Socket> getClientSocketDict() {
		return clientSocketDict;
	}

	public void setClientSocketDict(HashMap<String, Socket> clientSocketDict) {
		this.clientSocketDict = clientSocketDict;
	}

	public HashMap<String, byte[]> getPeerTicketDict() {
		return peerTicketDict;
	}

	public void setPeerTicketDict(HashMap<String, byte[]> peerTicketDict) {
		this.peerTicketDict = peerTicketDict;
	}

	public HashMap<String, Map<String, byte[]>> getPeerTicketRequestDict() {
		return peerTicketRequestDict;
	}

	public void setPeerTicketRequestDict(HashMap<String, Map<String, byte[]>> peerTicketRequestDict) {
		this.peerTicketRequestDict = peerTicketRequestDict;
	}

	public byte[] getR6() {
		return R6;
	}

	public void setR6(byte[] r6) {
		R6 = r6;
	}

	public String getLogoutRequestTime() {
		return logoutRequestTime;
	}

	public void setLogoutRequestTime(String logoutRequestTime) {
		this.logoutRequestTime = logoutRequestTime;
	}
}
