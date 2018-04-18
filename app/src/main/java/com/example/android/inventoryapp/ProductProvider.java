package com.example.android.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.example.android.inventoryapp.data.DbHelper;
import com.example.android.inventoryapp.data.ProductContract;

import static com.example.android.inventoryapp.data.ProductContract.ProductEntry.CONTENT_URI;

public class ProductProvider extends ContentProvider {
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private DbHelper dbHelper;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            Toast needNameToast = Toast.makeText(getContext(), R.string.null_product_name_toast, Toast.LENGTH_SHORT);
            needNameToast.show();
        }
        Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRICE);
        if (price == null || price <= 0) {
            Toast needPriceToast = Toast.makeText(getContext(), R.string.null_product_price_toast, Toast.LENGTH_SHORT);
            needPriceToast.show();
        }
        Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE);
        if (quantity == null || quantity <0) {
            Toast needQuantityToast = Toast.makeText(getContext(), R.string.null_product_quantity_toast, Toast.LENGTH_SHORT);
            needQuantityToast.show();
        }
        String imageFileName = values.getAsString(ProductContract.ProductEntry.COLUMN_IMAGE);
        if (imageFileName == null) {
            Toast needNameToast = Toast.makeText(getContext(), R.string.null_product_image_toast, Toast.LENGTH_SHORT);
            needNameToast.show();
        }

        long id = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                Toast needNameToast = Toast.makeText(getContext(), R.string.null_product_name_toast, Toast.LENGTH_SHORT);
                needNameToast.show();
            }
        }
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRICE);
            if (price == null || price <= 0) {
                Toast needPriceToast = Toast.makeText(getContext(), R.string.null_product_price_toast, Toast.LENGTH_SHORT);
                needPriceToast.show();
            }
        }
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE)) {
            Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE);
            if (quantity == null) {
                Toast needQuantityToast = Toast.makeText(getContext(), R.string.null_product_quantity_toast, Toast.LENGTH_SHORT);
                needQuantityToast.show();
            } else if (quantity <= 0) {
                Toast needQuantityToast = Toast.makeText(getContext(), R.string.quantity_at_zero, Toast.LENGTH_SHORT);
                needQuantityToast.show();
            }
        }
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_IMAGE)) {
            String imageFileName = values.getAsString(ProductContract.ProductEntry.COLUMN_IMAGE);
            if (imageFileName == null) {
                Toast needNameToast = Toast.makeText(getContext(), R.string.null_product_image_toast, Toast.LENGTH_SHORT);
                needNameToast.show();
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        getContext().getContentResolver().notifyChange(uri, null);
        return database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    static public Uri getContentUri(int productId) {
        return ContentUris.withAppendedId(CONTENT_URI, productId);
    }
}
