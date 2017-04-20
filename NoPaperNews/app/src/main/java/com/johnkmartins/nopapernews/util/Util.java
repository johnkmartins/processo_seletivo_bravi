package com.johnkmartins.nopapernews.util;

import android.text.Html;
import android.util.Log;
import android.util.Xml;

import com.johnkmartins.nopapernews.model.Feed;
import com.johnkmartins.nopapernews.model.FeedItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Martins on 4/17/2017.
 */

public class Util {

    public static boolean exists(Feed feed, List<Feed> feeds) {

        for (Feed f : feeds) {
            if(f.getLink().equalsIgnoreCase(feed.getLink())) {
                return true;
            }
        }
        return false;
    }

    public static List<FeedItem> removeFeedItem(FeedItem feedItem, List<FeedItem> feedItems){
        for(FeedItem item : feedItems) {
            if(item.getLink().equalsIgnoreCase(feedItem.getLink())) {
                feedItems.remove(item);
                break;
            }
        }
        return feedItems;
    }

    public static void replaceFeedItem(List<FeedItem> feedItems, FeedItem feedItem) {

        for(FeedItem item : feedItems) {
            if(item.getLink().equalsIgnoreCase(feedItem.getLink())) {
                feedItems.set(feedItems.indexOf(item), feedItem);
            }
        }
    }

    @SuppressWarnings({"ConstantConditions", "ThrowFromFinallyBlock", "deprecation"})
    public static Feed parseFeed(final InputStream inputStream) throws XmlPullParserException,
            IOException {

        String title = null;
        String link = null;
        String description = null;
        String pubDate = null;
        boolean isItem = false;
        boolean isChannel = false;
        Feed feed = new Feed();
        List<FeedItem> feedItems = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null) {
                    continue;
                }

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase(FeedConstants.ITEM)) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase(FeedConstants.ITEM)) {
                        isItem = true;
                        isChannel = false;
                        continue;
                    } else if(name.equalsIgnoreCase((FeedConstants.CHANNEL))) {
                        isChannel = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase(FeedConstants.TITLE)) {
                    title = result;
                    if(isChannel) {
                        feed.setTitle(title);
                    }
                } else if (name.equalsIgnoreCase(FeedConstants.LINK)) {
                    link = result;
                    if(isChannel) {
                        feed.setLink(link);
                    }
                } else if (name.equalsIgnoreCase(FeedConstants.DESCRIPTION)) {
                    description = result;
                    if(isChannel) {
                        feed.setDescription(description);
                    }
                } else if (name.equalsIgnoreCase(FeedConstants.PUB_DATE)) {
                    pubDate = result;
                }

                if (title != null && link != null && description != null && pubDate != null) {

                    if(isItem) {
                        FeedItem item = new FeedItem();
                        item.setLink(link);
                        String resume = Html.fromHtml(description).toString();
                        item.setResume(resume.replace("￼", ""));
                        item.setTitle(title);
                        Document document = Jsoup.parse(description);
                        String imgSrc = document.select(FeedConstants.IMAGE).first().attr(FeedConstants.SOURCE);
                        item.setImgSrc(imgSrc);
                        item.setPubDate(pubDate);
                        item.setFavorite(false
                        );
                        feedItems.add(item);
                    }

                    title = null;
                    link = null;
                    description = null;
                    pubDate = null;
                    isItem = false;
                }
            }

            feed.setFeedItemList(feedItems);
            return feed;

        } finally {
            inputStream.close();
        }
    }
}
