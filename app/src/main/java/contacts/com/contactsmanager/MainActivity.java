package contacts.com.contactsmanager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ListView contacts_list;
    EditText search;
    Cursor phones;
    ContentResolver resolver;
    Bitmap bit_thumb;
    ArrayList<SelectUser> selectUsers;
    SelectUserAdapter adapter;
    SelectUser data;
    String emails;
    TextView nothing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MainActivity.this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        selectUsers = new ArrayList<SelectUser>();
        resolver = getContentResolver();

        contacts_list = (ListView) findViewById(R.id.contacts_list);
        search = (EditText) findViewById(R.id.search);
        nothing = (TextView) findViewById(R.id.nothing);

        nothing.setVisibility(View.GONE);

        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String newText = search.getText().toString();
                String regexStr = "^[0-9]*$";

                if(newText.trim().matches(regexStr))
                    adapter.filterUsingNumber(newText);
                else
                    adapter.filter(newText);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        selectUsers.clear();

        phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        LoadContact loadContact = new LoadContact();
        loadContact.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_LONG).show();
            return true;
        }
        if(id == R.id.action_delete) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            while (cur.moveToNext()) {
                try{
                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    cr.delete(uri, null, null);
                }
                catch(Exception e)
                {
                    System.out.println(e.getStackTrace());
                }
            }
            Toast.makeText(getApplicationContext(), "All contacts deleted.", Toast.LENGTH_LONG).show();

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class LoadContact extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone

            if (phones != null) {
                Log.e("count", "" + phones.getCount());
                if (phones.getCount() == 0) {
                    nothing.setVisibility(View.VISIBLE);
                }

                while (phones.moveToNext()) {
                    String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String row = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));
                    String selected_name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String image_thumb = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                    SelectUser selectUser = new SelectUser();

                    try {
                        if (image_thumb != null) {
                            System.out.println(Uri.parse(image_thumb));
                            bit_thumb = MediaStore.Images.Media.getBitmap(resolver, Uri.parse(image_thumb));
                            selectUser.setThumb(bit_thumb);
                        } else {
                            Log.e("No Image Thumb", "--------------");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    selectUser.setId(id);
                    selectUser.setRowId(row);
                    selectUser.setName(selected_name);
                    selectUser.setPhone(phoneNumber);

                    Cursor emailCursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);

                    while (emailCursor.moveToNext()) {
                        emails = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        selectUser.setEmail(emails);
                    }
                    emailCursor.close();

                    selectUser.setCheckedBox(false);
                    selectUsers.add(selectUser);
                }
            } else {
                Log.e("Cursor close 1", "----------------");
            }
            //phones.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new SelectUserAdapter(selectUsers, MainActivity.this);
            contacts_list.setAdapter(adapter);

            // Select item on listclick
            contacts_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Log.e("search", "here---------------- listener");

                    data = selectUsers.get(i);

                    String id = data.getId();
                    String row = data.getRowId();
                    String phoneNumber = data.getPhone();
                    String email = data.getEmail();
                    String name = data.getName();
                    Bitmap image = data.getThumb();

                    if (image == null) {
                        Intent i1 = new Intent(MainActivity.this, SingleContact.class);
                        i1.putExtra("id", id);
                        i1.putExtra("row_id", row);
                        i1.putExtra("name", name);
                        i1.putExtra("phone_number", phoneNumber);
                        i1.putExtra("email", email);
                        i1.putExtra("image", "");
                        startActivity(i1);
                    } else {
                        String filename = "ContactsManager.png";
                        try {
                            FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE);
                            image.compress(Bitmap.CompressFormat.PNG, 75, stream);
                            //Cleanup
                            stream.close();
                            image.recycle();

                            Intent i1 = new Intent(MainActivity.this, SingleContact.class);
                            i1.putExtra("id", id);
                            i1.putExtra("row_id", row);
                            i1.putExtra("name", name);
                            i1.putExtra("phone_number", phoneNumber);
                            i1.putExtra("email", email);
                            i1.putExtra("image", filename);
                            startActivity(i1);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            contacts_list.setFastScrollEnabled(true);
        }
    }

    public  void addContact(View v) {
        Intent i = new Intent(MainActivity.this, AddContact.class);
        startActivity(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
        phones.close();
    }
}
