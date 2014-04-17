package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import utils.CryptoUtils;
import utils.Message;
import utils.MessageReader;

public class peerThread extends Thread{

	private BufferedReader in;
	private PrintWriter out;
	private Client client = null;
	private Socket peerSocket = null;
	private String peerName;
	private byte[] ticket;
	private byte[] R5;
	private byte[] kab;
	private byte[] gamodp;
	private byte[] gbmodp;
	private byte[] gabmodp;
	private Key secretKeyKab;
	private Map<String, Key> DHKeyPair = null;
	private byte[] tempKab;
	


	private Message messageFromPeer;
	private Message messageToPeer;
	private static final int LOGIN_AUTH = 1;
	private static final int LIST_REQUEST = 2;
	private static final int CHAT_REQUEST = 3;
	private static final int LOGOUT_REQUEST = 5;
	private static final int MESSAGE = 4;

	
	public peerThread(Client c, Socket s){
		try{
			this.client = c;
			this.peerSocket = s;
			
			in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
	        out = new PrintWriter(peerSocket.getOutputStream(), true);
		
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

	//get connection notice, update secret key dict, add new user
	//get logout notice, update secret key dict, delete this user
	
	private void handleSocket() throws IOException {
		
		 in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
         out = new PrintWriter(peerSocket.getOutputStream(), true);
         
         while(true){
	         String temp = in.readLine();
	    
	         if(temp==null) continue;
	         
//	         System.out.println(temp);

	         messageFromPeer = MessageReader.messageFromJson(temp);
	         messageToPeer = new Message(0,0);
	         
	         switch (messageFromPeer.getProtocolId()){
	        	 case LOGIN_AUTH:
	        		 break;
	        	 case LIST_REQUEST:
	        		 break;
	        	 case CHAT_REQUEST:
	        		 chatRequest(messageFromPeer);
	        		 break;
	        	 case LOGOUT_REQUEST:
	        		 break;
	        	 case MESSAGE:
	        		 handleMessage(messageFromPeer);
	        	 default:
	        		 break;
	         }
	         
         }
        
	}

	private void handleMessage(Message message) {
		int step = message.getStepId();
		if(step == 1){
			handleMessage41(message.getDataBytes());
		}else{
			System.out.println("Step doesn't exist");
		}
	}

	private void handleMessage41(byte[] dataBytes) {

		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		map = CryptoUtils.getDecryptedMap(dataBytes, getSecretKeyKab());
		byte[] msg = map.get("message");
		byte[] sig = map.get("signature");
//		System.out.println("==========receive any===========");

		byte[] b = CryptoUtils.getMD5(msg);
    	if((new String(b)).equals(new String(sig))){
        	System.out.println(getPeerName() + ": " + new String(msg));
    	}
	}

	private void chatRequest(Message message) {
		int step = message.getStepId();
		if(step == 3){
			handleMessage33(message.getDataBytes());
		}else if(step == 5){
			handleMessage35(message.getDataBytes());
		}else{
			System.out.println("Step doesn't exist");
		}
	}

	private void handleMessage35(byte[] data) {
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
//		System.out.println("==========receive 3 5===========");
		
		map = CryptoUtils.getDecryptedMap(data, getTempKab());
		byte[] gamodp = map.get("GAmodP");

    	Key priKey = getDHKeyPair().get("private_key");
    	byte[] sessionkeyKab = CryptoUtils.generateSessionKey(gamodp, priKey.getEncoded());
    	Key sessionKey = CryptoUtils.generateAESKey(sessionkeyKab);
    	
    	setSecretKeyKab(sessionKey);
		
	}

	private void handleMessage33(byte[] data) {
//		   System.out.println("==========receive 3 3===========");

		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		HashMap<String, byte[]> mapdata = new HashMap<String, byte[]>();
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();

		map = CryptoUtils.mapFromByte(data);
		byte[] r5byte = map.get("encryptR5");
		byte[] ticketbyte = map.get("Ticket");

		mapdata = CryptoUtils.getDecryptedMap(ticketbyte, client.getSecretKeyKas());
		byte[] sendername = mapdata.get("A");
		byte[] kabbytekey = mapdata.get("kabbyte");
		setTempKab(kabbytekey);
		byte[] R5 = CryptoUtils.decryptByAES(r5byte, CryptoUtils.generateAESKey(kabbytekey));
		
		setPeerName(new String(sendername));
		
		//prepare GBmodP
		Map<String, Key> bDHKeyMap = CryptoUtils.generateDHKey();
		setDHKeyPair(bDHKeyMap);
		
		mapout = new HashMap<String, byte[]>();
		mapout.put("R5", R5);
		mapout.put("GBmodP", bDHKeyMap.get("public_key").getEncoded());

	    String msgstr = CryptoUtils.getOutputStream(3, 4, mapout, CryptoUtils.generateAESKey(kabbytekey));
	    out.println(msgstr);
//		System.out.println("***********send 3 4 *************");

	}

	public Map<String, Key> getDHKeyPair() {
		return DHKeyPair;
	}

	public void setDHKeyPair(Map<String, Key> dHKeyPair) {
		DHKeyPair = dHKeyPair;
	}

	public String getPeerName() {
		return peerName;
	}

	public void setPeerName(String peerName) {
		this.peerName = peerName;
	}

	public Key getSecretKeyKab() {
		return secretKeyKab;
	}

	public void setSecretKeyKab(Key secretKeyKab) {
		this.secretKeyKab = secretKeyKab;
	}
	
	public byte[] getTempKab() {
		return tempKab;
	}

	public void setTempKab(byte[] tempKab) {
		this.tempKab = tempKab;
	}
	
}