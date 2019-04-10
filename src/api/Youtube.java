package api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Youtube extends API{
    private static final String key="AIzaSyAYIXX7lgATXN2xPSCIK71wNQjgUzmYL0s";
    // https://www.googleapis.com/youtube/v3/search?type=video&q=surfing&maxResults=25&part=snippet&key=AIzaSyDFOuo3jbhaYqgzfOWDQxSMnTP0SuJLZjM

    public Youtube() {

    }

    /**
     * get id of first result of youtube search
     * @param keyword songname to search on youtube
     * @return string with id of video
     * @throws IOException thrown if there is no internet connection
     */
    public String firstResultID(String keyword) throws IOException{
        return (String)((JSONObject)((JSONObject)((JSONArray) searchYoutube(keyword).get("items")).get(0)).get("id")).get("videoId");
    }

    /**
     * search on youtube for specific keyword
     * @param keyword search query
     * @return object with all serach results
     * @throws IOException thrown if there is no internet connection
     */
    public JSONObject searchYoutube(String keyword) throws IOException{
        JSONObject data = null;
        try {
            Map<String,String> mymap = new HashMap<>();
            mymap.put("q",keyword);
            mymap.put("type","video");
            mymap.put("maxResults","25");
            mymap.put("part","snippet");
            mymap.put("key",key);

            data = (JSONObject) this.requestData("https://www.googleapis.com/youtube/v3/search",mymap,false);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return data;
    }


}
