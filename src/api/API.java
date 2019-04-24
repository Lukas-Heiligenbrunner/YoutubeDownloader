package api;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.util.*;

public class API {

    /**
     * reqests api data
     * @param url basic url
     * @return returns json object
     * @throws IOException is thrown on no internet connection
     * @throws ParseException is thrown on wrong json data
     */
    protected Object requestData(String url) throws IOException, ParseException {
        return requestData(url,new HashMap<>(),new HashMap<>(),false);
    }

    /**
     * reqests api data
     * @param url basic url
     * @param arguments get/post parameters
     * @param POST false if get and true if post request method
     * @return returns json object
     * @throws IOException is thrown on no internet connection
     * @throws ParseException is thrown on wrong json data
     */
    protected Object requestData(String url, Map<String,String> arguments ,Boolean POST) throws IOException, ParseException {
        return requestData(url,arguments,new HashMap<>(),POST);
    }

    /**
     * reqests api data
     * @param url basic url
     * @param headParameters sets head parameters
     * @return returns json object
     * @throws IOException is thrown on no internet connection
     * @throws ParseException is thrown on wrong json data
     */
    protected Object requestData(String url, Map<String,String> headParameters) throws IOException, ParseException {
        return requestData(url,new HashMap<>(),headParameters,false);
    }

    /**
     * reqests api data
     * @param myurl basic url
     * @param arguments get/post arguments
     * @param headParameters
     * @param POST false if get and true if post request method
     * @return returns json object
     * @throws IOException is thrown on no internet connection
     * @throws ParseException is thrown on wrong json data
     */
    protected Object requestData(String myurl, Map<String,String> arguments,Map<String,String> headParameters,Boolean POST) throws IOException, ParseException {

        if(!POST){ //get request
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet()) sj.add(entry.getKey()+ "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            myurl+="?"+sj;
        }

        URL url = new URL(myurl);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setDoOutput(true);
        http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

        if (headParameters.size() > 0){
            for (Map.Entry<String,String> entry:headParameters.entrySet()) {
                http.addRequestProperty(entry.getKey(),entry.getValue());
            }
        }

        if (POST){
            http.setRequestMethod("POST");

            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet()) sj.add(entry.getKey()+ "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            OutputStreamWriter wr = new OutputStreamWriter(http.getOutputStream());
            wr.write(sj.toString());
            wr.flush();
            wr.close();
        }


        BufferedReader inreader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = inreader.readLine()) != null) {
            result.append(line);
        }
        inreader.close();
        JSONParser myparser = new JSONParser();
        return myparser.parse(result.toString());
    }

}
