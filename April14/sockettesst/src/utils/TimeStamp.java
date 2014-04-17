package utils;

import java.util.HashMap;

public class TimeStamp {
	
	private String time1;
	private String time2;
	
	private HashMap<String, Integer> timemap1;
	private HashMap<String, Integer> timemap2;

	private String hour1;
	private int hournum1;
	private String minute1;
	private int minnum1;
	private String second1;
	private int secnum1;
	
	private String hour2;
	private int hournum2;
	private String minute2;
	private int minnum2;
	private String second2;
	private int secnum2;
	
	private static final String HOUR = "hour";
	private static final String MINUTE = "minute";
	private static final String SECOND = "second";

	public TimeStamp(){
		
	}
	
	public HashMap<String, Integer> splitTimeStamp(String time1){
		
		String[] strs = time1.split(" ");
		String date = strs[0];
		String time = strs[1];
		
		String[] hms = time.split(":");
		String hour = hms[0];
		String minute = hms[1];
		String second = hms[2];
		
		int hournum = Integer.parseInt(hour);
		int minnum = Integer.parseInt(minute);
		int secnum = Integer.parseInt(second);
		HashMap<String, Integer> timemap = new HashMap<String, Integer>();
		timemap.put(HOUR, hournum);
		timemap.put(MINUTE, minnum);
		timemap.put(SECOND, secnum);
		
		return timemap;
	}
	
	public int getDifference(String time1, String time2){
		int diff = 0;
		HashMap<String, Integer> timemap1 = new HashMap<String, Integer>();
		HashMap<String, Integer> timemap2 = new HashMap<String, Integer>();

		timemap1 = splitTimeStamp(time1);
		timemap2 = splitTimeStamp(time2);
		
		int hournum1 = timemap1.get(HOUR);
		int minnum1 = timemap1.get(MINUTE);
		int secnum1 = timemap1.get(SECOND);
		
		int hournum2 = timemap2.get(HOUR);
		int minnum2 = timemap2.get(MINUTE);
		int secnum2 = timemap2.get(SECOND);
		
		
		
		return diff;
	}
	
	public static void main(String args[]){
		Message message = new Message();
		
	}
}
