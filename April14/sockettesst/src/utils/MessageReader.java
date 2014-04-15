package utils;

import java.io.BufferedReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MessageReader {
	
	public MessageReader(){
		
	}
	
	/**
	 * convert json to message
	 * @param inputstring
	 * @return message
	 */
	public static Message messageFromJson(String inputstring) {
		Message msg = new Message();
		Gson gson = new Gson();
		msg = gson.fromJson(inputstring, new TypeToken<Message>() {}.getType());
//		System.out.println("message is converted from json");
		return msg;
	}
	
	/**
	 * convert message to json
	 * add "eof" as finishing symbol
	 * @param message
	 * @return string
	 */
	public static String messageToJson(Message msg) {
		 String str;
		 Gson gson = new Gson();
		 str = gson.toJson(msg);
//		 str = str + "eof";
		 return str;
	}
	
	/**
	 * read data from inputStream
	 * @param socket
	 * @return inputstream in String
	 * @throws IOException
	 */
	public static String readInputStream(BufferedReader br) throws IOException{
        StringBuilder sb = new StringBuilder();  
        String temp;  
        int index;  
        while ((temp=br.readLine()) != null) {  
  		  //read inputstream until meat end symbol "eof"
           if ((index = temp.indexOf("eof")) != -1) {
            sb.append(temp.substring(0, index));  
               break;  
           }  
           sb.append(temp);  
        }
//        System.out.println("INPUT STREAM is (without eof) "+ sb);  
        return sb.toString();
	}
	
	public static Message getMessageFromStream(BufferedReader br) throws IOException{
		String str;
		str = MessageReader.readInputStream(br);
		Message msg = new Message();
		msg = MessageReader.messageFromJson(str);
	    return msg;
	}
}
