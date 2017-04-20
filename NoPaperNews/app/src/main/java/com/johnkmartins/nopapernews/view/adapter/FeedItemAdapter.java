package com.johnkmartins.nopapernews.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.johnkmartins.nopapernews.R;
import com.johnkmartins.nopapernews.model.FeedItem;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by John Martins on 4/16/2017.
 */

public class FeedItemAdapter extends ArrayAdapter<FeedItem> {


    public FeedItemAdapter(final Context context, final List<FeedItem> feedItems) {
        super(context, 0, feedItems);
    }

    private class ViewHolder {
        TextView textViewTitle;
        TextView textViewPubDate;
        ImageView imageViewFeedItem;
        TextView textViewResume;
    }


    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup viewGroup) {

        View view = convertView;
        final ViewHolder viewHolder;

        if (view == null) {

            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_item, viewGroup, false);
            viewHolder = new ViewHolder();

            viewHolder.textViewTitle = (TextView) view.findViewById(R.id.text_view_title);
            viewHolder.textViewPubDate = (TextView) view.findViewById(R.id.text_view_pub_date);
            viewHolder.imageViewFeedItem = (ImageView) view.findViewById(R.id.image_view_feed_item);
            viewHolder.textViewResume = (TextView) view.findViewById(R.id.text_view_resume);

            view.setTag(viewHolder);

        }  else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final FeedItem feedItem = getItem(position);

        assert feedItem != null;
        viewHolder.textViewTitle.setText(feedItem.getTitle());
        viewHolder.textViewPubDate.setText(feedItem.getPubDate());
        viewHolder.textViewResume.setText(feedItem.getResume());
        Picasso.with(viewGroup.getContext()).load(feedItem.getImgSrc()).into(viewHolder.imageViewFeedItem);

        return view;
    }
}
