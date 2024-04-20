package com.handson.tinyurl.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShortUrl {
    private String longUrl;
    private Integer totalClicks;
    private Map<String, Integer> clicks = new HashMap<>();

    public Map<String, Integer> getClicks() {
        return clicks;
    }

    public Integer getTotalClicks() {
        return totalClicks;
    }

    public void setClicks(Map<String, Integer> clicks) {
        this.clicks = clicks;
    }

    public void setTotalClicks(Integer totalClicks) {
        this.totalClicks = totalClicks;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ShortUrl shortUrl = (ShortUrl) o;
        return Objects.equals(clicks, shortUrl.clicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clicks);
    }

    @Override
    public String toString() {
        return "ShortUrl{" +
                "clicks=" + clicks +
                '}';
    }

}
