package com.example.android.inventoryapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.android.inventoryapp.data.ProductContract;

public class CursorAdapter extends android.widget.CursorAdapter {

    public CursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        Button saleButton = view.findViewById(R.id.sale_button);

        int productIdIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE);

        String productName = cursor.getString(nameColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        int quanitity = cursor.getInt(quantityColumnIndex);
        final int productId = cursor.getInt(productIdIndex);

        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(price));
        quantityTextView.setText(String.valueOf(quanitity));
        saleButton.setText(R.string.sale_button);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri contentUri = ProductProvider.getContentUri(productId);
                Intent intent = EditorActivity.getStartIntent(context, contentUri);
                context.startActivity(intent);
            }
        });
    }
}
