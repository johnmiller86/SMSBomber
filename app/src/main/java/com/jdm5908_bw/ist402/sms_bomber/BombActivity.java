package com.jdm5908_bw.ist402.sms_bomber;

// Imports
import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
    private static final int REQUEST_PERMISSION = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 3;

    // UI Components
    private TextView targetNameTextView;
    private EditText messageEditText, quantityEditText;
    private ImageView targetImageView;

    // Instance Variables
    private Uri contact;
    private String contactId, contactNumber;
    private SmsManager smsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bomb);

        // Initializing UI Components
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        quantityEditText = (EditText) findViewById(R.id.quantityEditText);
        targetImageView = (ImageView) findViewById(R.id.imageView);
        targetNameTextView = (TextView) findViewById(R.id.textView);
        smsManager = SmsManager.getDefault();
    }

    /**
     * Click Listener for the Select Contact Button.
     * @param view the select button.
     */
    @SuppressWarnings("unused")
    public void pickContact(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    /**
     * Click Listener for the Send Text Button.
     * @param view the send button.
     */
    @SuppressWarnings("unused")
    public void sendText(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
    }

    /**
     * Handles operations based on permission results.
     * @param requestCode the request code.
     * @param permissions the result code.
     * @param grantResults the grant results array.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // Granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_PERMISSION);
                }
                // Blocked
                else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){
                    new AlertDialog.Builder(this)
                            .setTitle("Permission was blocked!")
                            .setMessage("You have previously blocked this app from accessing contacts. This app will not function without this access. Would you like to go to settings and allow this permission?")

                            // Open Settings button
                            .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    goToSettings();
                                }
                            })

                            // Denied, close app
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                // Denied
                else{
                    new AlertDialog.Builder(this)
                            .setTitle("Permission was denied!")
                            .setMessage("This app will not function without access to  contacts. Would you like to allow access?")

                            // Open Settings button
                            .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(BombActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                                }
                            })

                            // Denied, close app
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_SEND_SMS:{
                // Granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    sendBombs();
                }
                // Blocked
                else if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                    new AlertDialog.Builder(this)
                            .setTitle("Permission was blocked!")
                            .setMessage("You have previously blocked this app from sending SMS. This app will not function without this access. Would you like to go to settings and allow this permission?")

                            // Open Settings button
                            .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    goToSettings();
                                }
                            })

                            // Denied, close app
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                // Denied
                else {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission was denied!")
                            .setMessage("This app will not function without access to sending SMS. Would you like to allow access?")

                            // Open Settings button
                            .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(BombActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                                }
                            })

                            // Denied, close app
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        }
    }

    /**
     * Performs operations upon successful target selection.
     * @param requestCode the request code.
     * @param resultCode the result code.
     * @param data the data returned from the Intent.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION && resultCode == RESULT_OK){
            contact = data.getData();
            retrieveContactName();
            retrieveContactNumber();
            retrieveContactPhoto();
        }
    }

    /**
     * Gets the Contact's Name.
     */
    private void retrieveContactName() {

        Cursor cursor = getContentResolver().query(contact, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()){
            targetNameTextView.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            cursor.close();
        }
    }

    /**
     * Gets the Contact's Number.
     */
    private void retrieveContactNumber() {

        contactNumber = null;

        // Querying Contact ID
        Cursor cursor = getContentResolver().query(contact, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        if (cursor != null && cursor.moveToFirst()){
            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            cursor.close();
        }

        // Retrieving Mobile Number
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{contactId}, null);

        // Storing Mobile Number
        if (cursor != null && cursor.moveToFirst()){
            contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            cursor.close();
        }
    }

    /**
     * Gets and Sets the Contact's Picture if Available.
     */
    private void retrieveContactPhoto() {

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId)));

            if (inputStream != null){
                Bitmap photo = BitmapFactory.decodeStream(inputStream);
                targetImageView.setImageBitmap(photo);
                inputStream.close();
            }else{
                targetImageView.setImageResource(R.mipmap.ic_launcher);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Deploys messages.
     */
    private void sendBombs(){
        // Contact not selected
        if (contact == null || contact.toString().equals("")){
            Toast.makeText(this, "You must select a target!!", Toast.LENGTH_SHORT).show();
        }
        // Contact selected has no number
        else if (contactNumber == null || contactNumber.equals("")){
            Toast.makeText(this, "Target has no associated mobile number!!", Toast.LENGTH_SHORT).show();
        }
        // Amount empty
        else if (quantityEditText.getText().toString().equals("")){
            Toast.makeText(this, "An amount was not specified!!", Toast.LENGTH_SHORT).show();
        }
        // Message empty
        else if (messageEditText.getText().toString().equals("")){
            Toast.makeText(this, "A message was not specified!!", Toast.LENGTH_SHORT).show();
        }
        // Send
        else{
            int bombs = Integer.parseInt(quantityEditText.getText().toString());
            try {
                for (int i = 0; i < bombs; i++) {
                    smsManager.sendTextMessage(contactNumber, null, messageEditText.getText().toString(), null, null);
                }
                Toast.makeText(this, "Bombs Deployed!!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Launch Failed!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Opens the app's settings page in AppManager.
     */
    private void goToSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION);
    }
}