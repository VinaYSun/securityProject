package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utils.CryptoUtils;
import utils.Message;
import utils.MessageReader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ServerThread extends Thread{

	private BufferedReader in;
	private PrintWriter out;
	private Server server = null;
	private Socket clientSocket = null;
	private HashMap<String, String> clientPortDict;
	private Key privateKey = null;
	private Map<String, String> passwordBook;

	//parameters for this user thread
	private String username;
	private byte[] gamodp;
	private byte[] gsmodp;
	private byte[] gasmodp;
	private Key secretKeyKas = null;
	private String userport;
	private byte[] R1 = null;
	private byte[] R2 = null;
	private byte[] R3 = null;
	private byte[] R4 = null;
	private String password;
	private Message messageFromClient;
	private Message messageToClient;
	private static final int LOGIN_AUTH = 1;
	private static final int LIST_REQUEST = 2;
	private static final int CHAT_REQUEST = 3;
	private static final int LOGOUT_REQUEST = 5;
	private Map<String, Key> DHServerKeyPair;



	public ServerThread(Server s, Socket client){
		try{
			server = s;
			clientSocket = client;
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        out = new PrintWriter(clientSocket.getOutputStream(), true);
	        passwordBook = s.getPasswordBook();
	        
			clientPortDict = new HashMap<String, String>();
			
			privateKey = CryptoUtils.getPrivateKey("private.der");
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		try {
			handleSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleSocket() throws IOException {
		
         while(true){
        	 
	         String temp = in.readLine();
	         
	         if(temp==null) continue;
	         
//	         System.out.println(temp);
	         
	         messageFromClient = MessageReader.messageFromJson(temp);
	         messageToClient = new Message(0,0);
	         
	         switch (messageFromClient.getProtocolId()){
	        	 case LOGIN_AUTH:
	        		 loginRequest(messageFromClient);
	        		 break;
	        	 case LIST_REQUEST:
	        		 listRequest(messageFromClient);
	        		 break;
	        	 case CHAT_REQUEST:
	        		 chatRequest(messageFromClient);
	        		 break;
	        	 case LOGOUT_REQUEST:
	        		 logoutRequest();
	        		 break;
	        	 default:
	        		 break;
	         }
//	         if( messageToClient.getDataBytes() == null || messageToClient.getStepId() == 0){
//	        	 System.out.println("Got unvalid message");
//	         }
         }
	}
	
	
	private void logoutRequest() {
		
	}


	private void chatRequest(Message message) {
		int step = message.getStepId();
		if(step == 1){
			handleMessage31(message.getDataBytes());
		}else{
			System.out.println("Step doesn't exist");
		}
	}


	private void handleMessage31(byte[] data) {
		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();

		if(secretKeyKas !=null){
			mapin = CryptoUtils.getDecryptedMap(data, secretKeyKas);
			byte[] receiver = mapin.get("receiver");
			byte[] R4 = mapin.get("R4");
			byte[] sender = mapin.get("sender");
			
			String receiverName = new String(receiver);
			clientPortDict = server.getClientPortDict();
			if(clientPortDict.containsKey(receiverName)){
				//prepare kab
				byte[] keybyteKab = CryptoUtils.generateNonce();
				//receiver is online
				byte[] ticket = prepareTicket(sender, keybyteKab, receiverName);
			
				mapout.put("TicketB", ticket);
				mapout.put("receiver", receiver);
				mapout.put("port", clientPortDict.get(receiverName).getBytes());
				mapout.put("R4", R4);
				mapout.put("kab", keybyteKab);
			}else{
				//reciever is offline or doesn't exist
				//just put a receiver inside
				mapout.put("receiver", receiver);
				
			}
			String msgstr = CryptoUtils.getOutputStream(3, 2, mapout, secretKeyKas);
			out.println(msgstr);
		}
	}

	private byte[] prepareTicket(byte[] sender, byte[] keybyteKab, String receiver) {
		//get  {kab, a }
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();
		mapout.put("kabbyte", keybyteKab);
		mapout.put("A", sender);
		//get kbs
		Key kbs = server.getClientSecretKeyDict().get(receiver);
		//Kbs{Kab, A}
		byte[] ticket = CryptoUtils.getEncryptedMap(mapout, kbs);
		return ticket;
	}


	private void listRequest(Message message) {
		int step = message.getStepId();
		if(step == 1){
			handleMessage21(message.getDataBytes());
		}else{
			System.out.println("Step doesn't exist");
		}
	}


	private void handleMessage21(byte[] data) {
		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();

		if(secretKeyKas !=null){
			mapin = CryptoUtils.getDecryptedMap(data, secretKeyKas);
			byte[] list = mapin.get("list");
			if((new String(list)).equals("list")){
				clientPortDict = server.getClientPortDict();
				//get names 
				Set<String> nameSet = clientPortDict.keySet();
				//encrypt nameset
				Gson gson = new Gson();
				String str = gson.toJson(nameSet, new TypeToken<Set<String>>(){}.getType());
				byte[] cipherdata = CryptoUtils.encryptByAES(str.getBytes(), secretKeyKas);
				
				messageToClient.setProtocolId(2);
				messageToClient.setStepId(2);
				messageToClient.setData(cipherdata);
				String strout = MessageReader.messageToJson(messageToClient);
		        out.println(strout);
			}
		}
		
	}


	private void loginRequest(Message message) {
		int step = message.getStepId();
		if(step == 1){
			handleMessage11(message.getData());
		}else if(step == 3){
			handleMessage13(message.getDataBytes());
		}else if(step == 5){
			handleMessage15(message.getDataBytes());
		}else{
			System.out.println("Step doesn't exist");
		}
	}




	private void handleMessage11(String msgstr) {
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();

		if(msgstr.equals("login")){
			InetAddress addr = clientSocket.getInetAddress();
			String clientIP = addr.getHostAddress();
			R1 = CryptoUtils.generateNonce();
			mapout.put("IP", clientIP.getBytes(Charset.forName("UTF-8")));
			mapout.put("cookie", R1);
	        messageToClient.setProtocolId(1);
			messageToClient.setStepId(2);
			messageToClient.setData(CryptoUtils.mapToByte(mapout));
			String str = MessageReader.messageToJson(messageToClient);
	        out.println(str);
		}
	}
	
	private void handleMessage13(byte[] data) {
		
		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
		HashMap<String, byte[]> mapdata = new HashMap<String, byte[]>();
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();
		
		if(R1 != null){
			
			//get key and data
			mapin = CryptoUtils.mapFromByte(data);
			byte[] encryptedkey = mapin.get("key");
			byte[] encryptedmap = mapin.get("mapdata");
			
			//decrypt key and data
			byte[] decryptedKey = CryptoUtils.decryptByRSAPrivateKey(encryptedkey, privateKey);
			mapdata = CryptoUtils.getDecryptedMap(encryptedmap, decryptedKey);
			
			//get data
			byte[] r1byte = mapdata.get("R1");
			byte[] r2byte = mapdata.get("R2");
			byte[] portbyte = mapdata.get("port");
			byte[] gamodp = mapdata.get("gamodp");
			byte[] passwordbyte = mapdata.get("password");
			byte[] usernamebyte = mapdata.get("username");
			
			//save parameters
			setGamodp(gamodp);
			setR2(r2byte);
			setUsername(new String(usernamebyte));
			setUserport(new String(portbyte));
			
			
			if((new String(R1)).equals(new String(r1byte))){
				boolean b = false;
				b = verifyPassword(getUsername(), new String(passwordbyte));
				if(b == true){
					//prepare gsmodp, r3, key
					Key WKey = prepareWKey(passwordbyte, r2byte);
					
					//prepare gsmodp
					Map<String, Key> DHServerkey = CryptoUtils.generateDHKey(gamodp);
					setDHServerKeyPair(DHServerkey);
		        	
					//save gasmodp
					generateKas(DHServerkey, gamodp);
					
					//prepare R3
					R3 = CryptoUtils.generateNonce();
					setR3(R3);
					
					//prepareMap
					mapout.put("R3", R3);
					mapout.put("gsmodp", DHServerkey.get("public_key").getEncoded());
					
					//set encrypted map into message
					messageToClient.setData(CryptoUtils.getEncryptedMap(mapout, WKey));
					messageToClient.setStepId(4);
     		        messageToClient.setProtocolId(1);
     		        String str = MessageReader.messageToJson(messageToClient);
	    	        out.println(str);
					
				}
			}
		}
	}


	private void handleMessage15(byte[] data) {
		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();

		if(R3 != null && secretKeyKas !=null){
			mapin = CryptoUtils.getDecryptedMap(data, secretKeyKas);
			byte[] r3byte = mapin.get("R3");
			if((new String(R3)).equals(new String(r3byte))){
				//update list
				updateClientPortDict();
				updateClientSecretKeyDict();
			}
		}
	}

	private void generateKas(Map<String, Key> dHServerkey, byte[] gamodp) {
		Key dhServerPriKey = dHServerkey.get("private_key");
    	byte[] kas  = CryptoUtils.generateSessionKey(gamodp, dhServerPriKey.getEncoded());
    	Key sessionKey = CryptoUtils.generateAESKey(kas);
    	setSecretKeyKas(sessionKey);
	}


	private Key prepareWKey(byte[] pwd, byte[] salt) {
		byte[] pwdKey =  CryptoUtils.getSaltHashByteArr(pwd, salt);
		Key pwdaesKey = CryptoUtils.generateAESKey(pwdKey);
		return pwdaesKey;
	}


	private Boolean verifyPassword(String name, String password) {
		Boolean check = false;
		if(passwordBook.containsKey(name)){
			String[] section = passwordBook.get(name).split(":");
	        String correctSalt = section[0];
	        String correctPwd = section[1];
	        String loginPassword = CryptoUtils.getSaltHash(password, new String(CryptoUtils.hexStringToBytes(correctSalt)));
	    	
	        if(loginPassword.equals(correctPwd)){
	        	check = true;
	        }
		}
		return check;
	}
	
	/**
	 * (after authentication) get client name and its opening port, save to address book
	 * @param str
	 */
	private void updateClientPortDict() {
	    HashMap<String, String> clientPortDict = null;
	    clientPortDict = server.getClientPortDict();
		if(clientPortDict == null){
			clientPortDict = new HashMap<String, String>();
		}
	    clientPortDict.put(getUsername(), getUserport());
	    server.setClientPortDict(clientPortDict);
	    System.out.println("Add user: " + getUsername());
	}

	private void updateClientSecretKeyDict() {
		HashMap<String, Key> clientSecretKeyDict = null;
		clientSecretKeyDict = server.getClientSecretKeyDict();
		if(clientSecretKeyDict == null){
			clientSecretKeyDict = new HashMap<String, Key>();
		}
		clientSecretKeyDict.put(getUsername(), getSecretKeyKas());
		server.setClientSecretKeyDict(clientSecretKeyDict);;
	}
	
	public byte[] getGamodp() {
		return gamodp;
	}

	public void setGamodp(byte[] gamodp) {
		this.gamodp = gamodp;
	}

	public byte[] getR2() {
		return R2;
	}

	public void setR2(byte[] r2) {
		R2 = r2;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getUserport() {
		return userport;
	}


	public void setUserport(String userport) {
		this.userport = userport;
	}
	
	public Map<String, Key> getDHServerKeyPair() {
		return DHServerKeyPair;
	}


	public void setDHServerKeyPair(Map<String, Key> dHServerKeyPair) {
		DHServerKeyPair = dHServerKeyPair;
	}


	public byte[] getR3() {
		return R3;
	}


	public void setR3(byte[] r3) {
		R3 = r3;
	}


	public Key getSecretKeyKas() {
		return secretKeyKas;
	}


	public void setSecretKeyKas(Key secretKeyKas) {
		this.secretKeyKas = secretKeyKas;
	}
}
