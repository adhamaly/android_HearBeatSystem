package com.example.adham.firebase1_authentication.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.adham.firebase1_authentication.Adapter.ItemAdapter;
import com.example.adham.firebase1_authentication.R;
import com.example.adham.firebase1_authentication.models.Item;

import java.util.ArrayList;


public class ItemList extends AppCompatActivity {

    ItemAdapter itemAdapter;
    ListView listView;
    ArrayList<Item> arrayList;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_item_list );
        // save the links of maps..

        arrayList = new ArrayList<>(  );
        arrayList.add( new Item( "nearest Hospitals",R.drawable.hospital ) );
        arrayList.add( new Item( "nearest Cardiac centers",R.drawable.cardiac ) );
        arrayList.add( new Item( "nearest Gym",R.drawable.gym ) );
        itemAdapter =new ItemAdapter( this,arrayList);
        listView=(ListView)findViewById(R.id.list);
        listView.setAdapter( itemAdapter );
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                if(position == 0) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=hospitals");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
                else if(position == 1) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=cardiac hospitals");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
                else if(position == 2) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=Gym");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                }



            }
        });



    }
}
