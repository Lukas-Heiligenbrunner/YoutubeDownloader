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

    // https://you-link.herokuapp.com/?url=https://www.youtube.com/watch?v=Ns6mceo9DZs //--> didnt work ...
    // http://www.convertmp3.io/fetch/?format=JSON&video=https://www.youtube.com/watch?v=i62Zjga8JOM

    public YoutubeToLink() {


    }

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

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public String getLengthsecs() {
        return lengthsecs;
    }
}
