
package com.johnkmartins.nopapernews.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.johnkmartins.nopapernews.R;
import com.johnkmartins.nopapernews.model.Feed;
import com.johnkmartins.nopapernews.model.FeedItem;
import com.johnkmartins.nopapernews.util.AppSharedPreferences;
import com.johnkmartins.nopapernews.util.Util;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebActivity extends AppCompatActivity {

    private final static String CONTENT_TYPE = "text/plain";
    private final static int MAX_PROGRESS = 100;
    private AppSharedPreferences sharedPreferences;
    private FeedItem feedItem;

    @BindView(R.id.web_view_news)
    WebView webView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        ButterKnife.bind(this);

        final Bundle bundle = getIntent().getExtras();
        feedItem = Parcels.unwrap(bundle.getParcelable(HomeActivity.BUNDLE_FEED_ITEM));

        sharedPreferences = new AppSharedPreferences(this);

        webView.getSettings().setJavaScriptEnabled(true);
        this.webView.setWebChromeClient(new CustomWebViewClient());
        this.webView.setWebViewClient(new WebViewClient());
        this.webView.loadUrl(feedItem.getLink());

        this.progressBar.setProgress(0);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            onBackSetResult();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        onBackSetResult();
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.web_menu, menu);

        MenuItem menuItem = menu.getItem(0);
        if(feedItem.isFavorite()) {
            menuItem.setIcon(R.drawable.ic_favorite_white);
            menuItem.setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                onBackSetResult();
                this.finish();
                return true;

            case R.id.action_share:
                shareUrl();
                return true;

            case R.id.action_open_browser:
                openInBrowser();
                return true;

            case R.id.action_favorite:
                checkFavoriteStatus(item);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class CustomWebViewClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    private void setValue(int progress) {
        this.progressBar.setProgress(progress);

        if(progress == MAX_PROGRESS) {
            this.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void shareUrl() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, feedItem.getLink());
        sendIntent.setType(CONTENT_TYPE);
        startActivity(sendIntent);
    }

    private void openInBrowser() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(feedItem.getLink()));
        startActivity(i);
    }

    private void onBackSetResult() {
        Intent intent = new Intent();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(HomeActivity.BUNDLE_FEED_ITEM, Parcels.wrap(feedItem));
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }

    private void checkFavoriteStatus(final MenuItem item){

        if(item.isChecked()){
            item.setChecked(false);
            item.setIcon(R.drawable.ic_favorite_border_white);
            feedItem.setFavorite(false);
            deleteFavorite();
            Toast.makeText(WebActivity.this, getString(R.string.removed_favorites), Toast.LENGTH_SHORT).show();

        }else{
            item.setChecked(true);
            item.setIcon(R.drawable.ic_favorite_white);
            feedItem.setFavorite(true);
            saveFavorite();
            Toast.makeText(WebActivity.this, getString(R.string.saved_favorites), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFavorite() {
        Feed favorites = sharedPreferences.getFavoritesFeed();

        if(favorites == null) {
            favorites = new Feed();
            favorites.setTitle(getString(R.string.favorites_title));
            favorites.setFavorites(true);

            List<FeedItem> feedItems = new ArrayList<>();
            feedItems.add(feedItem);
            favorites.setFeedItemList(feedItems);
        } else {
            if(favorites.getFeedItemList() == null) {
                List<FeedItem> feedItems = new ArrayList<>();
                feedItems.add(feedItem);
                favorites.setFeedItemList(feedItems);
            }else {
                favorites.getFeedItemList().add(feedItem);
            }
        }

        updateFavoriteList(favorites);
    }

    private void deleteFavorite() {
        Feed favorites = sharedPreferences.getFavoritesFeed();
        if(favorites != null) {
            Util.removeFeedItem(feedItem, favorites.getFeedItemList());
            updateFavoriteList(favorites);
        }
    }

    private void updateFavoriteList(Feed feedFavorite) {
        sharedPreferences.saveFeedFavorites(feedFavorite);
    }
}
