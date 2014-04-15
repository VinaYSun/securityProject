package utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * constructs message 
 * @author yaweisun
 * @param  protocolId
 * @param  stepId
 * @param  data
 */
public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int protocolId;
	private int stepId;
	private byte[] data;
	private String timestamp;
	private HashMap<String, Object> dataMap;
	
	public Message(int protocolId, int stepId, byte[] data){
		this.protocolId = protocolId;
		this.stepId = stepId;
		this.data = data;
		this.timestamp = this.getTimestampToString(System.currentTimeMillis());
	}
	
	public Message(int protocolId, int stepId, String dataString){
		this.protocolId = protocolId;
		this.stepId = stepId;
		this.data = dataString.getBytes(Charset.forName("UTF-8"));
		this.timestamp = this.getTimestampToString(System.currentTimeMillis());
	}
	
	public Message(int protocolId, int stepId){
		this.protocolId = protocolId;
		this.stepId = stepId;
		this.timestamp = this.getTimestampToString(System.currentTimeMillis());
	}
	
	public Message() {
		this.data = null;
		this.timestamp = this.getTimestampToString(System.currentTimeMillis());
		this.protocolId = 0;
		this.stepId = 0;
	}
	
	
	public int getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}

	public int getStepId() {
		return stepId;
	}

	public void setStepId(int stepId) {
		this.stepId = stepId;
	}

	public byte[] getDataBytes() {
		return data;
	}
	
	public String getData(){
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new String(data);
		}
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}

	public void setData(String data){
		this.data = data.getBytes(Charset.forName("UTF-8"));
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestampToString(long timestamp){
		  SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		  Long time=new Long(timestamp);
		  String d = format.format(time);
		  return d;
	}
	
	public static int getSid(String in) {
		
		int stepid = 0;
		
		Message msg = new Message();
		msg = MessageReader.messageFromJson(in);

		stepid = msg.getStepId();
		return stepid;
	}

	public static int getPid(String in) {
		int pid = 0;
		
		Message msg = new Message();
		msg = MessageReader.messageFromJson(in);

		pid = msg.getProtocolId();
		return pid;
	}

	/**
	 * get a input string (decrypted), and return a map
	 * @param in
	 * @param key in byte
	 * @return
	 */
	public static HashMap<String, byte[]> getDataMap(String in, byte[] key) {
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		
		byte[] plaindata = null;
		byte[] cipherdata = null;
		
		Message msg = new Message();
		msg = MessageReader.messageFromJson(in);
		cipherdata = msg.getDataBytes();
		plaindata = CryptoUtils.decryptByAES(cipherdata, key);
		map = CryptoUtils.mapFromByte(plaindata);
		
		return map;
	}
	
	/**
	 * get input message string(not decrypted), and return the map contained in the message
	 * @param in
	 * @return
	 */
	public static HashMap<String, byte[]> getDataMap(String in) {
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		
		byte[] data = null;
		
		Message msg = new Message();
		msg = MessageReader.messageFromJson(in);
		data = msg.getDataBytes();
		map = CryptoUtils.mapFromByte(data);
		
		return map;
	}
	
	/**
	 * get a message string(decrypted), and return the map contained in the message
	 * @param in
	 * @param key in Key
	 * @return
	 */
	public static HashMap<String, byte[]> getDataMap(String in, Key key) {
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		
		byte[] plaindata = null;
		byte[] cipherdata = null;
		
		Message msg = new Message();
		msg = MessageReader.messageFromJson(in);
		cipherdata = msg.getDataBytes();
		
		plaindata = CryptoUtils.decryptByAES(cipherdata, key);

		map = CryptoUtils.mapFromByte(plaindata);
		
		return map;
	}
	
	/**
	 * get output with map encrypted
	 * @param protocolId
	 * @param stepId
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getOutputStream(int protocolId, int stepId, HashMap<String, byte[]> map, Key key) {
		String output = null;
		byte[] inputdata = null;
		byte[] cipherdata = null;
 		
		inputdata = CryptoUtils.mapToByte(map);
		cipherdata = CryptoUtils.encryptByAES(inputdata, key);
		
		Message msg = new Message(protocolId, stepId);
		msg.setData(cipherdata);
		output = MessageReader.messageToJson(msg);
		
		return output;
	}
	
	/**
	 * get output with map not encrypted
	 * @param protocolId
	 * @param stepId
	 * @param map
	 * @return
	 */
	public static String getOutputStream(int protocolId, int stepId, HashMap<String, byte[]> map) {
		String output = null;
		byte[] inputdata = null;
 		
		inputdata = CryptoUtils.mapToByte(map);
		
		Message msg = new Message(protocolId, stepId);
		msg.setData(inputdata);
		output = MessageReader.messageToJson(msg);
		
		return output;
	}
	
	public static byte[] getEncryptedMap(HashMap<String, byte[]> map, Key key){
		byte[] inputdata = null;
		byte[] cipherdata = null;
		inputdata = CryptoUtils.mapToByte(map);
		cipherdata = CryptoUtils.encryptByAES(inputdata, key);
		return cipherdata;
	}
	
	public static HashMap<String, byte[]> getDecryptedMap(byte[] mapbyte, Key key){
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		byte[] plaindata = null;
		plaindata = CryptoUtils.decryptByAES(mapbyte, key);
		map = CryptoUtils.mapFromByte(plaindata);
		return map;
	}
	
	public static HashMap<String, byte[]> getDecryptedMap(byte[] mapbyte, byte[] key){
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		byte[] plaindata = null;
		plaindata = CryptoUtils.decryptByAES(mapbyte, key);
		map = CryptoUtils.mapFromByte(plaindata);
		return map;
	}
	
	public static void main(String args[]) throws NoSuchAlgorithmException{
			
		//message 1
		Message msgIn = new Message(1,1,"login");
		
		String str= MessageReader.messageToJson(msgIn);
		
		Message msgOut = new Message();
		msgOut = MessageReader.messageFromJson(str);
		System.out.println(msgOut.getData());
		
		//message 2
		Message msgIn2 = new Message(1,2);
		byte[] random = new byte[128/8];
		try {
			random  = CryptoUtils.generateNonce();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String IP = "127.0.0.1";
		
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		map.put("IP", IP.getBytes(Charset.forName("UTF-8")));
		map.put("cookie", random);
		try {
			System.out.print(new String(random, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		msgIn2.setData(CryptoUtils.mapToByte(map));
		
		String str2= MessageReader.messageToJson(msgIn2);
		
		Message msgOut2 = new Message();
		msgOut2 = MessageReader.messageFromJson(str2);
		HashMap<String, byte[]> map2 = new HashMap<String, byte[]>();
		map2 = CryptoUtils.mapFromByte(msgOut2.getDataBytes());
		byte[] outIp = map2.get("IP");
		byte[] outcookie = map2.get("cookie");
		
		try {
			String outIPStr = new String(outIp, "UTF-8");
			String outcookieStr = new String(outcookie, "UTF-8");
			System.out.println(outIPStr + " and " + outcookieStr);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//message 3
		byte[] inputdata =null;
		byte[] cipher = null;
		byte[] encyrptdata = null;
		byte[] plaindata = null;
		
				
		
		try {
			Message msgIn3 = new Message(1,3);
			//message 3
			//prepare aes key
			Key tmpKey = null;
			//128bits random 
			byte[] keybyte = CryptoUtils.generateNonce();
			//generate a tmp aeskey
			tmpKey = CryptoUtils.generateAESKey(keybyte);
			
			//ecrypt key byte1 Kpub{aeskey}
			byte[] encryptedKey = CryptoUtils.encryptByRSAPublicKey(keybyte, CryptoUtils.getPublicKey("public.der"));
	        
			System.out.println("Key length" + encryptedKey.length);
	        System.out.println("Key length" + keybyte.length);

			//prepare parameters
			byte[] R2 = CryptoUtils.generateNonce();
			String password = "thankyou";
			String port = "9092";
			String username = "yawei";
			Map<String, Key> dhKeyMap = CryptoUtils.generateDHKey();
			System.out.println(new String(R2, "UTF-8"));
			
			HashMap<String, byte[]> map3 = new HashMap<String, byte[]>();
			map3.put("R2", R2);
			map3.put("password", password.getBytes(Charset.forName("UTF-8")));
			map3.put("port", port.getBytes(Charset.forName("UTF-8")));
			map3.put("username", username.getBytes(Charset.forName("UTF-8")));
			map3.put("gamodp", dhKeyMap.get("public_key").getEncoded());
			
			//convert map -> encrypted byte
			byte[] mapbyte = getEncryptedMap(map3, tmpKey);
			
			//put encryptedkey and mapbyte into a map 
			map2 = new HashMap<String, byte[]>();
			map2.put("key", encryptedKey);
			map2.put("datamap", mapbyte);
			
			str2 = getOutputStream(1, 3, map2);
//			String str3 = getOutputStream(1, 3, map3, tmpKey);
			
//			String outputStr3 = str3 + "eof" + new String(encryptedKey, "UTF-8");
			
//			String[] input = outputStr3.split("eof");
//			String inputStr1 = input[0];
//			String keyString = input[1];
			
			HashMap<String, byte[]> map7 = new HashMap<String, byte[]>();
			map7 = getDataMap(str2);
			byte[] encyrptedmap = map7.get("datamap");
			byte[] getKeybyte = map7.get("key");
			
			HashMap<String, byte[]> map4 = new HashMap<String, byte[]>();
			
			byte[] decryptedKey = CryptoUtils.decryptByRSAPrivateKey(getKeybyte, CryptoUtils.getPrivateKey("private.der"));
//			byte[] decryptedmap = CryptoUtils.decryptByAES(encyrptedmap, decryptedKey);
//			Key generatedAeskey = CryptoUtils.generateAESKey(decryptedKey);
			map4 = getDecryptedMap(encyrptedmap, decryptedKey);

			byte[] passwordbyte = map4.get("password");
			byte[] r2byte= map4.get("R2");
			byte[] portbyte = map4.get("port");
			byte[] usernamebyte = map4.get("username");
			byte[] gamodp = map4.get("gamodp");
			
			System.out.println("password: "+ new String(passwordbyte, "UTF-8"));
			System.out.println("R2: "+ new String(r2byte, "UTF-8"));
			System.out.println("port: "+ new String(portbyte, "UTF-8"));
			System.out.println("user: " + new String(usernamebyte, "UTF-8"));
			
			if(password.equals(new String(passwordbyte, "UTF-8"))){
				System.out.println("correct 3 2");
			}
			if((new String(R2)).equals(new String(r2byte))){
				System.out.println("correct 3 2");
			}
			
			
			//message 4 W{gsmodp,R3} W = {R2|password}
			
			//prepare W
			byte[] pwdKey =  CryptoUtils.getSaltHashByteArr(passwordbyte, r2byte);
			Key pwdaesKey = CryptoUtils.generateAESKey(pwdKey);
			
			//prepare gsmodp 
        	Map<String, Key> DHServerkey = CryptoUtils.generateDHKey(gamodp);
        	
			//prepare R3
			byte[] R3 = CryptoUtils.generateNonce();
			
			HashMap<String, byte[]> map5 = new HashMap<String, byte[]>();
			map5.put("R3", R3);
			map5.put("gsmodp", DHServerkey.get("public_key").getEncoded());
			map5.put("test", "test".getBytes(Charset.forName("UTF-8")));
			
			/**
			 * give protocol, step, map, key, have output stream back 
			 * 
			 */
			
			String str5 = getOutputStream(1, 4, map5, pwdaesKey);
			
			
			
			HashMap<String, byte[]> map6 = new HashMap<String, byte[]>();
			
			//get message 4
			byte[] pwdKey2 = CryptoUtils.getSaltHashByteArr(password.getBytes(),R2);
			Key pwdaesKey2 = CryptoUtils.generateAESKey(pwdKey2);

			map6 = getDataMap(str5, pwdaesKey2);
			byte[] r3byte = map6.get("R3");
			byte[] gsmodp = map6.get("gsmodp");
			if((new String(R3)).equals(new String(r3byte))){
				System.out.println("R3 correct!");
			}
			
			 
			//message 5  Kas{R4}
			
			//generate gasmodp session key and SAVE
        	Key dhClientPriKey = dhKeyMap.get("private_key");
        	byte[] sessionkeyKas = CryptoUtils.generateSessionKey(gsmodp, dhClientPriKey.getEncoded());
        	Key sessionKey=  CryptoUtils.generateAESKey(sessionkeyKas);
        	
			//generate gasmodp session key and save the other side
        	Key dhServerPriKey = DHServerkey.get("private_key");
        	byte[] KasforServer  = CryptoUtils.generateSessionKey(gamodp, dhServerPriKey.getEncoded());
        	Key sessionKey2=   CryptoUtils.generateAESKey(KasforServer);
        	
			//pack R3
			HashMap<String, byte[]> map9 = new HashMap<String, byte[]>();
			map9.put("R3", r3byte);
			
			String str9 = getOutputStream(1, 5, map9, sessionKey);
			
			
			HashMap<String, byte[]> map8 = new HashMap<String, byte[]>();
			map8 = getDataMap(str9, sessionKey2);
			byte[] r3back = map8.get("R3");
			if((new String(r3back)).equals(new String(r3byte))){
				System.out.println("Step 5 correct!");
			}
			
			
			//message 3, 1 
			

			//Kas{A,B,R4}
			
			//prepare r4
			byte[] R4 = CryptoUtils.generateNonce();
			
			String nameA = "yawei";
			String nameB = "Crazy";
			
		    map = new HashMap<String, byte[]>();
			map.put("R4", R4);
			map.put("A", nameA.getBytes(Charset.forName("UTF-8")));
			map.put("B", nameB.getBytes(Charset.forName("UTF-8")));
			
			str = getOutputStream(3, 1, map, sessionKey);
			
			map2 = new HashMap<String, byte[]>();
			map2 = getDataMap(str, sessionKey2);
			byte[] r4byte = map2.get("R4");
			byte[] nameAbyte = map2.get("A");
			byte[] nameBbyte = map2.get("B");
			
			if((new String(nameAbyte)).equals(nameA)){
				System.out.println("Step 3 1  correct!");
			}
			
			
			//message 3,2
			//prepare TICKET Kbs{kab, A}
			byte[] keybyteKbs = CryptoUtils.generateNonce();
			//generate a tmp aeskey
			Key tmpKeyKbs = CryptoUtils.generateAESKey(keybyteKbs);
			
			
			
			//prepare kab
			byte[] keybyteKab = CryptoUtils.generateNonce();
			//generate a tmp aeskey
			Key tmpKeyKab = CryptoUtils.generateAESKey(keybyteKab);
			
			map3 = new HashMap<String, byte[]>();
			map3.put("kabbyte", keybyteKab);
			map3.put("A", nameAbyte);
			
			byte[] map3byte = getEncryptedMap(map3, tmpKeyKbs);
			
			//{map3byte, B, kab, R4} kas
			
			map = new HashMap<String, byte[]>();
			map.put("R4", r4byte);
			map.put("mapbyte", map3byte);
			map.put("B", nameBbyte);
			map.put("kab", keybyteKab);
			
			str = getOutputStream(3, 2, map, sessionKey);
			
			map2 = new HashMap<String, byte[]>();
			map2 = getDataMap(str, sessionKey2);
			byte[] r4byte2 = map2.get("R4");
			byte[] mapbyte2 = map2.get("mapbyte");
			byte[] nameBbyte2 = map2.get("B");
			byte[] kabbyte = map2.get("kab");
			
			if((new String(nameBbyte2)).equals(nameB)){
				System.out.println("Step 3 2  correct!");
			}
			
			
			//message 3,3  kab{R5}, ticketB
			//prepare ticket and R5
		
			byte[] R5 = CryptoUtils.generateNonce();
			byte[] encryptR5 = CryptoUtils.encryptByAES(R5, CryptoUtils.generateAESKey(kabbyte));
			map = new HashMap<String, byte[]>();
			map.put("encryptR5", encryptR5);
			map.put("Ticket", mapbyte2);
			
			str = getOutputStream(3, 3, map);
			
			map2 = new HashMap<String, byte[]>();
			map2 = getDataMap(str);
			byte[] r5byte = map2.get("encryptR5");
			
			//kbs{kab, a}
			byte[] ticketbyte = map2.get("Ticket");
			
			map3 = new HashMap<String, byte[]>();
			map3 = getDecryptedMap(ticketbyte, tmpKeyKbs);
			byte[] abytename = map3.get("A");
			byte[] kabbytekey = map3.get("kabbyte");
			
			byte[] r5inMessage3 = CryptoUtils.decryptByAES(r5byte, CryptoUtils.generateAESKey(kabbytekey));
			
			if((new String(r5inMessage3)).equals(new String(R5))){
				System.out.println("Step 3 3  correct!");
			}
			
			//message 3,4 Kab{N5, GBmodP}
			
			
			
			/////////////
			//prepare parameters
			//prepare GBmodP
			Map<String, Key> bDHKeyMap = CryptoUtils.generateDHKey();
			
			map = new HashMap<String, byte[]>();
			map.put("R5", r5inMessage3);
			map.put("GBmodP", bDHKeyMap.get("public_key").getEncoded());
			
		    str = getOutputStream(3, 4, map, CryptoUtils.generateAESKey(kabbytekey));
			
			map2 = new HashMap<String, byte[]>();
		    Key generatedKab = CryptoUtils.generateAESKey(kabbyte);
			map2 = getDataMap(str, generatedKab);

			byte[] n5fromB = map2.get("R5");
			byte[] GBmodP = map2.get("GBmodP");
			
			if((new String(n5fromB)).equals(new String(R5))){
				System.out.println("Step 3 4  correct!");
			}
			
			//message 3,5
			//prepare GAmodP
			Map<String, Key> aDHKeyMap = CryptoUtils.generateDHKey(GBmodP);
        	
			map = new HashMap<String, byte[]>();
			map.put("GAmodP", aDHKeyMap.get("public_key").getEncoded());
			
		    str = getOutputStream(3, 5, map, CryptoUtils.generateAESKey(kabbytekey));
			
			map2 = new HashMap<String, byte[]>();
			map2 = getDataMap(str, generatedKab);

			byte[] GAmodP = map2.get("GAmodP");
			
			
			
			//generate Kab  key and SAVE
        	Key aPriKey = aDHKeyMap.get("private_key");
        	byte[] kabforA = CryptoUtils.generateSessionKey(GBmodP, aPriKey.getEncoded());
        	Key sessionKeyA =  CryptoUtils.generateAESKey(kabforA);
        	
			//generate Kab  key and save the other side
        	Key bPriKey = bDHKeyMap.get("private_key");
        	byte[] kabforB  = CryptoUtils.generateSessionKey(GAmodP, bPriKey.getEncoded());
        	Key sessionKeyB =   CryptoUtils.generateAESKey(kabforB);
		    
        	
        	
        	String success = "success";
        	byte[] in = CryptoUtils.encryptByAES(success.getBytes(), sessionKeyA);
        	byte[] out = CryptoUtils.decryptByAES(in, sessionKeyB);
        	
        	if((new String(out)).equals(success)){
        		System.out.println("Step 3 5  correct!");
        	}
			///////////
			
			
			
			
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}



}
