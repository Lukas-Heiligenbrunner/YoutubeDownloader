package api;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YoutubeToLink extends API{

    private String link = "";
    private String name = "";
    private String lengthsecs = ";";

    // http://www.convertmp3.io/fetch/?format=JSON&video=https://www.youtube.com/watch?v=i62Zjga8JOM

    /**
     * exchanges youtube video id to direct download link
     * @param id youtube video id of video
     * @throws IOException thrown if there is no internet connection
     * @throws ParseException thrown if received data is not in json format
     */
    public void getDirectLink(String id) throws IOException, ParseException {

        Map<String,String> mymap = new HashMap<>();
        mymap.put("format","JSON");
        mymap.put("video","https://www.youtube.com/watch?v="+id);

        JSONObject request = (JSONObject) this.requestData("http://www.convertmp3.io/fetch/",mymap,false);

        link = ((String)request.get("link"));
        System.out.println(link);
        name = ((String)request.get("title"));
        lengthsecs = ((String)request.get("length"));
    }

    /**
     * get the downloadLink
     * @return the download link
     */
    public String getLink() {
        return link;
    }

    /**
     * get the file name
     * @return the name of the music file
     */
    public String getName() {
        return name;
    }

    /**
     * get the length of the video
     * @return length in seconds
     */
    public String getLengthsecs() {
        return lengthsecs;
    }
}
