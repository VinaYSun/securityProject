package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ServerThread extends Thread{

	private BufferedReader in;
	private PrintWriter out;
	private Server server = null;
	private Socket clientSocket = null;
	private HashMap<String, String> clientPortDict;
	
	//parameters for this user thread
	private String username;
	private byte[] gamodp;
	private byte[] gsmodp;
	private byte[] gasmodp;
	private byte[] secretKey;
	private byte[] R1;
	private byte[] R2;
	private byte[] R3;
	private byte[] R4;
	private String password;
	private byte[] pwdKey;
	
	public ServerThread(Server s, Socket client){
		try{
			server = s;
			clientSocket = client;
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        out = new PrintWriter(clientSocket.getOutputStream(), true);
	        
			clientPortDict = new HashMap<String, String>();
			
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
	         
	         System.out.println(temp);
	         
	         //if client sending list, send back the name addressbook<String name, String port>
	         if(temp.equalsIgnoreCase("list")){
	        	 Gson gson = new Gson(); 
	        	 String json = gson.toJson(clientPortDict);
//	        	 System.out.println(json);
	        	 out.println(json);
	         }
	         
	         if(temp.equalsIgnoreCase("sendport")){
	        	 String str = in.readLine();
	        	 updateClientPortDict(str);
	         }
	         
	         if(temp.equalsIgnoreCase("chat")){
	        	 
	          	 out.println("Welcome");
	         }
//	         System.out.println("Socket local port" + clientSocket.getLocalPort() + "  Remote port:" + clientSocket.getPort());
         }
        
	}
	
	
	/**
	 * (after authentication) get client name and its opening port, save to address book
	 * @param str
	 */
	private void updateClientPortDict(String str) {
		String[] split = str.split(" ");
		clientPortDict = server.getClientPortDict();
	    clientPortDict.put(split[0], split[1]);
	    server.setClientPortDict(clientPortDict);
//	    System.out.println(clientPortDict);
	}
}
