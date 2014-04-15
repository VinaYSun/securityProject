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
	private Socket socket = null;
	private HashMap<String, String> clientPortDict;
	
	
	public ServerThread(Server s, Socket soc) throws IOException{
		
		server = new Server();
		socket = new Socket();
		server = s;
		socket = soc;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        String temp = in.readLine();
		initialClientPortDict(temp);
	
	}
	
	private void initialClientPortDict(String str) {
		clientPortDict = new HashMap<String, String>();
		clientPortDict = server.getNameaddr();
		String[] split=str.split(" ");
	    clientPortDict.put(split[0], split[1]);
	    server.setNameaddr(clientPortDict);
	    System.out.println(clientPortDict);
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
	        	 System.out.println(json);
	        	 out.println(json);
	         }
	         
	         System.out.println("Socket local port" +  socket.getLocalPort() + "  Remote port:" +socket.getPort());
         }
        
	}

}
