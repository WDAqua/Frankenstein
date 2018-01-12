package testWrapper;

import java.awt.PageAttributes.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public class TestWrapper {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		final String CLIENT_ID  = "5e15e933";
		
		final String CLIENT_SECRET = "a09256c925adc9e2279435038df9d55e";
		
		String url="https://api.ambiverse.com/oauth/token";
		
		String question = "Who is the wife of Barak Obama ?";
		
		 try
	      {
			String[] getAccessTokenCmd = {"curl", "-X", "POST", "-H", 
				                         "Content-Type: application/x-www-form-urlencoded",
						                 "-d", "grant_type=client_credentials",
				                         "-d", "client_id=5e15e933",
				                         "-d", "client_secret=a09256c925adc9e2279435038df9d55e",
				                           url };
			
			
			ProcessBuilder process = new ProcessBuilder(getAccessTokenCmd);
			Process p = process.start();
			
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = null;
			
			System.out.println(b.readLine());
			while ((line = b.readLine()) != null) {
			  System.out.println(line);
			}
			
			
			
			int exitVal = p.waitFor();
	         System.out.println("Exited with error code " + exitVal);
	         
	         
	         
	         if (exitVal == 0) {
	        	   String access_token = "e8f0dd3d3ce6bea381dc12102f6cff0598421281";
	        	   String urlEntity = "https://api.ambiverse.com/v1/entitylinking/analyze";
	        	   String query = "{\n" + 
	        	   		"  \"docId\": \"ma-alibaba-investments\",\n" + 
	        	   		"  \"text\": \"Ma founded Alibaba in Hangzhou with investments from SoftBank and Goldman.\"\n" + 
	        	   		"}";
	        	 
	           
	           
	           try
	 	      {
	        	   
	        	    String[] accessTokenCmd = {"curl", "-X", "POST", "-H", "Content-Type: application/json", "-H", "Accept: application/json", "-H", 
	                          "Authorization:"+access_token,  "-d", query, urlEntity };
	 			
	 			ProcessBuilder process_token = new ProcessBuilder(accessTokenCmd);
	 			Process p_token = process_token.start();
	 			
	 			BufferedReader b_token = new BufferedReader(new InputStreamReader(p_token.getInputStream()));
	 			
	 			String line_token = null;
	 			
	 			System.out.println(b.readLine());
	 			while ((line_token = b_token.readLine()) != null) {
	 			 String text  = IOUtils.toString(line_token, StandardCharsets.UTF_8.name());
	 			  System.out.println(text);
	 			}
	 			
	 			int exitVal_token = p.waitFor();
	 	         System.out.println("Exited with error code " + exitVal_token);
	 	        b.close();
	        		 
	         }
	           
	           catch (Exception e)
	 	      {
	 	         System.out.println(e.toString());
	 	         e.printStackTrace();
	 	      }
	
			b.close();
	         }
	      }
		 
		 catch (Exception e)
	      {
	         System.out.println(e.toString());
	         e.printStackTrace();
	      }
		 
		 
		
		
		 
		 
		 /*OkHttpClient client = new OkHttpClient();

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "{\"text\" : \"Ma founded Alibaba in Hangzhou with investments from SoftBank and Goldman.\"}");
		Request request = new Request.Builder()
		  .url("https://api.ambiverse.com/v1/entitylinking/analyze")
		  .post(body)
		  .addHeader("content-type", "application/json")
		  .addHeader("accept", "application/json")
		  .addHeader("authorization", "Bearer ACCESS_TOKEN")
		  .build();

		Response response = client.newCall(request).execute();*/
		

	}

}
