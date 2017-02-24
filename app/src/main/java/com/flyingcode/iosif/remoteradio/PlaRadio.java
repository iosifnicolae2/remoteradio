package com.flyingcode.iosif.remoteradio;

/**
 * Created by iosif on 11/29/15.
 */
public class PlaRadio {
    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUser(String user) {
        this.user = user;
    }

    private  String url;
    private  String title;
    private  String user;
    private  boolean isplaying = false;

    public PlaRadio(String url, String title, String user) {
        this.url = url;
        this.title = title;
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }

    public String toString() {

        return "{\"url\":\""+url+
                "\",\"title\":\""+title+
                "\",\"isPlaying\":\""+isplaying+
                "\",\"user\": \""+user+"\"}";
    }

    public boolean isplaying() {
        return isplaying;
    }

    public void setIsplaying(boolean isplaying) {
        this.isplaying = isplaying;
    }
}
