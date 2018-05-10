package com.bobtoong.companyinforake;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Owner on 2018-04-30.
 */

public class CustomChoiceListViewAdapter extends BaseAdapter {
    private static final String				TAG							= "CompanyInfo/CCLVAdapter";

//    private static ArrayList<String> listViewItemList = new ArrayList<>();
    private static HashMap<String, String> listViewItemMap = new LinkedHashMap<>();
    public CustomChoiceListViewAdapter(){

    }

    private String convertPos2Key(int position){
        if (position < 0 || position >= listViewItemMap.size()){
            Log.e(TAG, "convertPos2Key("+position +") is out of bound");
            return null;
        }

        // Data Set(listViewItemMap)에서 position에 위치한 데이터 참조 획득
        Set<String> set = listViewItemMap.keySet();
        Iterator<String> iter = set.iterator();

        String key = null, value = null;
        int i = 0;
        while (iter.hasNext()){
            key = (String)iter.next();
            if (i == position){
                Log.i(TAG, "convertPos2Key(" + position + "), " + key);
                return key;
            }
            i++;
        }
        Log.e(TAG, "convertPos2Key("+position +") has no matched key");
        return null;
    }

    @Override
    public int getCount() {
        return listViewItemMap.size();
    }

    @Override
    public Object getItem(int i) {
        return listViewItemMap.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            Log.i(TAG, "convertView is null!!");
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item_layout, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView textTextView = (TextView) convertView.findViewById(R.id.customListViewtextView) ;

        // 아이템 내 각 위젯에 데이터 반영
//        Log.i(TAG, listViewItem);
        String key = convertPos2Key(position);
        if (key == null){
            return null;
        }
        String value = listViewItemMap.get(key);
        textTextView.setText(key + ", " + value);

        return convertView;
    }

    public void addItem(String companyCode, String companyName){
        listViewItemMap.put(companyCode, companyName);
    }

    public void updaeItem(String companyCode, String companyName){
        listViewItemMap.put(companyCode, companyName);

    }

    public boolean deleteItem(String key){
        if (listViewItemMap.remove(key) == null){
            Log.e(TAG, "deleteItem(String " + key + ") can not delete item.");
            return false;
        }
        return true;
    }

    public String deleteItem(int position){
        Log.i(TAG, "deleteItem(int " + position + ")");
        String key = convertPos2Key(position);
        if (key == null){
            return null;
        }

        if(deleteItem(key) == true){
            return key;
        } else {
            return null;
        }
    }
}
