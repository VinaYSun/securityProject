package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InputListener extends Thread{
	
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader serverIn;
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
    	 serverIn = new BufferedReader(new InputStreamReader(socketToServer.getInputStream()));
         out = new PrintWriter(socketToServer.getOutputStream(), true);
         
         while(true){
        	 
	         String temp = in.readLine();
	         if(temp==null) continue;
	         
	         if(temp.equalsIgnoreCase("sendport")){
	        	 	//send "port" request 
	        	 	int port = client.getPortForPeers();
	        	 	String username = client.getUsername();
	        	 	out.println("sendport");
	        	 	out.println(username +" "+ port);
	         }else if(temp.equalsIgnoreCase("chat")){
	        	 	out.println("chat");
	        	 	out.println("Message from client XX:  local port" +  socketToServer.getLocalPort() + "  Remote port:" +socketToServer.getPort());
	        	 	String str = serverIn.readLine();
	        	 	System.out.println("From server: "  +str);
	        	 	
	         }else if(temp.equalsIgnoreCase("list")){
	        	    out.println("list");

	        	    String mapStr = serverIn.readLine();
	        	    System.out.println(mapStr);
	        	    Gson gson = new Gson();
	        	    Type type = new TypeToken<HashMap<String, String>>(){}.getType();
	        	    HashMap<String, String> nameMap = gson.fromJson(mapStr, type);
	        	    client.setClientPortDict(nameMap);
	        	    
	         }else if(temp.equalsIgnoreCase("logout")){
	        	    out.println("logout");

	        	    //send server a logout notice
	        	    //server knows who is off , update user-secretkey map and update user-port map
	        	    
	        	    //get peerSecret key list
	        	    //send everyone(iterate map keys(username)) on the list, a logout notice
	        	    
	        	    
	        	    
	         }else{
	        	 
		         String[] input= temp.split(" ");
	             if(input.length>2){
		        	 if(input[0].equals("chat")){
		        		 
		        		 String name = input[1];
		        		 //get port number
		        		 clientPortDict = client.getClientPortDict();
		        		 String portstr = clientPortDict.get(name);
		        		 int port = Integer.parseInt(portstr);
		        		 
		        		 Socket socket = new Socket("127.0.0.1", port);
		        		 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		        		 PrintWriter wr = new PrintWriter(socket.getOutputStream(), true);
		        		 wr.println("from " + client.getUsername() + ": "+ input[2]);
		        		 System.out.println("!!!!!!");
		        		 String message = br.readLine();
		        		 System.out.println(message);
//		        		 socket.close();
		        	 }
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
