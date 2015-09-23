package contacts.com.contactsmanager;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Jithin on 9/21/2015.
 */
public class EditContact extends AppCompatActivity {
    private final int PICK_PHOTO = 1;
    String id, row_id, name_text, phone_text, email_text, image;
    Bitmap bitmap;
    ImageView photo;
    TextView name, phone, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_contact);

        Intent i = getIntent();
        id = i.getExtras().getString("id");
        row_id = i.getExtras().getString("row_id");
        name_text = i.getExtras().getString("name");
        phone_text = i.getExtras().getString("phone");
        email_text = i.getExtras().getString("email");
        image = i.getStringExtra("image");

        photo = (ImageView) findViewById(R.id.photo);
        name = (TextView) findViewById(R.id.name);
        phone = (TextView) findViewById(R.id.phone);
        email = (TextView) findViewById(R.id.email);

        if(image.equalsIgnoreCase("")) {
            photo.setImageResource(R.drawable.noimage);
        }
        else {
            try {
                FileInputStream is = this.openFileInput(image);
                bitmap = BitmapFactory.decodeStream(is);
                photo.setImageBitmap(bitmap);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        name.setText(name_text);
        phone.setText(phone_text);
        email.setText(email_text);
    }

    public void selectPhoto(View v) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case PICK_PHOTO:
                if(resultCode == RESULT_OK){
                    // Getting the uri of the picked photo
                    Uri selectedImage = data.getData();

                    InputStream imageStream = null;
                    try {
                        // Getting InputStream of the selected image
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    // Creating bitmap of the selected image from its inputstream
                    bitmap = BitmapFactory.decodeStream(imageStream);

                    // Getting reference to ImageView
                    ImageView photo = (ImageView) findViewById(R.id.photo);

                    // Setting Bitmap to Imageview
                    photo.setImageBitmap(bitmap);
                }
        }
    }

    public void saveContact(View v) {
        String names = name.getText().toString();
        String phones = phone.getText().toString();
        String emails = email.getText().toString();

        if(names.trim().equals("")) {
            name.setError("name required.");
        }
        else if(phones.trim().equals("")) {
            phone.setError("phone number required.");
        }
        else {
            int row = Integer.parseInt(row_id);

            ContentResolver contentResolver  = getContentResolver();

            String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

            String[] emailParams = new String[]{id, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
            String[] nameParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
            String[] numberParams = new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
            String[] photoParams = new String[]{id, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};

            ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<android.content.ContentProviderOperation>();

            if(email_text == null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, row);
                contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                contentValues.put(ContactsContract.CommonDataKinds.Email.ADDRESS, emails);
                contentValues.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME);

                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI).withValues(contentValues).build());
            }
            else {
                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, emailParams)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, emails)
                        .build());
            }

            ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, nameParams)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, names)
                    .build());

            ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, numberParams)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phones)
                    .build());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if(bitmap != null){    // If an image is selected successfully
                bitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

                // to update Photo in the table ContactsContract.Data
                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, photoParams)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                        .build());

                try {
                    stream.flush();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(getApplicationContext(), "Contact updated.", Toast.LENGTH_LONG).show();

                Intent i = new Intent(EditContact.this, MainActivity.class);
                startActivity(i);
                finish();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelContact(View v) {
        Intent i = new Intent(EditContact.this, SingleContact.class);
        i.putExtra("name", name_text);
        i.putExtra("phone_number", phone_text);
        i.putExtra("email", email_text);
        i.putExtra("image", image);
        startActivity(i);
        finish();
    }

    public  void  deleteContact(View v) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone_text));
        Cursor cur = getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name_text)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        getContentResolver().delete(uri, null, null);
                        Toast.makeText(getApplicationContext(), "Contact deleted.", Toast.LENGTH_LONG).show();

                        Intent i = new Intent(EditContact.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}
