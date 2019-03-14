package api;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.util.*;

public class API {

    protected Object requestData(String url, Map<String,String> arguments ,Boolean POST) throws IOException, ParseException {
        return requestData(url,arguments,new HashMap<>(),POST);
    }

    protected Object requestData(String url, Map<String,String> headParameters) throws IOException, ParseException {
        return requestData(url,new HashMap<>(),headParameters,false);
    }

    protected Object requestData(String myurlurl, Map<String,String> arguments,Map<String,String> headParameters,Boolean POST) throws IOException, ParseException {

        if(!POST){ //get request
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet()) sj.add(entry.getKey()+ "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            myurlurl+="?"+sj;
        }

        URL url = new URL(myurlurl);
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
            http.setRequestMethod("POST"); // PUT is another valid option

            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet()) sj.add(entry.getKey()+ "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            OutputStreamWriter wr = new OutputStreamWriter(http.getOutputStream());
            System.out.println(sj.toString());
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
