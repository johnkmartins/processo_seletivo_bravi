package com.johnkmartins.nopapernews.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.johnkmartins.nopapernews.R;
import com.johnkmartins.nopapernews.model.Feed;

import java.util.List;

/**
 * Created by John Martins on 4/18/2017.
 */

public class ChannelListAdapter extends ArrayAdapter<Feed> {


    public ChannelListAdapter(final Context context, final List<Feed> feeds) {
        super(context, 0, feeds);
    }

    private class ViewHolder {
        TextView textViewTitle;
    }


    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup viewGroup) {

        View view = convertView;
        final ChannelListAdapter.ViewHolder viewHolder;

        if (view == null) {

            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.channel_list_item, viewGroup, false);
            viewHolder = new ChannelListAdapter.ViewHolder();
            viewHolder.textViewTitle = (TextView) view.findViewById(R.id.text_view_title);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ChannelListAdapter.ViewHolder) view.getTag();
        }

        final Feed feed = getItem(position);

        assert feed != null;
        viewHolder.textViewTitle.setText(feed.getTitle());
        return view;
    }
}
