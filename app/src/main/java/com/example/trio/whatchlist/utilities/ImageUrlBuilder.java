package com.example.trio.whatchlist.utilities;

/**
 * Created by ASUS on 27/08/2017.
 */

public class ImageUrlBuilder {
    public static final String IMG_URL = "http://image.tmdb.org/t/p/";

    public static String getPosterUrl(String path) {
        return IMG_URL + "w185" + path;
    }

    public static String getBackdropUrl(String path) {
        return IMG_URL + "w500" + path;
    }
}
