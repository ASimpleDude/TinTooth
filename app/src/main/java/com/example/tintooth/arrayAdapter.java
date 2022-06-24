package com.example.tintooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class arrayAdapter extends ArrayAdapter<Card> {
     Context context;
     public arrayAdapter(Context context, int resourceId, List<Card> items){
         super(context, resourceId, items);
     }
     public View getView(int position, View convertView, ViewGroup parent){
         Card card_item = getItem(position);
         if(convertView == null){
             convertView = LayoutInflater.from(getContext()).inflate(R.layout.swipecard, parent, false);
         }
         TextView name = (TextView) convertView.findViewById(R.id.username);
         ImageView image = (ImageView) convertView.findViewById(R.id.image);
         name.setText(card_item.getName());
         image.setImageResource(R.mipmap.ic_launcher);
         return convertView;
      }
}
