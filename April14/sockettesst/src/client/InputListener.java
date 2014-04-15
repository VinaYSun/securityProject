package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.security.Key;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InputListener extends Thread{
	
	private BufferedReader in;
	private PrintWriter out;
//	private BufferedReader serverIn;
	private Client client = null;
	private Socket socketToServer = null;
	private HashMap<String, String> clientPortDict = null;
	
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
//    	 serverIn = new BufferedReader(new InputStreamReader(socketToServer.getInputStream()));
         out = new PrintWriter(socketToServer.getOutputStream(), true);
         
         while(true){
        	 if(client.getSecretKeyKas() == null){
        		continue;
        	 }else{
        		 String temp = in.readLine();
    	         if(temp==null) continue;
    	         String[] input = temp.split(" ");
    	         int length = input.length;
    	         
    	         if(length == 3 && input[0].equals("chat")){
    	        	 	String peerName = input[1];
    	        	 	String chatmessage = input[2];	
    	        	 	
    	        	 	if(client.getTicketToPeer() == null){
    	        	 		//Message 3, 1 send server Kas{A B R1}
		        	 		System.out.println("get ticket first");
    	        	 	}else{
    	        	 		HashMap<String, Key> peerSecretKeyDict = client.getPeerSecretKeyDict();
    	        	    	if(peerSecretKeyDict.containsKey(peerName)){
    		        	 		//Message 5, 1 send peer Kab{message}
    		        	 		System.out.println("Send peer message");

    		        	 	}else{
    		        	 		//Message 3,3 send peer Ticket, Kab{N2}
    		        	 		System.out.println("set up connection between A, B first");
    		        	 	}
    	        	 	}
    	         }else if(temp.equalsIgnoreCase("list")){
    	        	    	//send kas{list}
    	        	    	
    	         }else if(temp.equalsIgnoreCase("logout")){

    	        	    	//send server Kas{logout}
    		        	    //server knows who is off , update user-secretkey map and update user-port map
    		        	    
    		        	    //get peerSecret key list
    		        	    //send everyone(iterate map keys(username)) on the list, a logout notice
    	        	   
    	         }else{
    	        	     System.out.println("Please user the following command");
    		        	 System.out.println(" 1.'list': get a list of online users");
    		        	 System.out.println(" 2.'chat <username> <message>': send 'username' a message");
    		        	 System.out.println(" 3.'logout': log out system.");
    	        	 
    	         }
        	 }
         }
	}
	
	public HashMap<String, String> getNameMap() {
		return clientPortDict;
	}

	public void setNameMap(HashMap<String, String> nameMap) {
		this.clientPortDict = nameMap;
	}

	/*
	public static HashMap<String, Integer> MapFromJson(String str) {
		Gson gson= new Gson();
	    HashMap<String, Integer> map = new HashMap<String, Integer>();
	    System.out.println(str);
	    return map;
	}
	*/
}
