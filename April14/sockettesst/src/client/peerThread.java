package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.util.HashMap;

public class peerThread extends Thread{

	private BufferedReader in;
	private PrintWriter out;
	private Client client = null;
	private Socket peerSocket = null;
	private HashMap<String, Key> peerSecretKeyDict = null;
	private String peerName;
	private byte[] ticket;
	private byte[] R5;
	private byte[] kab;
	private byte[] gamodp;
	private byte[] gbmodp;
	private byte[] gabmodp;
	private Key secretKey;
	
	public peerThread(Client c, Socket s){
		this.client = c;
		this.peerSocket = s;
		peerSecretKeyDict = new HashMap<String, Key>();

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
	         System.out.println(temp);

	         //get Ticket-to-Me, kab{N2}
	         
	         //verify and SEND Kab{N2, gBmodp}
	         
	         //get Kab{gamodp}
	         
	         //set session key between this invitor A and me
	         /*
	         peerSecretKeyDict = client.getPeerSecretKeyDict();
	         peerSecretKeyDict.put(peerName, sessionKey);
	         client.setPeerSecretKeyDict(peerSecretKeyDict);
	         */
	         
//	         System.out.println("local port: " +  peerSocket.getLocalPort() + " port: " +peerSocket.getPort());

	         out.println("Got your msg!");
	         
         }
        
	}
}
