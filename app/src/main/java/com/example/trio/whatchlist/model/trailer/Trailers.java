package com.example.trio.whatchlist.model.trailer;

import java.util.List;

/**
 * Created by ASUS on 29/08/2017.
 */

public class Trailers {
    private int id;
    private List<Trailer> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Trailer> getResults() {
        return results;
    }

    public void setResults(List<Trailer> results) {
        this.results = results;
    }
}
