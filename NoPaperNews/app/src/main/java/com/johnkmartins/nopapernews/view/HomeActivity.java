package com.johnkmartins.nopapernews.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.johnkmartins.nopapernews.R;
import com.johnkmartins.nopapernews.model.Feed;
import com.johnkmartins.nopapernews.model.FeedItem;
import com.johnkmartins.nopapernews.util.AppSharedPreferences;
import com.johnkmartins.nopapernews.util.FeedConstants;
import com.johnkmartins.nopapernews.util.NetworkUtility;
import com.johnkmartins.nopapernews.util.Util;
import com.johnkmartins.nopapernews.view.adapter.ChannelListAdapter;
import com.johnkmartins.nopapernews.view.adapter.FeedItemAdapter;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.parceler.Parcels;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("deprecation")
public class HomeActivity extends AppCompatActivity{

    public static final String BUNDLE_FEED_ITEM = "bundle_feed_item";
    private static final int RESULT_CODE_FEED_ITEM = 3;
    private static final int RESULT_CODE_FEED_FAVORITE = 4;

    private Feed selectedFeed = new Feed();
    private List<Feed> feedList = new ArrayList<>();
    private AppSharedPreferences sharedPreferences;
    private EditText editTextUrlDialog;
    private int feedToRemoveIndex = -1;
    private GoogleApiClient client;

    @BindView(R.id.list_view_news)
    ListView listViewNews;

