package com.jdm5908_bw.ist402.smsbomber;

// Imports
import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

/**
 * Main Activity for the SMS Bomber.
 */
public class BombActivity extends AppCompatActivity {

    // ID Constants
    private static final int ACTION_PICK_RESULT = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 3;

    // UI Components
    private TextView contactNameTextView;
    private EditText messageEditText, quantityEditText;
    private ImageView imageView;

    // Instance Variables
    private Uri contact;
    private String contactId, contactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bomb);

        // Initializing UI Components
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        quantityEditText = (EditText) findViewById(R.id.quantityEditText);
        imageView = (ImageView) findViewById(R.id.imageView);
        contactNameTextView = (TextView) findViewById(R.id.textView);
    }

    /**
     * Click Listener for the Select Contact Button.
     * @param view the select button.
     */
    public void pickContact(View view){

        // If OS == Marshmallow
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), ACTION_PICK_RESULT);
    }

    /**
     * Click Listener for the Send Text Button.
     * @param view the send button.
     */
    public void sendText(View view){
        SmsManager smsManager = SmsManager.getDefault();

        // If OS == Marshmallow
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (contactNumber != null) {
            int bombs = Integer.parseInt(quantityEditText.getText().toString());
            try {
                for (int i = 0; i < bombs; i++){
                    smsManager.sendTextMessage(contactNumber, null, messageEditText.getText().toString(), null, null);
                    Toast.makeText(this, "Sending Bombs!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_PICK_RESULT && resultCode == RESULT_OK){
            contact = data.getData();
            retrieveContactName();
            retrieveContactNumber();
            retrieveContactPhoto();
        }
    }

    /**
     * Gets and Sets the Contact's Picture if Available.
     */
    private void retrieveContactPhoto() {

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));

            if (inputStream != null){
                Bitmap photo = BitmapFactory.decodeStream(inputStream);
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(photo);
                inputStream.close();
            }
            else{
                //TODO use non-deprecated method.
                imageView.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the Contact's Number.
     */
    private void retrieveContactNumber() {

        contactNumber = null;

        // Querying Contact ID
        Cursor cursor = getContentResolver().query(contact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursor.moveToFirst()) {

            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursor.close();

        // Retrieving Number(s)
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactId},
                null);

        // Storing Contacts Number
        if (cursor.moveToFirst()) {
            contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursor.close();

        // Contact Doesn't Contain Number
        if (contactNumber == null) {
            Toast.makeText(this, "This contact does not have a number!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets the Contact's Name.
     */
    private void retrieveContactName() {

        Cursor cursor = getContentResolver().query(contact, null, null, null, null);

        if (cursor.moveToFirst()) {

            contactNameTextView.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
        }

        cursor.close();
    }
}
