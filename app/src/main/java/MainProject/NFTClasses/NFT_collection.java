package MainProject.NFTClasses;


import MainProject.Graphics.MonitorThread;
import MainProject.Utils.JSONParser;
import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
/*
 *  This class is the rapresentation of a collection of NFT, 
 *  it contain the basics and also more important information about it.
 *  It also has a method to initialize it starting from its own name  
 */
public class NFT_collection{
    private String name=null;
    private String description;
    private String discord;
    private String twitter;
    private String website;
    private MonitorThread monitor;
    private ImageIcon collectionPic;

    public NFT_collection() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscord() {
        return discord;
    }

    public void setDiscord(String discord) {
        this.discord = discord;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public MonitorThread getMonitorThread() {
        return monitor;
    }

    public void setMonitorTread(MonitorThread monitor) {
        this.monitor = monitor;
    }

    public ImageIcon getCollectionPic() {
        return collectionPic;
    }

    public void setCollectionPic(String s) {
        ImageIcon icon;
        Image resizedIcon;
        URL url;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        icon = new ImageIcon(url);
        resizedIcon = icon.getImage().getScaledInstance(200, 200,Image.SCALE_DEFAULT);

        this.collectionPic = new ImageIcon(resizedIcon);
    }

    public NFT_collection(String name, String description, String discord, String twitter, String website) {
        this.name = name;
        this.description = description;
        this.discord = discord;
        this.twitter = twitter;
        this.website = website;
    }

    public boolean init(String collectionName) {
        // System.out.println(collectionName);
        GetRequest response = Unirest.get("https://api-mainnet.magiceden.io/collections/"+collectionName+"?edge_cache=true");
        if(response.asString().getStatusText().equals("OK")){
        String[] str = JSONParser.parseFromString(response.asString().getBody(),
               new String[]{"description","discord", "symbol", "twitter", "website", "image" });
            setDescription(str[0]);
            setDiscord(str[1]);
            setName(str[2]);
            setTwitter(str[3]);
            setWebsite(str[4]);
            setCollectionPic("https://img-cdn.magiceden.dev/rs:fill:320:320:0:0/plain/"+str[5]);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "NFT_collection:\n"
                +"Name: "+name
                +"\nDescription: "+description
                +"\nWebsite: "+website
                +"\nDiscord: "+discord
                +"\nTwitter: "+twitter+"\n";
    }
}
