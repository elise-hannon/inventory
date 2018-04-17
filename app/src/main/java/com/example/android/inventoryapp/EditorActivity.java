package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.android.inventoryapp.data.ProductContract;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private int quantityInt;
    private int priceInt;
    private static final int EXISTING_LOADER = 0;
    private String name;
    public static final int GET_FROM_GALLERY = 3;
    private ImageButton imageButton;
    private Uri currentUri;
    private boolean productHasChanged = false;
    private Bitmap bitmap;
    private byte[] photo;

    static public Intent getStartIntent(Context context, Uri contentUri) {
        Intent intent = new Intent(context, EditorActivity.class);
        intent.setData(contentUri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        currentUri = getIntent().getData();

        if (currentUri != null) {
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        nameEditText = findViewById(R.id.edit_product_name);
        priceEditText = findViewById(R.id.edit_product_price);
        quantityEditText = findViewById(R.id.current_quantity);

        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);

        Button incrementButton = findViewById(R.id.increment_button);
        Button decrementButton = findViewById(R.id.decrement_button);
        Button orderButton = findViewById(R.id.order_from_supplier_button);
        imageButton = findViewById(R.id.image_button);

        if (currentUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQuantity = quantityInt + 1;

                ContentValues values = new ContentValues();
                values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, newQuantity);

                getContentResolver().update(currentUri, values, null, null);
                quantityEditText.setText(String.valueOf(newQuantity));
            }
        });

        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityInt > 0) {
                    int newQuantity = quantityInt - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, newQuantity);

                    getContentResolver().update(currentUri, values, null, null);
                    quantityEditText.setText(String.valueOf(newQuantity));
                } else {
                    Toast toast = Toast.makeText(EditorActivity.this, R.string.quantity_less_than_zero, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_line));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.order_message_template, name));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private boolean saveProduct() {
        String name = nameEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();

        if (!isValidData(name, price, quantity, photo)) {
            return false;
        }

        if (currentUri == null &&
            TextUtils.isEmpty(name) && TextUtils.isEmpty(name) &&
            TextUtils.isEmpty(price) && TextUtils.isEmpty(quantity)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, name);
        priceInt = Integer.parseInt(price);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, priceInt);
        quantityInt = Integer.parseInt(quantity);
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE, quantityInt);
        values.put(ProductContract.ProductEntry.COLUMN_IMAGE, photo);

        if (currentUri == null) {

            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                               +Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                               Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                               Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                               Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveProduct()) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE,
                ProductContract.ProductEntry.COLUMN_IMAGE};

        return new CursorLoader(this, currentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY_AVAILABLE);
            int imageColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE);

            name = data.getString(nameColumnIndex);
            priceInt = data.getInt(priceColumnIndex);
            quantityInt = data.getInt(quantityColumnIndex);
            photo = data.getBlob(imageColumnIndex);

            nameEditText.setText(name);
            priceEditText.setText(Integer.toString(priceInt));
            quantityEditText.setText(Integer.toString(quantityInt));
            bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            imageButton.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setSelection(0);
        quantityEditText.setSelection(0);
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {

        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentUri != null) {
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                               Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                               Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageButton.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                photo = baos.toByteArray();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isValidData(String name, String price, String quantity, byte[] photos) {
        if (name == null || TextUtils.isEmpty(name)) {
            Toast needNameToast = Toast.makeText(this, R.string.null_product_name_toast, Toast.LENGTH_SHORT);
            needNameToast.show();
            return false;
        }
        if (price == null || TextUtils.isEmpty(price) || Integer.parseInt(price) <= 0) {
            Toast needPriceToast = Toast.makeText(this, R.string.null_product_price_toast, Toast.LENGTH_SHORT);
            needPriceToast.show();
            return false;
        }
        if (quantity == null || TextUtils.isEmpty(quantity) || Integer.parseInt(quantity) <= 0) {
            Toast needQuantityToast = Toast.makeText(this, R.string.null_product_quantity_toast, Toast.LENGTH_SHORT);
            needQuantityToast.show();
            return false;
        }
        if (photos == null || photos.length == 0) {
            Toast needNameToast = Toast.makeText(this, R.string.null_product_image_toast, Toast.LENGTH_SHORT);
            needNameToast.show();
            return false;
        }
        return true;
    }
}

