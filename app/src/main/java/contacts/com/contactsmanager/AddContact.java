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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Jithin on 9/21/2015.
 */
public class AddContact extends AppCompatActivity {

    EditText name, phone, email;
    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    private final int PICK_PHOTO = 1;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);

        name = (EditText) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
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
        String name_text = name.getText().toString();
        String phone_text = phone.getText().toString();
        String email_text = email.getText().toString();

        if(name_text.trim().equals("")) {
            name.setError("name required.");
        }
        else if(phone_text.trim().equals("")) {
            phone.setError("phone number required.");
        }
        else {
            int rawContactID = ops.size();

            // to insert a new raw contact in the table ContactsContract.RawContacts
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // to insert display name in the table ContactsContract.Data
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name_text)
                    .build());

            // to insert Mobile Number in the table ContactsContract.Data
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone_text)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());

            // to insert Home Email in the table ContactsContract.Data
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email_text)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                    .build());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if(bitmap != null){    // If an image is selected successfully
                bitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

                // Adding insert operation to operations list
                // to insert Photo in the table ContactsContract.Data
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                        .build());

                try {
                    stream.flush();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try{
                // Executing all the insert operations as a single database transaction
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(AddContact.this, "Contact added.", Toast.LENGTH_LONG).show();
                finish();
            }catch (RemoteException e) {
                e.printStackTrace();
            }catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelContact(View v) {
        Intent i = new Intent(AddContact.this, MainActivity.class);
        startActivity(i);
    }
}
