package com.example.adham.firebase1_authentication.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.adham.firebase1_authentication.R;
import com.example.adham.firebase1_authentication.models.Item;

import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter<Item> {

    public ItemAdapter(@NonNull Context context, ArrayList<Item> arrayList) {
        super( context, 0 ,arrayList);

    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View ListItem = convertView;
        if(ListItem==null)
        {

            ListItem = LayoutInflater.from( getContext() ).inflate( R.layout.custom_list_view,parent,false);
        }


        Item item = getItem( position );

        ImageView image = (ImageView) ListItem.findViewById( R.id.list_img );
        image.setImageResource(item.GetItemImage());

        TextView textView = (TextView)ListItem.findViewById( R.id.item_name );
        textView.setText( item.GetItemName() );


        return ListItem;





    }
}
