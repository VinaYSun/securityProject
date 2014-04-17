package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CryptoUtils {
	
	
	/**
	 * Get 128 bits nonce
	 * @return byte
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] generateNonce(){ 
        SecureRandom random;
        byte[] bytes = new byte[128/8];
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		    random.nextBytes(bytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return bytes;
	}
	
	
	  /**
     * Convert Map<String, byte[]> to byte 
     * @param Map<String, byte[]>
     * @return byte[]
     */
    public static byte[] mapToByte(Map<String, byte[]> map){
    	Gson gson = new Gson();
    	String str = gson.toJson(map,  new TypeToken<HashMap<String, byte[]>>(){}.getType());
    	return str.getBytes(Charset.forName("UTF-8"));
    }
    
    /**
     * Convert byte to Map<String, byte[]>
     * @param byte
     * @return Map<String, byte[]>
     */
    public static HashMap<String, byte[]> mapFromByte(byte[] b){
    	String json;
		try {
			json = new String(b, "UTF-8");
			Gson gson = new Gson();
			HashMap<String, byte[]> map = gson.fromJson(json, new TypeToken<HashMap<String, byte[]>>(){}.getType());
	    	return map;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("MapFromByte Encoding error");
			return null;
		}
    }
    
	/**
	 * Generate aes secret key
	 * @return AES key
	 */
	public static Key generateAESKey(byte[] bytes)  {
		SecretKey key = new SecretKeySpec(bytes, "AES");
		return key;
	}
    
	/**
	 * AES Encrypt
	 * @param data
	 * @param AES key
	 * @return encrypted data
	 * @throws Exception
	 */
    public static byte[] encryptByAES(byte[] data, Key key){  
        Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		System.out.println("Encrypting error!");
		return data;
    } 
    
	/**
	 * AES Decrypt
	 * @param data
	 * @param AES key
	 * @return encrypted data
	 * @throws Exception
	 */
    public static byte[] decryptByAES(byte[] data, Key key){  
    	try{
    		Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");  
    		cipher.init(Cipher.DECRYPT_MODE, key);  
    		return cipher.doFinal(data);  
	    } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		System.out.println("Decrypting error!");
		return data;
    }  
    
	/**
	 * AES Decrypt
	 * @param data
	 * @param AES key
	 * @return derypted data
	 * @throws Exception
	 */
    public static byte[] decryptByAES(byte[] data, byte[] key){  
     
		try {
			SecretKey secretKey=new SecretKeySpec(key,"AES");  
		    Cipher cipher;
			cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}  
		System.out.println("Decryption error!");
		return data;
    } 
    
	/**
	 * Load private key from keyfile
	 * @param privateKeyFileName
	 * @return  PrivateKey
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String privateKeyFileName){
		KeyFactory rsaKeyFactory;
		PrivateKey key = null;
		try {
			rsaKeyFactory = KeyFactory.getInstance("RSA");
			byte[] privateKey;
			privateKey = FileUtils.toByteArray(privateKeyFileName);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
			key = rsaKeyFactory.generatePrivate(privateKeySpec);
		} catch (InvalidKeySpecException e) {
				e.printStackTrace();
		}catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println("private key is generated");
		return key;
	}
	
	/**
	 * Load public key from keyfile
	 * @param publicKeyFileName
	 * @return PublicKey
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(String publicKeyFileName){
		KeyFactory rsaKeyFactory;
		PublicKey key = null;
		try {
			rsaKeyFactory = KeyFactory.getInstance("RSA");
			byte[] publicKey = FileUtils.toByteArray(publicKeyFileName);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
			key = rsaKeyFactory.generatePublic(publicKeySpec);
//			System.out.println("public key is generated");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	/**
	 * Encyrption with RSA public key
	 * @param plaintext
	 * @param publicKey
	 * @return cipherdata
	 * @throws Exception
	 */
	public static byte[] encryptByRSAPublicKey(byte[] data, Key publicKey){
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		    return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		System.out.println("decrypting error");
        return data;
	}
	
	/**
	 * Decryption with RSA private key
	 * @param cipherdata
	 * @param privateKey
	 * @return plaintext
	 * @throws Exception
	 */
	public static byte[] decryptByRSAPrivateKey(byte[] data, Key privateKey){
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        cipher.init(Cipher.DECRYPT_MODE, privateKey);
	        return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		System.out.println("decrypting error");
		return data;
	}
	
	/**
	 * Generate 512-bits DH key pair
	 */
	public static Map<String, Key> generateDHKey(){  
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("DH");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("keypairGenerator error");
			e.printStackTrace();
		}  
        keyPairGenerator.initialize(512);  
        KeyPair keyPair=keyPairGenerator.generateKeyPair();  
        DHPublicKey publicKey=(DHPublicKey) keyPair.getPublic();  
        DHPrivateKey privateKey=(DHPrivateKey) keyPair.getPrivate();  
        Map<String,Key> keyMap = new HashMap<String,Key>();  
        keyMap.put("public_key", publicKey);  
        keyMap.put("private_key", privateKey);  
        return keyMap;  
    } 
	
	/**
	 * Hash with salt using SHA-256
	 * @param string
	 * @param salt
	 * @return hash String
	 */
	public static String getSaltHash(byte[] string, byte[] salt)
	{
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(string);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }
	
	
	public static byte[] getSaltHashByteArr(byte[] string, byte[] salt)
	{
		byte[] bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            bytes = md.digest(string);

        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }
        return bytes;
    }
	
	
	/**
	 * Generate 512-bits DH key pair from input key 
	 * @param public key from the other side
	 * @return Map<keyname, key>
	 * @throws Exception
	 */
	public static Map<String,Key> generateDHKey(byte[] key) {  
	    X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);  
	    KeyFactory keyFactory = null;
        Map<String,Key> keyMap=new HashMap<String,Key>();  

		try {
			keyFactory = KeyFactory.getInstance("DH");
		    PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);  
		    DHParameterSpec dhParamSpec=((DHPublicKey)pubKey).getParams();  
	        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(keyFactory.getAlgorithm());  
	        keyPairGenerator.initialize(dhParamSpec); 
	        KeyPair keyPair=keyPairGenerator.genKeyPair();  
	        DHPublicKey publicKey=(DHPublicKey)keyPair.getPublic();  
	        DHPrivateKey privateKey=(DHPrivateKey)keyPair.getPrivate();  
	        keyMap.put("public_key", publicKey);  
	        keyMap.put("private_key", privateKey);  
	        
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}  
        return keyMap;  
	}
	
	/**
	 * Generate Session Key
	 * @return session key
	 * @throws IllegalStateException 
	 * @throws InvalidKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	public static byte[] generateSessionKey(byte[] publicKey, byte[] privateKey) {
		
		KeyFactory keyFactory;
		SecretKey secretKey = null;
		
		try {
			keyFactory = KeyFactory.getInstance("DH");
			X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(publicKey);  
		    PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);  
		    PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(privateKey);  
		    PrivateKey priKey=keyFactory.generatePrivate(pkcs8KeySpec);  
            KeyAgreement keyAgree=KeyAgreement.getInstance(keyFactory.getAlgorithm());  
	        keyAgree.init(priKey);  
	        keyAgree.doPhase(pubKey, true);  
		    secretKey=keyAgree.generateSecret("AES");  
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}  
        return secretKey.getEncoded();  
	}
	
	/**
	 * decrypt map byte with key and convert mapbyte to hashmap
	 * @param mapbyte
	 * @param key
	 * @return
	 */
	public static HashMap<String, byte[]> getDecryptedMap(byte[] mapbyte, byte[] key){
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		byte[] plaindata = null;
		plaindata = CryptoUtils.decryptByAES(mapbyte, key);
		map = CryptoUtils.mapFromByte(plaindata);
		return map;
	}
	
	/**
	 * decrypt map byte with key and convert mapbyte to hashmap
	 * @param mapbyte
	 * @param key
	 * @return
	 */
	public static HashMap<String, byte[]> getDecryptedMap(byte[] mapbyte, Key key){
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		byte[] plaindata = null;
		plaindata = CryptoUtils.decryptByAES(mapbyte, key);
		map = CryptoUtils.mapFromByte(plaindata);
		return map;
	}
	
	/**
	 * Hash with salt using SHA-256
	 * @param string
	 * @param salt
	 * @return hash String
	 */
	public static String getSaltHash(String string, String salt)
	{
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(string.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    
    /**
     * Convert string to hexByte 
     * @param string
     * @return byte
     */
    public static byte[] hexStringToBytes(String str) {  
        if (str == null || str.equals("")) {  
            return null;  
        }  
        str = str.toUpperCase();  
        int length = str.length() / 2;  
        char[] hexChars = str.toCharArray();  
        byte[] d = new byte[length];  
        for (int i = 0; i < length; i++) {  
            int pos = i * 2;  
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
        }  
        return d;  
    } 
    
    /** 
     * Convert char to byte 
     * @param char 
     * @return byte 
     */  
    public static byte charToByte(char c) {  
        return (byte) "0123456789ABCDEF".indexOf(c);  
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
     * input a hashmap and aeskey, return an encrypted byte array
     * @param map
     * @param key
     * @return
     */
	public static byte[] getEncryptedMap(HashMap<String, byte[]> map, Key key){
		byte[] inputdata = null;
		byte[] cipherdata = null;
		inputdata = CryptoUtils.mapToByte(map);
		cipherdata = CryptoUtils.encryptByAES(inputdata, key);
		return cipherdata;
	}
	
	/**
	 * Get message digest by SHA-256
	 * @param string
	 * @param salt
	 * @return hash String
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] getMD5(byte[] data){
		  MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          md.update(data);
          return md.digest();
	}
	
	/**
	 * Get message digest by SHA-256
	 * @param string
	 * @param salt
	 * @return hash String
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] getMD5(String data){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        md.update(data.getBytes());
        return md.digest();
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
	
	/**
	 * get a input string (decrypted) and Key, and return a map
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

	
	public static void main(String[] args) throws Exception {
		
		
		System.out.println(new String(getMD5("Hello")));
		System.out.println(new String(getMD5("Hello")));
		
		/*
		byte[] a = new byte[16];
		a = new String("ABCDEFG").getBytes();
		byte[] encrypted = CryptoUtils.encryptByRSAPublicKey(a, CryptoUtils.getPublicKey("public.der"));
		byte[] cipher = new byte[16];
		cipher = CryptoUtils.decryptByRSAPrivateKey(encrypted, CryptoUtils.getPrivateKey("private.der"));
		
		System.out.println("before"+ new String(a));
		System.out.println("after" + new String(cipher));
		String astr = new String(a, "UTF-8");
		String bstr = new String(cipher, "UTF-8");
		System.out.println("length" + a.length);
		System.out.println("length" + cipher.length);
		if(astr.equals(bstr)){
			System.out.println("True");
		}

		encrypted = CryptoUtils.encryptByRSAPublicKey("hello".getBytes(), CryptoUtils.getPublicKey("public.der"));
		cipher = CryptoUtils.decryptByRSAPrivateKey(encrypted, CryptoUtils.getPrivateKey("private.der"));
		
		System.out.println("length" + "hello".getBytes().length);
		System.out.println("length" + encrypted.length);
		System.out.println("length" + cipher.length);
		
		String str = new String(cipher, "UTF-8");
		if(str.equals("Hello")){
		System.out.println(new String(cipher, "UTF-8"));
		}
		*/
	}

}
