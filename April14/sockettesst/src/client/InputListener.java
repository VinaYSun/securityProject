package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import utils.CryptoUtils;
import utils.Message;
import utils.MessageReader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InputListener extends Thread{
	
	private BufferedReader in;
	private PrintWriter outServer;
//	private BufferedReader serverIn;
	private Client client = null;
	private Socket socketToServer = null;
	private HashMap<String, String> clientPortDict = null;
	private HashMap<String, Key> peerSecretKeyDict = null;
	private HashMap<String, Socket> clientSocketDict = null;
	private Message messageToServer;
	private Message messageToPeer;
	private byte[] R4 = null;
	private byte[] tempkab = null;
	private byte[] ticket = null;
 	
	public InputListener(Client c, Socket socket){
		this.client = c;
		this.socketToServer = socket;
		clientPortDict = new HashMap<String, String>();
	}
	
	@Override
	public void run() {
		try {
			handleInput();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleInput() throws IOException {
		
		 in = new BufferedReader(new InputStreamReader(System.in));
         outServer = new PrintWriter(socketToServer.getOutputStream(), true);
         while(true){
        	//wait for user input
        	 try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	 if(client.getSecretKeyKas() == null){
//        		 System.out.println("Get login first");
        	 }else{
        		 String temp = in.readLine();
    	         if(temp==null) continue;
    	         String[] input = temp.split(" ");
    	         System.out.println("temp" + temp);
    	         int length = input.length;
    	         
    	         if(length == 3 && input[0].equals("chat")){
    	        	 	
    	        	 	String peerName = input[1];
    	        	 	String chatmessage = input[2];	
    	        	 	
    	        	 	peerSecretKeyDict = client.getPeerSecretKeyDict();
    	        	 	
    	        	 	if(peerSecretKeyDict.get(peerName)!=null){
    	        	 		chatPeer(peerName, chatmessage, peerSecretKeyDict.get(peerName));
    	        	 	}else{
    	        	 		
    	        	 		Key key = null;
    	        	 		//send message 31 to server  Kas{ua, ub, R4}
    	        	 		if(requestTicket(peerName)){
    	        	 			Socket socket = new Socket();
    	        	 			
        	        	 		chatPeer(peerName, chatmessage, key);
        	        	 		saveKeyandSocket(peerName, socket, key);
        	        	 		clearTicket();
        	        	 		clearTempKab();
    	        	 		}else{
    	        	 			System.out.println("Connection failed");
    	        	 			System.out.println("Please type <" + temp+ "> again!");
    	        	 		}
    	        	 	}
    	        	 	
    	         }else if(temp.equalsIgnoreCase("list")){
    	        	 System.out.println("==========");
    	        	 requestList();
    	        	 
    	         }else if(temp.equalsIgnoreCase("logout")){
    	        	 requestLogout();
    	         }else{
    	        	 inputReminder();
    	         }
        	 }
         }
	}

	private void chatPeer(String peerName, String message, Key key) {
		//Message 5, 1 send peer Kab{message}
 		System.out.println("Send message:" + peerName + ":"+ message);
	}

	private Boolean requestTicket(String peerName) {
		Boolean b = false;
		//Message 3, 1 send server Kas{A B R4}
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();
		if(R4 == null){
			R4 = CryptoUtils.generateNonce();
		}
		mapout.put("R4", R4);
		mapout.put("receiver", peerName.getBytes(Charset.forName("UTF-8")));
		mapout.put("sender", client.getUsername().getBytes(Charset.forName("UTF-8")));
		
		String msgstr = CryptoUtils.getOutputStream(3, 1, mapout, client.getSecretKeyKas());
		
        outServer.println(msgstr);
        try {
			sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
       
	   HashMap<String, byte[]> mapin = new HashMap<String, byte[]>();
	   
       if(client.getPeerTicketRequestDict().containsKey(peerName)){
    	   Map<String, byte[]> map = client.getPeerTicketRequestDict().get(peerName);
    	   
    	   byte[] ticket = map.get("TicketB");
    	   byte[] receiver = map.get("receiver");
    	   byte[] portbyte = map.get("port");
    	   byte[] r4byte = map.get("R4");
    	   byte[] kabbyte = map.get("kab");
    	   

    	   if((new String(receiver)).equals(peerName) && (new String(r4byte)).equals(new String(R4))){
    		   //set Kab, set Ticket B, set port
    		   setTempkab(kabbyte);
    		   setTicket(ticket);
    		   savePort(peerName, new String(portbyte));
               System.out.println("Send ticket request");
    		   b = true;
    	   }
       }
       
       return b;
	}
	
	private void savePort(String peerName, String port) {
		HashMap<String, String> clientPortDict = new HashMap<String, String>();
		clientPortDict = client.getClientPortDict();
		clientPortDict.put(peerName, port);
		client.setClientPortDict(clientPortDict);
	}

	private void clearTicket() {
		setTempkab(null);
	}

	private void clearTempKab() {
		setTicket(null);
	}

	private void requestLogout() {
		//send server Kas{logout}
	    //server knows who is off , update user-secretkey map and update user-port map
	    
	    //get peerSecret key list
	    //send everyone(iterate map keys(username)) on the list, a logout notice
	}

	private void requestList() {
	   	//send kas{list}
		messageToServer = new Message(2, 1);
		//preapare kas{list}
		HashMap<String, byte[]> mapout = new HashMap<String, byte[]>();
		mapout.put("list", "list".getBytes());
		byte[] data = CryptoUtils.getEncryptedMap(mapout, client.getSecretKeyKas());
		messageToServer.setData(data);
		String str = MessageReader.messageToJson(messageToServer);
		//save list request sent time
		client.setListRequestTime(messageToServer.getTimestamp());
        outServer.println(str);
        System.out.println("Send list request");
	}

	private void inputReminder() {
		 System.out.println("Please user the following command");
    	 System.out.println(" 1.'list': get a list of online users");
    	 System.out.println(" 2.'chat <username> <message>': send 'username' a message");
    	 System.out.println(" 3.'logout': log out system.");		
	}
	
	private void saveKeyandSocket(String peerName, Socket socket, Key key) {
 		clientSocketDict = client.getClientSocketDict();
 		peerSecretKeyDict.put(peerName, key);
 		clientSocketDict.put(peerName, socket);
 		client.setPeerSecretKeyDict(peerSecretKeyDict);
 		client.setClientSocketDict(clientSocketDict);
	}

	public HashMap<String, String> getNameMap() {
		return clientPortDict;
	}

	public void setNameMap(HashMap<String, String> nameMap) {
		this.clientPortDict = nameMap;
	}

	public HashMap<String, Socket> getClientSocketDict() {
		return clientSocketDict;
	}

	public void setClientSocketDict(HashMap<String, Socket> clientSocketDict) {
		this.clientSocketDict = clientSocketDict;
	}

	public byte[] getTempkab() {
		return tempkab;
	}

	public void setTempkab(byte[] tempkab) {
		this.tempkab = tempkab;
	}

	public byte[] getTicket() {
		return ticket;
	}

	public void setTicket(byte[] ticket) {
		this.ticket = ticket;
	}

}
