package com.example.trio.whatchlist.utilities;

/**
 * Created by ASUS on 29/08/2017.
 */

public class TrailerUtil {
    public static String getVideoThumbnailUrl(String key) {
        return "http://img.youtube.com/vi/" + key + "/0.jpg";
    }

    public static String getYoutubeUrl(String key) {
        return "http://www.youtube.com/watch?v=" + key;
    }
}
