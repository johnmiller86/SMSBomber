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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
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
    private Button selectTargetButton, launchButton;

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
        launchButton = (Button) findViewById(R.id.sendTextButton);
        selectTargetButton = (Button) findViewById(R.id.selectContactButton);
        smsManager = SmsManager.getDefault();
    }

    /**
     * Click Listener for the Select Contact Button.
     * @param view the select button.
     */
    public void pickContact(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    /**
     * Click Listener for the Send Text Button.
     * @param view the send button.
     */
    public void sendText(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
    }
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
     * Gets and Sets the Contact's Picture if Available.
     */
    private void retrieveContactPhoto() {

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId)));

            if (inputStream != null){
                Bitmap photo = BitmapFactory.decodeStream(inputStream);
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(photo);
                inputStream.close();
            }else{
                targetImageView.setImageResource(R.mipmap.ic_launcher);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Gets the Contact's Number.
     */
    private void retrieveContactNumber() {

        contactNumber = null;

        // Querying Contact ID
        Cursor cursor = getContentResolver().query(contact, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        if (cursor.moveToFirst()){
            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursor.close();

        // Retrieving Number(s)
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{contactId}, null);

        // Storing Contacts Number
        if (cursor.moveToFirst()){
            contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursor.close();

        // Contact Doesn't Contain Number
        if (contactNumber == null){
            Toast.makeText(this, "This contact does not have a number!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets the Contact's Name.
     */
    private void retrieveContactName() {

        Cursor cursor = getContentResolver().query(contact, null, null, null, null);

        if (cursor.moveToFirst()){
            targetNameTextView.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
        }
        cursor.close();
    }

    /**
     * Deploys messages.
     */
    private void sendBombs(){
        if (contactNumber != null && !contactNumber.equals("")) {
            int bombs = 0;
            try {
                bombs = Integer.parseInt(quantityEditText.getText().toString());
            }catch (NumberFormatException e){
                Toast.makeText(this, "An amount was not specified!!", Toast.LENGTH_SHORT).show();
            }
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

    private void goToSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION);
    }
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
                    // TODO Show Dialog Explanation
                    goToSettings();
                }
                // Denied
                else{
                    selectTargetButton.setEnabled(false);
                    // TODO Show Dialog Explanation
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
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
                    // TODO Show Dialog Explanation
                    goToSettings();
                }
                // Denied
                else {
                    launchButton.setEnabled(false);
                    // TODO Show Dialog Explanation
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                }
            }
        }
    }
}
