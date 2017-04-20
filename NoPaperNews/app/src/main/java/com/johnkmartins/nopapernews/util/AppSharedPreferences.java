package com.johnkmartins.nopapernews.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.johnkmartins.nopapernews.model.Feed;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Martins on 4/17/2017.
 */

public class AppSharedPreferences {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public AppSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("com.johnkmartins.nopapernews", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveFeedList(List<Feed> feedList) {
        Gson gson = new Gson();
        String json = gson.toJson(feedList);
        editor.putString("feed_list", json);
        editor.commit();
    }

    public List<Feed> getFeedList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("feed_list", "");
        Type listType = new TypeToken<ArrayList<Feed>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public void saveFeedFavorites(Feed favorites) {
        Gson gson = new Gson();
        String json = gson.toJson(favorites);
        editor.putString("feed_favorites", json);
        editor.commit();
    }

    public Feed getFavoritesFeed() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("feed_favorites", "");
        return gson.fromJson(json, Feed.class);
    }
}