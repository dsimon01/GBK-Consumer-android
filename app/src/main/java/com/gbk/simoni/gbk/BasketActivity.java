package com.gbk.simoni.gbk;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class BasketActivity extends AppCompatActivity {

    Toolbar toolbar;
    ProgressDialog dialog;
    AlertDialog alertDialog;
    AlertDialog.Builder builder;
    ImageView bin;
    TextView totalPrice, itemNumberSummary;
    RecyclerView basketRecyclerView;
    String json;
    static int orderNumber;
    static List<Items> orderItems;
    ArrayList<String> itemNamesList , itemDescriptionList;
    ArrayList<Double> itemPriceList;
    ArrayList<Integer> itemImageList;
    Items item = new Items();
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basket_activity);

        // Call to method which finds text views and assigns them a value to display.
        orderSummary();

        // Call to method that arranges the display of a toolbar
        // Calls internally a dialog function when the bin icon within the toolbar is clicked.
        setToolbar();

        // Call to method that arranges the user's items so far in a recycler view.
        setRecycler();

        // The following method obtains an array list of objects and retrieves each object's
        // attributes.
        retrieveObjectData();

    }

    public void orderSummary(){

        itemNumberSummary = findViewById(R.id.itemNumberSummary);
        totalPrice = findViewById(R.id.totalPrice);
        totalPrice.setText((String.format(Locale.ENGLISH, "£%.2f", MenuActivity.totalPrice)));
        itemNumberSummary.setText(Integer.toString(MenuActivity.selectedItemsList.size()));

    }

    public void setToolbar(){

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bin = findViewById(R.id.binImage);

        bin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBinDialog();
            }
        });

    }

    public void setRecycler(){

        basketRecyclerView = findViewById(R.id.recyclerViewBasket);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        basketRecyclerView.setLayoutManager(linearLayoutManager);
        basketRecyclerView.setHasFixedSize(true);
        basketRecyclerView.setAdapter(new BasketAdapter(MenuActivity.selectedItemsList));
    }

    public void retrieveObjectData(){

        createArrayLists();

        json = gson.toJson(MenuActivity.selectedItemsList);
        JSONArray jsonarray = null;

        try {

            jsonarray = new JSONArray(json);

        } catch (JSONException e) {

            e.printStackTrace();
        }

        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = null;
            try {

                jsonobject = jsonarray.getJSONObject(i);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            double price = 0;
            String name = "";
            String description = "";
            int image = 0;

            try {

                price = jsonobject.getDouble("price");
                name = jsonobject.getString("itemName");
                description = jsonobject.getString("itemDescription");
                image = jsonobject.getInt("itemImage");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            item.itemName = name;
            itemNamesList.add(item.itemName);
            item.price = price;
            itemPriceList.add(item.price);
            item.itemDescription = description;
            itemDescriptionList.add(item.itemDescription);
            item.itemImage = image;
            itemImageList.add(item.itemImage);
            orderItems.add(item);
        }
    }

    public void createArrayLists(){

        orderItems = new ArrayList<>();
        itemNamesList = new ArrayList<>();
        itemPriceList = new ArrayList<>();
        itemImageList = new ArrayList<>();
        itemDescriptionList = new ArrayList<>();

    }

    public void onPlaceOrderClick(View view){

        dialog = new ProgressDialog(BasketActivity.this);
        dialog.setTitle("Processing your order");
        dialog.setMessage("Please wait...");

        dialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                System.out.println("PLACED ORDER");
                orderNumber = new Random().nextInt(9000) + 1000; // [0,8999] + 1000 => [1000, 9999]
                ParseObject order = new ParseObject("Order");
                order.put("TableNumber", ParseUser.getCurrentUser().getUsername());
                order.put("OrderID", orderNumber);
                order.put("Status", "new");
                order.put("Item", itemNamesList);
                order.put("Description", itemDescriptionList);
                order.put("Image", itemImageList);
                order.put("Price", itemPriceList);
                order.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null){
                            Toast.makeText(BasketActivity.this, "Order is now COOKING", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), OrderUpdatesActivity.class);
                            startActivity(intent);
                        }else {
                            e.printStackTrace();
                        }
                    }
                });
                order.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException ex) {
                        if (ex == null) {
                            Log.i("Parse Result", "Successful!");
                        } else {
                            Log.i("Parse Result", "Failed" + ex.toString());
                        }
                    }
                });

                dialog.cancel();
            }
        }, 2000);
    }

    public void setBinDialog() {

        builder = new AlertDialog.Builder(this);
        builder.setMessage("Remove all items in basket?");
        builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                System.out.println("clicked on remove!");
                MenuActivity.totalPrice = 0.00;
                MenuActivity.selectedItemsList.clear();
                Intent intent = new Intent(BasketActivity.this, MenuActivity.class);
                startActivity(intent);

            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                System.out.println("clicked on cancel");

            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }
}
