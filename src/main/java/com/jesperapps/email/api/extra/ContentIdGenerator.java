package com.jesperapps.email.api.extra;
import java.net.UnknownHostException;
import java.util.Random;
public class ContentIdGenerator {
	
	
	static int seq = 0;
	  static String hostname;
	  
	  static {
	    hostname = new Random(System.currentTimeMillis()).nextInt(100000) + "@jespersoft.com";
	  }
	  
	  /**
	   * Sequence goes from 0 to 100K, then starts up at 0 again.  This is large enough, 
	   * and saves
	   * @return
	   */
	  public static synchronized int getSeq() {
	    return (seq++) % 100000;
	  }
	  
	  /**
	   * One possible way to generate very-likely-unique content IDs.
	   * @return A content id that uses the hostname, the current time, and a sequence number
	   * to avoid collision.
	   */
	  public static String getContentId() {
	    int c = getSeq();
	    return c + "." + System.currentTimeMillis() + hostname;
	  }


	  public static void main(String[] args) throws InterruptedException {
	    for (int i = 0; i < 5; i++) {
	      for (int j = 0; j < 5; j++) {
	        System.out.println(ContentIdGenerator.getContentId());
	      }
	      Thread.sleep(100);
	    }
	  }

}
