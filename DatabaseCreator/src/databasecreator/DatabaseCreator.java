package databasecreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class DatabaseCreator {

    //////////////////////////
    // Insert Query         //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // insert into main 
    // values (type, url, main_title, episodes, start_date, end_date, description, rating, number_of_ratings, runtime, image);
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    final public static String PATH_TO_AT_XML = "C:\\Users\\Alexander\\Desktop\\Touya\\anime-titles.xml\\anime-titles.xml";
    
    //////////////////////////////////////////////
    // These types align with the table         //
    ////////////////////////////////////////////////////////
    public static String type;              //varchar(30)
    public static String url;               //varchar(70)
    public static String main_title;        //varchar(120
    public static int episodes;             //smallint
    public static String start_date;        //date
    public static String end_date;          //date
    public static String description;       //varchar(500)
    public static double rating;             //float
    public static int number_of_ratings;    //mediumint
    public static int runtime;              //smallint
    public static String image;             //varchar(70)
    public static int aid;
    ///////////////////////////////////////////////////////
    
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {//, Exception {
        ///////////////********************************************
        //This entire process should be done in a while loop, while
        //the anime-titles.xml file still has lines in it
        ///////////////********************************************
        File outFile = new File("anime.csv");
        outFile.createNewFile();
        ArrayList<Integer> aidList = new ArrayList<>();
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        writer.write("type|url|main_title|episodes|start_date|end_date|description|rating|number_of_ratings|runtime|image|aid");
        writer.newLine();
        
        ////////////////////////////////////////////////////////
        //Open the anime-titles.xml file and get the current aid
        //and create a loop that will be used to go through all
        //of the 
        //requirements: file reader
        ////////////////////////////////////////////////////////
        // Open XML file for raw parsing 
        BufferedReader br = new BufferedReader(new FileReader(PATH_TO_AT_XML));
        String line;
        
        while ((line = br.readLine()) != null) { 
            ///////////////////////////////////////////////////////////////////
            // BEGIN RAW PARSING
            ///////////////////////////////////////////////////////////////////
            String curAidStr="";
            // If the line begins with /t<anime then get its "aid"
            if(line.length()>6 && line.subSequence(0, 7).equals("\t<anime")) {
                // Get the value of the aid between the two quotes
                for(int sent=0, i=0; sent<2; i++) {
                    if(sent==1) curAidStr+=line.charAt(i);
                    if(line.charAt(i+1)=='"') sent++;
                }
                // Get rid of leading quote error
                curAidStr=curAidStr.substring(1, curAidStr.length());
                aid = Integer.parseInt(curAidStr);
                
                aidList.add(aid);
            }
            else
                continue;
            ///////////////////////////////////////////////////////////////////
            // END RAW PARSING
            ///////////////////////////////////////////////////////////////////
            
        }
        
        Collections.shuffle(aidList);
        
        for(int j=0; j<aidList.size(); j++) {
            
            type=null;              //varchar(30)
            url=null;               //varchar(70)
            main_title=null;        //varchar(120
            episodes=-1;             //smallint
            start_date=null;        //date
            end_date=null;          //date
            description=null;       //varchar(500)
            rating=-1;             //float
            number_of_ratings=-1;    //mediumint
            runtime=-1;              //smallint
            image=null;             //varchar(70)
            aid=-1;
            
            
            if(aidList.indexOf(j)==-1) continue;
            
            ////////////////////////////////////////////////////////
            //Send http request to anidb api and return xml file
            //requirements: http requester
            ////////////////////////////////////////////////////////
            String httpRequest = "http://api.anidb.net:9001/httpapi?client=touya&clientver=1&protover=1&request=anime&aid="+aidList.indexOf(j);
            //String httpRequest = "";
            try {
                String xmlResponse = sendGet(httpRequest);

                ////////////////////////////////////////////////////////
                //Convert the xml to json and gather data
                //requirements: json parser, xml to json converter
                ////////////////////////////////////////////////////////
                JSONObject xmlJSONObj = XML.toJSONObject(xmlResponse);

                System.out.println(xmlJSONObj);

                type = (String)xmlJSONObj.getJSONObject("anime").get("type");
                url = "https://anidb.net/anime/"+aidList.indexOf(j);

                // We only want to get the main title
                JSONArray tempJA = xmlJSONObj.getJSONObject("anime").getJSONObject("titles").getJSONArray("title");
                for(int i=0; i<tempJA.length(); i++) {
                    if(tempJA.getJSONObject(i).get("type").equals("main")) {
                        main_title = (String)tempJA.getJSONObject(i).get("content");
                    }
                }

                episodes = (int)xmlJSONObj.getJSONObject("anime").get("episodecount");
                start_date = (String)xmlJSONObj.getJSONObject("anime").get("startdate");
                end_date = (String)xmlJSONObj.getJSONObject("anime").get("enddate");
                description = (String)xmlJSONObj.getJSONObject("anime").get("description");

                if(description.contains("|"))
                    description.replaceAll("|", "::");

                rating = (double)xmlJSONObj.getJSONObject("anime").getJSONObject("ratings").getJSONObject("permanent").get("content");
                number_of_ratings = (int)xmlJSONObj.getJSONObject("anime").getJSONObject("ratings").getJSONObject("permanent").get("count");

                if(episodes>1) {
                    // We want to get the mode (most frequently occurring) episode air length
                    JSONArray episodeLengthJSONArray = xmlJSONObj.getJSONObject("anime").getJSONObject("episodes").getJSONArray("episode");
                    ArrayList<Integer> episodeLengthArray = new ArrayList<>();

                    for(int i=0; i<episodeLengthJSONArray.length(); i++) {
                        episodeLengthArray.add((int)episodeLengthJSONArray.getJSONObject(i).get("length"));
                    }

                    runtime = getMode(episodeLengthArray);
                }
                else {
                    // We want to get the mode (most frequently occurring) episode air length
                    JSONObject episodeLengthJSONObject = xmlJSONObj.getJSONObject("anime").getJSONObject("episodes").getJSONObject("episode");

                    runtime = (int)episodeLengthJSONObject.get("length");
                }

                // The image only has the end code, we really want to store the link for easy lookup
                image = "https://cdn-us.anidb.net/images/main/";
                image = image + (String)xmlJSONObj.getJSONObject("anime").get("picture");


                ////////////////////////////////////////////////////////
                //Add line to the CSV file that can be easily parsed to be
                //added to the mysql database
                ////////////////////////////////////////////////////////
                //writer.write("type,url,main_title,episodes,start_date,end_date,description,rating,number_of_ratings,runtime,image,aid");
                writer.write(type+"|"+url+"|"+main_title+"|"+episodes+"|"+start_date+"|"+end_date+"|"+description+"|"+rating+"|"+number_of_ratings+"|"+runtime+"|"+image+"|"+aidList.indexOf(j));
                writer.newLine();

                writer.flush();

                System.out.print("Adding Line: ");
                System.out.println(aidList.indexOf(j));

                ////////////////////////////////////////////////////////
                //Create a timer and a random number between 3000 and
                //5000 ms to pause for that much time
                //requirements: timer and rng
                ////////////////////////////////////////////////////////
                java.util.Random rn = new java.util.Random();
                int delay = rn.nextInt(3000)+7000;

                // Pause for between 7 and 10 seconds
                java.util.concurrent.TimeUnit.MILLISECONDS.sleep(delay);
            
            } catch (Exception e) {
                writer.write(type+"|"+url+"|"+main_title+"|"+episodes+"|"+start_date+"|"+end_date+"|"+description+"|"+rating+"|"+number_of_ratings+"|"+runtime+"|"+image+"|"+aid);
                writer.newLine();
            
                writer.flush();
            }
            
        }

        writer.close();
        br.close();
        
    }
    
    private static String sendGet(String url) throws Exception {
        HttpURLConnection httpClient =
                (HttpURLConnection) new URL(url).openConnection();

        // optional default is GET
        httpClient.setRequestMethod("GET");
        httpClient.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

        int responseCode = httpClient.getResponseCode();
        
        System.out.println(responseCode);
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(httpClient.getInputStream())))) {

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        }

    }
    
    public static int getMode(ArrayList<Integer> al) {
        // If the list is empty then just return -1
        if(al.size() <= 0) return -1;
        
        // Create a hashmap that will store <number that occurred, number of occurrences>
        HashMap<Integer, Integer> hm = new HashMap<>();
        
        // Add arraylist elements to hashmap with frequency as value
        for(Integer i : al) {
            if(hm.containsKey(i)) {
                int temp = hm.get(i);
                hm.replace(i, temp+1);
            }
            else {
                hm.put(i, 1);
            }
        }
        
        // Use these variables to find the most common number with the most occurrences
        int mostOccurences=0;
        int mostFrequentNum=-1;
        
        for(Map.Entry e : hm.entrySet())
        {
            if(((int)(e.getValue()))>mostOccurences) {
                mostOccurences = (int)e.getValue();
                mostFrequentNum = (int)e.getKey();
            }
        }
        
        return mostFrequentNum;
    }
    
}
