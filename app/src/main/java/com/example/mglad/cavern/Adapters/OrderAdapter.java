package com.example.mglad.cavern.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mglad.cavern.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mglad on 11/25/2016.
 */

public class OrderAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<JSONObject> mDataSource;
    private static final HashMap<String, Integer> IMAGE_BG_COLOR = new HashMap<String, Integer>() {{
        put("PLACED", R.color.trackingPlaced);
        put("IN_PROGRESS", R.color.trackingInProgress);
        put("FINISHED", R.color.trackingFinished);
    }};

    private static final HashMap<String, Integer> IMAGE = new HashMap<String, Integer>() {{
        put("PLACED", R.drawable.ic_order_created);
        put("IN_PROGRESS", R.drawable.ic_order_in_progress);
        put("FINISHED", R.drawable.ic_order_finished);
    }};

    public OrderAdapter(Context context, ArrayList<JSONObject> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        JSONObject order = (JSONObject) getItem(position);
        View rowView = mInflater.inflate(R.layout.list_item_order, parent, false);
        TextView mainCourseText = (TextView) rowView.findViewById(R.id.tracking_main_course);
        TextView sidesText = (TextView) rowView.findViewById(R.id.tracking_sides);
        TextView beverageText = (TextView) rowView.findViewById(R.id.tracking_beverage);
        TextView numberText = (TextView) rowView.findViewById(R.id.tracking_number);
        ImageView trackingImage = (ImageView) rowView.findViewById(R.id.tracking_image);
        String status = "";

        try {
            mainCourseText.setText(order.getString("mainCourse") + " - " + order.getString("type"));
            sidesText.setText(order.getString("side1") + ", " + order.getString("side2"));
            beverageText.setText(order.getString("beverage"));
            numberText.setText("Order Number: " + order.getInt("id"));
            status = order.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        trackingImage.setBackgroundColor(ContextCompat.getColor(parent.getContext(), IMAGE_BG_COLOR.get(status)));
        trackingImage.setImageResource(IMAGE.get(status));
        return rowView;
    }

}