package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import server.Server;
import utils.CryptoUtils;
import utils.Message;
import utils.MessageReader;

public class ServerListener extends Thread{
	
	private BufferedReader in;
	private PrintWriter out;
	private Message messageFromServer;
	private Message messageToServer;
	private Client client = null;
	private Socket socket = null;
	private Key publicKey = null;
	private Map<String, Key> clientKeyPair = null;
	private Key tmpKey = null;
	private byte[] gsmodp = null;
	private byte[] R1 = null;
	private byte[] R2 = null;
	private byte[] R3 = null;
	private byte[] R4 = null;
	private Key secretKeyKas = null;

	private static final int LOGIN_AUTH = 1;
	private static final int LIST_REQUEST = 2;
	private static final int CHAT_REQUEST = 3;
	private static final int LOGOUT_REQUEST = 5;
	
	public ServerListener(Client c, Socket s) {
		client = c;
		socket = s;
        try {
    		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	publicKey = CryptoUtils.getPublicKey("public.der");
	}

	@Override
	public void run(){
		try {
			handleSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleSocket() throws IOException{

        while(true){
       	 
	         String temp = in.readLine();
	         
	         if(temp==null) continue;
	         
//	         System.out.println(temp);
	         
	         messageFromServer = MessageReader.messageFromJson(temp);
	         messageToServer = new Message(0,0);
	         
	         switch (messageFromServer.getProtocolId()){
	        	 case LOGIN_AUTH:
	        		 loginRequest(messageFromServer);
	        		 break;
	        	 case LIST_REQUEST:
	        		 listRequest(messageFromServer);
	        		 break;
	        	 case CHAT_REQUEST:
	        		 chatRequest(messageFromServer);
	        		 break;
	        	 case LOGOUT_REQUEST:
	        		 logoutRequest(messageFromServer);
	        		 break;
	        	 default:
	        		 break;
	         }
	         
        }
		
	}

	private void chatRequest(Message message) {
		int step = message.getStepId();
		if(step == 2){
			handleMessage32(message.getDataBytes());
		}else{
			System.out.println("Step doesn't exist");
		}
	}

	//1. get peer ticket request dict  (ticket messages pool )
	//2. save <peername, decrypted mapdata> in the map
	//set map to client object
	private void handleMessage32(byte[] dataBytes) {
		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
		if(secretKeyKas !=null){
			mapin = CryptoUtils.getDecryptedMap(dataBytes, secretKeyKas);
			String peername = new String(mapin.get("receiver"));
			HashMap<String, Map<String, byte[]>> map = client.getPeerTicketRequestDict();
			map.put(peername, mapin);
			client.setPeerTicketRequestDict(map);
		}
	}

	private void logoutRequest(Message message) {
		int step = message.getStepId();
		if(step == 2){
			handleMessage52(message.getDataBytes(), message.getTimestamp());
		}else{
			System.out.println("Step doesn't exist");
		}
	}

	private void handleMessage52(byte[] dataBytes, String timestamp) {
		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
		if(secretKeyKas !=null){
			
			//check time stamp 
			String str1 = client.getLogoutRequestTime();
			System.out.println("sent time: " + str1);
			System.out.println("get time" + timestamp);
			if(str1.equals(timestamp)){
				
			}
			
			mapin = CryptoUtils.getDecryptedMap(dataBytes, secretKeyKas);
			byte[] R6 = mapin.get("logout");
			byte[] confirm = mapin.get("confirm");
			if((new String(R6)).equals(client.getR6()) && (new String(confirm)).equals("confirm")){
				
				
				System.out.println("Succesfully logged out!");
			}
		}
	}

	private void listRequest(Message message) {
		int step = message.getStepId();
		if(step == 2){
			handleMessage22(message.getDataBytes(), message.getTimestamp());
		}else{
			System.out.println("Step doesn't exist");
		}
	}

	private void handleMessage22(byte[] dataBytes, String timestamp) {
		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
		if(secretKeyKas !=null){
			//check time stamp 
			String str1 = client.getListRequestTime();
			System.out.println("sent time: " + str1);
			System.out.println("get time" + timestamp);
			if(str1.equals(timestamp)){
				
			}
			byte[] plaindata = CryptoUtils.decryptByAES(dataBytes, secretKeyKas);
			Gson gson = new Gson();
			Set<String> nameset = gson.fromJson(new String(plaindata), new TypeToken<Set<String>>(){}.getType());
			System.out.println("online user list: ");
			for (String str : nameset) {
			      System.out.println(str);
			}
		}
	}

	private void loginRequest(Message fromServer) {
		int step = fromServer.getStepId();
		if(step == 2){
			handleMessage12(fromServer.getDataBytes());
		}else if(step == 4){
			handleMessage14(fromServer.getDataBytes());
		}else{
			System.out.println("Step doesn't exist");
		}
	}

	private void handleMessage12(byte[] data) {

		HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
		HashMap<String, byte[]> mapdata = new HashMap<String, byte[]>();
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();
		
		mapin = CryptoUtils.mapFromByte(data);
		R1 = mapin.get("cookie");
		mapin.get("IP");
		
		//prepare message 1, 3 Pub{aes}. aes{R1, R2, port, gamodp, password, username}
		//get encryptedkey 
		byte[] encryptedkey = generateEncryptedKey();
		
		//prepare map data
		if(R2 == null){
			R2 = CryptoUtils.generateNonce();
		}
		String password = client.getPassword();
		String port = client.getPortForPeers();
		String username = client.getUsername();
		
		//prepare gamodp
		Map<String, Key> clientKeyMap = CryptoUtils.generateDHKey();
		setClientKeyPair(clientKeyMap);
		
		//set map
		mapdata.put("R1", R1);
		mapdata.put("R2", R2);
		mapdata.put("password", password.getBytes(Charset.forName("UTF-8")));
		mapdata.put("port", port.getBytes(Charset.forName("UTF-8")));
		mapdata.put("username", username.getBytes(Charset.forName("UTF-8")));
		mapdata.put("gamodp", clientKeyMap.get("public_key").getEncoded());
		
		//convert map -> encrypted byte
		byte[] mapbyte = CryptoUtils.getEncryptedMap(mapdata, tmpKey);

		//put encryptedkey and mapbyte into a map 
		mapout.put("key", encryptedkey);
		mapout.put("mapdata", mapbyte);

		messageToServer.setStepId(3);
		messageToServer.setProtocolId(1);
		messageToServer.setData(CryptoUtils.mapToByte(mapout));
		String str = MessageReader.messageToJson(messageToServer);
         //send message;
        out.println(str);
	}
	

	private void handleMessage14(byte[] dataBytes) {
		if(R2 != null && getClientKeyPair()!= null){
			//prepare W
			Key WKey = getWKey();

			//decrypt map
			HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
			HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();
			
			mapin = CryptoUtils.getDecryptedMap(dataBytes, WKey);

			R3 = mapin.get("R3");
			gsmodp = mapin.get("gsmodp");
			
			//generate kas and SAVE
			generateKas(getClientKeyPair(), gsmodp);
			mapout.put("R3", R3);
			byte[] output =  CryptoUtils.mapToByte(mapout);
			byte[] cipherdata = CryptoUtils.encryptByAES(output, getSecretKeyKas());

			messageToServer.setStepId(5);
			messageToServer.setProtocolId(1);
			messageToServer.setData(cipherdata);
			String str = MessageReader.messageToJson(messageToServer);
	        //send message;
	        out.println(str);
		}
	}
	
	private void generateKas(Map<String, Key> clientKeyMap, byte[] gsmodp) {
		Key DHClientPriKey = clientKeyMap.get("private_key");
    	byte[] kas  = CryptoUtils.generateSessionKey(gsmodp, DHClientPriKey.getEncoded());
    	Key sessionKey = CryptoUtils.generateAESKey(kas);
    	setSecretKeyKas(sessionKey);
    	client.setSecretKeyKas(sessionKey);
	}
	
	private Key getWKey() {
		byte[] pwdbyte = client.getPassword().getBytes(Charset.forName("UTF-8"));
		byte[] pwdKey =  CryptoUtils.getSaltHashByteArr(pwdbyte, R2);
		Key pwdaesKey = CryptoUtils.generateAESKey(pwdKey);
		return pwdaesKey;
	}

	private byte[] generateEncryptedKey() {
		Key tmpKey = null;
		byte[] keybyte = CryptoUtils.generateNonce();
		tmpKey = CryptoUtils.generateAESKey(keybyte);
		setTmpKey(tmpKey);
		//ecrypt key byte Kpub{aeskey}
		byte[] encryptedKey = CryptoUtils.encryptByRSAPublicKey(keybyte, publicKey);
		return encryptedKey;
	}


	public Key getTmpKey() {
		return tmpKey;
	}

	public void setTmpKey(Key tmpKey) {
		this.tmpKey = tmpKey;
	}

	public Map<String, Key> getClientKeyPair() {
		return clientKeyPair;
	}

	public void setClientKeyPair(Map<String, Key> clientKeyPair) {
		this.clientKeyPair = clientKeyPair;
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