    @BindView(R.id.text_view_feed)
    TextView textViewFeed;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.list_view_channels)
    ListView listViewChannels;

    @BindView(R.id.relative_layout_something_wrong)
    RelativeLayout relativeLayoutError;

    @BindView(R.id.relative_layout_no_favorites)
    RelativeLayout relativeLayoutNoFavorites;

    @BindView(R.id.relative_layout_no_feeds)
    RelativeLayout relativeLayoutNoFeed;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        sharedPreferences = new AppSharedPreferences(this);

        //PULL REFRESH
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });

        loadFeeds();

        //LIST OF CHANNELS ON DRAWER MENU
        listViewChannels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long l) {

                getFeed(feedList.get(position).getFeedUrl());
                drawer.closeDrawers();
            }
        });

        //LIST OF NEWS
        listViewNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, long l) {

                final FeedItem feedItem = selectedFeed.getFeedItemList().get(position);
                startActivityWebForResult(feedItem);
            }
        });

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        //Verification if the WebActivity is coming back from a normal feed visualization or from a favorite feed.
        if (requestCode == RESULT_CODE_FEED_ITEM) {
            if(resultCode == Activity.RESULT_OK){
                final Bundle bundle = data.getExtras();
                final FeedItem feedItem = Parcels.unwrap(bundle.getParcelable(HomeActivity.BUNDLE_FEED_ITEM));
                Util.replaceFeedItem(selectedFeed.getFeedItemList(), feedItem);
            }
        }else if(requestCode == RESULT_CODE_FEED_FAVORITE) {
            if(resultCode == Activity.RESULT_OK){
                callFavorites();
            }
        }
    }

    @OnClick(R.id.button_favorites)
    public void onFavoritesClick() {
        drawer.closeDrawers();
        callFavorites();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_rss:
                showDialogAddFeed();
                return true;

            case R.id.action_remove_rss:
                showDialogRemoveFeed();
                return true;

            case R.id.action_refresh:
                refreshFeed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Load feed channels from shared preferences, if they exists. If is the first time open the app, a default channel is loaded.
    private void loadFeeds() {
        final List<Feed> feeds = sharedPreferences.getFeedList();

        if (feeds == null) {
            getFeed(FeedConstants.DEFAULT_CHANNEL);
        } else {
            if(feeds.isEmpty()){
                relativeLayoutNoFeed.setVisibility(View.VISIBLE);
            }else {
                this.feedList = feeds;
                updateFeedList();
                getFeed(feeds.get(0).getFeedUrl());
                relativeLayoutNoFeed.setVisibility(View.GONE);
            }
        }
    }

    private void updateFeedList() {
        sharedPreferences.saveFeedList(feedList);
        listViewChannels.setAdapter(new ChannelListAdapter(HomeActivity.this, feedList));
    }

    //Here is done the refreshing of the feed, if the selected feed is a favorite feed the animation
    // of the pull refresh is not showed, because the favorites feeds are save on disk.
    private void refreshFeed() {
        if(selectedFeed == null || selectedFeed.isFavorites()) {
            refreshLayout.setRefreshing(false);
        }else {
            if (selectedFeed.getFeedUrl() == null) {
                if (feedList.isEmpty()) {
                    getFeed(FeedConstants.DEFAULT_CHANNEL);
                } else {
                    getFeed(feedList.get(0).getFeedUrl());
                }
            } else {
                getFeed(selectedFeed.getFeedUrl());
            }
        }
    }

    //Here we call the start activity passing the FeedItem to WebActivity
    private void startActivityWebForResult(final FeedItem feedItem) {

        final Intent i = new Intent(HomeActivity.this, WebActivity.class);
        final Bundle bundle = new Bundle();

        bundle.putParcelable(BUNDLE_FEED_ITEM, Parcels.wrap(feedItem));
        i.putExtras(bundle);
        if(selectedFeed.isFavorites()) {
            startActivityForResult(i, RESULT_CODE_FEED_FAVORITE);
        }else {
            startActivityForResult(i, RESULT_CODE_FEED_ITEM);
        }
    }

    private void getFeed(String urlLink) {

        if(NetworkUtility.isConnected(HomeActivity.this)) {
            refreshLayout.setRefreshing(true);
            relativeLayoutError.setVisibility(View.GONE);

            if (!urlLink.startsWith("http://") && !urlLink.startsWith("https://")) {
                urlLink = "http://" + urlLink;
            }
            connectToUrl(urlLink);

        }else {
            relativeLayoutError.setVisibility(View.VISIBLE);
        }
    }

    private void connectToUrl(final String urlLink) {
        Ion.with(this)
                .load(urlLink)
                .asInputStream()
                .setCallback(new FutureCallback<InputStream>() {

                    @Override
                    public void onCompleted(Exception e, InputStream result)  {
                        refreshLayout.setRefreshing(false);

                        if (result == null) {
                            Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        setResult(result, urlLink);
                    }
                });

    }

    public void setResult(final InputStream result, final String urlLink) {
        try {
            selectedFeed = Util.parseFeed(result);
            selectedFeed.setFeedUrl(urlLink);

            if (!Util.exists(selectedFeed, feedList)) {
                feedList.add(selectedFeed);
                updateFeedList();
            }

            listViewNews.setAdapter(new FeedItemAdapter(HomeActivity.this, selectedFeed.getFeedItemList()));
            relativeLayoutNoFavorites.setVisibility(View.GONE);
            relativeLayoutNoFeed.setVisibility(View.GONE);
            textViewFeed.setText(selectedFeed.getTitle());

        } catch (XmlPullParserException | IOException e1) {
            e1.printStackTrace();
            Toast.makeText(HomeActivity.this, getString(R.string.enter_valid_url), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void showDialogAddFeed() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_add_rss, true)
                .positiveText(R.string.save)
                .positiveColorRes(R.color.colorPrimaryDark)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.colorPrimaryDarker)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        getFeed(editTextUrlDialog.getText().toString());
                    }
                })
                .build();

        editTextUrlDialog = (EditText) dialog.getCustomView().findViewById(R.id.edit_text_url);

        dialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    private void showDialogRemoveFeed() {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_remove_rss, false)
                .positiveText(R.string.remove)
                .positiveColorRes(R.color.colorPrimaryDark)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.colorPrimaryDarker)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (feedToRemoveIndex != -1) {
                            feedList.remove(feedToRemoveIndex);
                            updateFeedList();
                            loadFeeds();
                        }
                    }
                })
                .build();

        ListView listView = (ListView) dialog.getCustomView().findViewById(R.id.list_view_channels_dialog);
        listView.setAdapter(new ChannelListAdapter(this, feedList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                view.setSelected(true);
                feedToRemoveIndex = position;
            }
        });

        dialog.show();

    }

    private void callFavorites() {

        Feed favorites = sharedPreferences.getFavoritesFeed();
        selectedFeed = favorites;
        relativeLayoutNoFeed.setVisibility(View.GONE);

        if(favorites == null || favorites.getFeedItemList() == null || favorites.getFeedItemList().size() == 0){
            relativeLayoutNoFavorites.setVisibility(View.VISIBLE);
        }else {
            listViewNews.setAdapter(new FeedItemAdapter(HomeActivity.this, favorites.getFeedItemList()));
            textViewFeed.setText(favorites.getTitle());
            relativeLayoutNoFavorites.setVisibility(View.GONE);
        }
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Home Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}
