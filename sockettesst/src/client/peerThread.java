package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class peerThread extends Thread{

	private BufferedReader in;
	private PrintWriter out;
	private client client = null;
	private Socket peer = null;
	
	public peerThread(client c, Socket s){
		this.client = c;
		this.peer = s;
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
		
		 in = new BufferedReader(new InputStreamReader(peer.getInputStream()));
         out = new PrintWriter(peer.getOutputStream(), true);
         
         while(true){
        	 
        	 
	         String temp = in.readLine();
	         
	         if(temp==null) continue;
	         
	         System.out.println(temp);
	         System.out.println(" local port" +  peer.getLocalPort() + "   port:" +peer.getPort());

	         out.println("Message from peer:  local port" +  peer.getLocalPort() + "  Remote port:" +peer.getPort());
	         
         }
        
	}
}
