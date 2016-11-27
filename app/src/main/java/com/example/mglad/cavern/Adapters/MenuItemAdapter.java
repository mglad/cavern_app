package com.example.mglad.cavern.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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

public class MenuItemAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<JSONObject> mDataSource;

    public MenuItemAdapter(Context context, ArrayList<JSONObject> items) {
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
        JSONObject item = (JSONObject) getItem(position);
        View rowView = mInflater.inflate(R.layout.list_item_menu, parent, false);
        TextView menuItemText = (TextView) rowView.findViewById(R.id.menu_item_text);
        TextView menuTypeText = (TextView) rowView.findViewById(R.id.menu_type_text);
        TextView menuCategoryText = (TextView) rowView.findViewById(R.id.menu_category_text);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.menu_item_checkbox);
        try {
            menuItemText.setText(item.getString("name"));
            if(item.has("type")) {
                menuTypeText.setText(item.getString("type"));
            }
            menuCategoryText.setText(item.getString("category"));
            checkBox.setChecked(item.getBoolean("available"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;
    }

}