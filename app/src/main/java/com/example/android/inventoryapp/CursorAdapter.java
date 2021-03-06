package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.inventoryapp.data.ProductContract;

public class CursorAdapter extends android.widget.CursorAdapter {

    CursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price_number);
        final TextView quantityTextView = view.findViewById(R.id.quantity_number);
        Button saleButton = view.findViewById(R.id.sale_button);

        int productIdIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE);

        String productName = cursor.getString(nameColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int productId = cursor.getInt(productIdIndex);

        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(price));
        quantityTextView.setText(String.valueOf(quantity));
        saleButton.setText(R.string.sale_button);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri contentUri = ProductProvider.getContentUri(productId);
                Intent intent = EditorActivity.getStartIntent(context, contentUri);
                context.startActivity(intent);
            }
        });

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0 ) {
                    int newQuantity = quantity-1;
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, newQuantity);

                    context.getContentResolver().update(
                            ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,productId),values,
                            null, null);
                    quantityTextView.setText(String.valueOf(newQuantity));
                } else {
                    Toast toast = Toast.makeText(v.getContext(), R.string.quantity_at_zero, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
