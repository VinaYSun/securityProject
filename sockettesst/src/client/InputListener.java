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
	private BufferedReader socketin;
	private client client = null;
	private Socket socket = null;
	private HashMap<String, String> nameMap = null;
	
	public InputListener(client c, Socket socket){
		this.client = c;
		this.socket = socket;
		nameMap = new HashMap<String, String>();
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
    	 socketin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         out = new PrintWriter(socket.getOutputStream(), true);
         
         while(true){
        	 
        	 
	         String temp = in.readLine();
	         
	         if(temp==null) continue;
	         
	         if(temp.equalsIgnoreCase("sendport")){
	        	 	//send "port" request 
	        	 	int port = client.getPortForPeers();
	        	 	String username = client.getUsername();
	        	 	out.println("port"+" "+ username +" "+ port);
	         }else if(temp.equalsIgnoreCase("greetserver")){
	        	 
	        	 	System.out.println(temp);
	        	 	System.out.println("Client side -- Socket local port" +  socket.getLocalPort() + "  Remote port:" +socket.getPort());
	 	         
	        	 	out.println("Message from client XX:  local port" +  socket.getLocalPort() + "  Remote port:" +socket.getPort());
	        	 
	         }else if(temp.equalsIgnoreCase("chat")){
	        	    out.println("chat");

	        	    String mapStr = socketin.readLine();
	        	    System.out.println(mapStr);
	        	    Gson gson = new Gson();
	        	    Type type = new TypeToken<HashMap<String, String>>(){}.getType();
	        	    HashMap<String, String> nameMap = gson.fromJson(mapStr, type);
	        	    client.setNameMap(nameMap);
	        	    
	         }else{
	        	 
		         String[] input= temp.split(" ");
	             if(input.length>2){
		        	 if(input[0].equals("chat")){
		        		 
		        		 String name = input[1];

		        		 //get port number
		        		 nameMap = client.getNameMap();
		        		 String portstr = nameMap.get(name);
		        		 System.out.println(portstr);
		        		 System.out.println(nameMap.get(name));
		        		 int port = Integer.parseInt(portstr);
		        		 
		        		 Socket socket = new Socket("127.0.0.1", port);
		        		 PrintWriter wr = new PrintWriter(socket.getOutputStream(), true);
		        		 wr.println("want to chat with you!");
		    	         System.out.println("Sender local port" +  socket.getLocalPort() + "Receiver port:" +socket.getPort());
		        	 }
		         }
	         }
	         
         }
         
	}
	
	public HashMap<String, String> getNameMap() {
		return nameMap;
	}

	public void setNameMap(HashMap<String, String> nameMap) {
		this.nameMap = nameMap;
	}

	public static HashMap<String, Integer> MapFromJson(String str) {
		Gson gson= new Gson();
	    HashMap<String, Integer> map = new HashMap<String, Integer>();
	    System.out.println(str);
	    return map;
	}
}
