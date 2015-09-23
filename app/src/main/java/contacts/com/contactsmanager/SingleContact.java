package contacts.com.contactsmanager;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;

/**
 * Created by Jithin on 9/20/2015.
 */
public class SingleContact extends ActionBarActivity {

    ImageView image;
    TextView name, phone, email, email_head;
    String id, row_id, name_text, phone_text, email_text, filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_single);

        image = (ImageView) findViewById(R.id.image);
        name = (TextView) findViewById(R.id.name);
        phone = (TextView) findViewById(R.id.phone);
        email = (TextView) findViewById(R.id.email);
        email_head = (TextView) findViewById(R.id.email_head);

        Intent i = getIntent();

        id = i.getExtras().getString("id");
        row_id = i.getExtras().getString("row_id");
        name_text = i.getExtras().getString("name");
        phone_text = i.getExtras().getString("phone_number");
        email_text = i.getExtras().getString("email");

        name.setText(name_text);
        phone.setText(phone_text);

        if(email_text == null) {
            email.setVisibility(View.GONE);
            email_head.setVisibility(View.GONE);
        }
        else
            email.setText(email_text);

        Bitmap bmp = null;
        filename = i.getStringExtra("image");

        if(filename.equalsIgnoreCase("")) {
            image.setImageResource(R.drawable.noimage);
        }
        else {
            try {
                FileInputStream is = this.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                image.setImageBitmap(bmp);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public  void makeCall(View v) {
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:"+phone_text));
        startActivity(i);
    }

    public void editContact(View v) {
        Intent i = new Intent(SingleContact.this, EditContact.class);
        i.putExtra("id", id);
        i.putExtra("row_id", row_id);
        i.putExtra("name", name_text);
        i.putExtra("phone", phone_text);
        i.putExtra("email", email_text);
        i.putExtra("image", filename);
        startActivity(i);
    }
}
