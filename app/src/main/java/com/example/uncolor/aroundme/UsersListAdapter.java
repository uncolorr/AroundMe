package com.example.uncolor.aroundme;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by uncolor on 18.08.17.
 */

public class UsersListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<UserItem> userItems;
    com.nostra13.universalimageloader.core.ImageLoader normalImageLoader;
    private DisplayImageOptions options;

    public UsersListAdapter(Context context, ArrayList<UserItem> userItems) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.userItems = userItems;
        normalImageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    @Override
    public int getCount() {
        return userItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.user_item, parent, false);
            final ImageView imageViewUserAvatar = (ImageView) view.findViewById(R.id.imageViewUserAvatar);

            normalImageLoader.loadImage(userItems.get(position).getAvatar_url(), options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    Log.i("fg", "onLoadingStarted");
                    Log.i("fg", imageUri);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Log.i("fg", "onLoadingFailed");
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    Log.i("fg", "onLoadingComplete");
                    imageViewUserAvatar.setImageBitmap(loadedImage);
                    notifyDataSetChanged();
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    Log.i("fg", "onLoadingCancelled");
                }

            });
           // imageViewUserAvatar.setImageResource(R.drawable.bg);
            TextView textViewUserLogin = (TextView)view.findViewById(R.id.textViewUserLogin);
            textViewUserLogin.setText(userItems.get(position).getLogin());
        }

        return view;
    }
}
