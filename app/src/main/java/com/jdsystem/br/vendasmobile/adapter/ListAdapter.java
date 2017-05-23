package com.jdsystem.br.vendasmobile.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by eduardo.costa on 22/10/2016.
 */

public class ListAdapter extends SimpleAdapter {

    protected ListView mList;
    private int[] colors = new int[]{0x30fefeff, 0x30fefeff};//  0x30ffffff, 0x30808080 };

    public ListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        int colorPos = position % colors.length;
        view.setBackgroundColor(colors[colorPos]);
        return view;
    }


}
