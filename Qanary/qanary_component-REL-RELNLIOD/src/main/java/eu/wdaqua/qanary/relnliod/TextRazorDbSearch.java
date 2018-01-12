package eu.wdaqua.qanary.relnliod;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextRazorDbSearch {
	//private static final Logger logger = LoggerFactory.getLogger(TextRazorDbSearch.class);
	private ArrayList<String> sentencesWord = new ArrayList<String>();
	private ArrayList<String> propertyList = new ArrayList<String>();
	private static ArrayList<String> filteredWordList = new ArrayList<String>();
	private HashSet<String> dbLinkListSet = new HashSet<String>();
	private boolean  relationsFlag = false;

	public ArrayList<String> createArrayWordsList(JSONArray jsonArraySent) {

		if (jsonArraySent.length() != 0) {
			for (int i = 0; i < jsonArraySent.length(); i++) {
				JSONArray jsonArrayWords = jsonArraySent.getJSONObject(i).getJSONArray("words");
				for (int j = 0; j < jsonArrayWords.length(); j++) {
					sentencesWord.add(jsonArrayWords.getJSONObject(j).getString("token"));
				}
			}
		} else {

			System.out.println("createArrayWordsList, Error: No Sentence to parse");
		}

		return sentencesWord;
	}

	public String getWordFromList(int i) {
		return sentencesWord.get(i);
	}
	
	public HashSet<String> getDbLinkListSet() {
		return dbLinkListSet;
	}
   
	public boolean getRelationsFlag() {
		return relationsFlag;
	}
	public void createPropertyList(JSONObject response1) {
        try {
		if (response1.has("relations")) {
			relationsFlag = true;
			JSONArray jsonArray = (JSONArray) response1.get("relations");
			System.out.println(":createPropertyListRelations: "+jsonArray.toString());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray wordPos = jsonArray.getJSONObject(i).getJSONArray("wordPositions");
				System.out.println("createPropertyList:wordPos: "+wordPos.toString());
				
				String str = "" ;
				for(int j = 0; j < wordPos.length(); j++) {
					str +=" "+getWordFromList(wordPos.getInt(j));
					
				}
				System.out.println("createPropertyList:propertyList "+i+" "+str);
				propertyList.add(str);
			}

		} else if (response1.has("properties")) {
			JSONArray jsonArray = (JSONArray) response1.get("properties");
			System.out.println("createPropertyList:Properties: "+jsonArray.toString());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray wordPos = jsonArray.getJSONObject(i).getJSONArray("wordPositions");
				System.out.println("createPropertyList:wordPos: "+wordPos.toString());
				String str ="" ;
				for(int j = 0; j < wordPos.length(); j++){
					
					str = str+" "+ getWordFromList(wordPos.getInt(j));
					
				}
				System.out.println("createPropertyList:propertyList "+i+" "+str);
				propertyList.add(str);
				
			}
		}}
        catch(Exception e) {
        	System.out.println("createPropertyList:createPropertyList"+e);
        }
		
		System.out.println("createPropertyList:"+propertyList.toString());

	}
	
	public void createRePropertyList(JSONObject response1) {
		propertyList.clear();
		try {
		if (response1.has("properties")) {
			JSONArray jsonArray = (JSONArray) response1.get("properties");
			System.out.println("createRePropertyList:Properties: "+jsonArray.toString());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray wordPos = jsonArray.getJSONObject(i).getJSONArray("wordPositions");
				System.out.println("createRePropertyList:wordPos: "+wordPos.toString());
				String str = "";
				for(int j = 0; j < wordPos.length(); j++){
					
					str = str+" "+ getWordFromList(wordPos.getInt(j));
				}
				System.out.println("Error:createRePropertyList:propertyList "+i+" "+str);
				propertyList.add(str);
				
			}
		}
		
		else {
			
			System.out.println("Error:createRePropertyList No propertyList");
		}
	}
	catch(Exception e) {
    	System.out.println("createRePropertyList "+e);
    }
		System.out.println("createPropertyList: "+propertyList.toString());
	}

	public static String searchDbLinkInTTL(String myKey) {
		
		String dbpediaProperty = null;
		try {
		String myKey1 = myKey.trim();
		if(myKey1!=null && !myKey1.equals("")) {
		System.out.println("searchDbLinkInTTL: "+myKey1);
			for (Entry<String, String> e : DbpediaRecorodProperty.get().tailMap(myKey1).entrySet()) {
				    if(e.getKey().contains(myKey1)) 
				    {
				     	dbpediaProperty = e.getValue();
				      	break;
				    }
					ArrayList<String> strArrayList = new ArrayList<String>(Arrays.asList(e.getKey().split("\\s+")));
					//System.out.println(strArrayList.toString());
					for (String s : strArrayList)
					{
					    if(myKey1.compareTo(s) == 0) {
					    	dbpediaProperty = e.getValue();
					 }
					}
					 
					 if(dbpediaProperty!=null)
					 break;
					    
					 }
         
		}
		} catch (Exception e) {
			// logger.info("Except: {}", e);
			// TODO Auto-generated catch block
		}
		System.out.println("searchDbLinkInTTL: "+dbpediaProperty);
		return dbpediaProperty;
	}

	public static String RemoveSubstring(String str) {
		String listString = null;
		//System.out.println("RemoveSubstring Start: "+ str);
		ArrayList<String> strArrayList = new ArrayList<String>(Arrays.asList(str.split("\\s+")));
		for (String s : filteredWordList) {
		     
			strArrayList.remove(s);
		}
		
		if(!strArrayList.isEmpty()) {
		for (String s : strArrayList)
		{   
			if(listString!=null) {
		    listString += s + " ";
		    }
		else {
			listString = s+ " ";
		}
		    
		}
		}

		System.out.println("RemoveSubstring End: "+listString.trim());
		return listString.trim();
	}
	
	public static void createFilteredWordList() {
		System.out.println("createFilteredWordList():");
		try {
		File filename = new File("src/main/resources/removal_list.txt");
		//File filename1 = new File("qanary_component-REL-RELNLIOD/src/main/resources/removal_list.txt");
		System.out.println(filename.getPath());
		System.out.println(filename.getAbsolutePath());
		Scanner in = new Scanner(new FileReader(filename.getAbsolutePath()));
		while(in.hasNext()) {
			filteredWordList.add((in.next().toString().trim()));
		}
		in.close();
		
		System.out.println(filteredWordList.toString());
		
		}
		catch(Exception e) {
			System.out.println("createFilteredWordList: Exception "+ e.toString());
		}
		
	}
	public void createDbLinkListSet(ArrayList<String> arrayListWords) {
		
		if (!arrayListWords.isEmpty()) {

			if (!propertyList.isEmpty()) {

				for (int i = 0; i < propertyList.size(); i++) {
					String str = TextRazorDbSearch.searchDbLinkInTTL(propertyList.get(i));
					if ( str!= null) 
						dbLinkListSet.add(str);

					else {
						String str1 = TextRazorDbSearch.searchDbLinkInTTL(RemoveSubstring(propertyList.get(i)));
                         	if(str1!= null)
							dbLinkListSet.add(str1);
					 }
				}
			}
			else {
				System.out.print("createDbLinkListSet:Info: No property in sentences");
			}

		} else {
			System.out.print("createDbLinkListSet:Error: No words in sentences");
		}
		
	}
}