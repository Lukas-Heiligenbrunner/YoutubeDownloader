package api;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class YoutubeToLinkmp3Music extends API{

    private String link = "";
    private String name = "";
    private String lengthsecs = ";";

    //https://youtubemp3music.info/@api/json/mp3/nD75vDVIn-Y

    public void getDirectLink(String id) throws IOException, ParseException {
        JSONObject request = (JSONObject) this.requestData("https://youtubemp3music.info/@api/json/mp3/"+id);
        link = "https:"+((String) ((JSONObject)((JSONObject)request.get("vidInfo")).get("0")).get("dloadUrl"));
        name = (String)request.get("vidTitle");
        System.out.println(link);
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
