package com.johnkmartins.nopapernews.model;

import org.parceler.Parcel;

/**
 * Created by John Martins on 4/16/2017.
 */

@Parcel
public class FeedItem {

    private String title;
    private String resume;
    private String link;
    private String imgSrc;
    private String pubDate;
    private boolean favorite;

    public FeedItem() {
        this.favorite = false;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
