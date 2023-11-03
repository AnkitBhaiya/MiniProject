package org.haryanvideveloper.miniproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.PendingIntent;
import android.telephony.SmsManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText statusEditText;
    private ToggleButton toggleButton;
    private String statusMessage;
    private boolean isActive;

    // BroadcastReceiver to detect incoming calls
    private BroadcastReceiver callReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                if (isActive) {
                    // Display a toast with the status message
                    Toast.makeText(context, "Status: " + statusMessage, Toast.LENGTH_SHORT).show();
                }
            }else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (isActive) {
                    // Call was missed, send an SMS
                    String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        sendStatusMessageSMS(phoneNumber, statusMessage);
                    }
                }
            }
        }
    };

    private void sendStatusMessageSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);

            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent);
        } catch (Exception e) {
            // Handle SMS sending failure
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusEditText = findViewById(R.id.statusEditText);
        toggleButton = findViewById(R.id.toggleButton);

        // Load the status message and activation state from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        statusMessage = sharedPreferences.getString("statusMessage", "");
        isActive = sharedPreferences.getBoolean("isActive", false);

        // Set the initial UI state
        statusEditText.setText(statusMessage);
        toggleButton.setChecked(isActive);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActive = toggleButton.isChecked();
                // Save the activation state in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isActive", isActive);
                editor.apply();
            }
        });

        // Register the BroadcastReceiver to detect incoming calls
        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver when the app is destroyed
        unregisterReceiver(callReceiver);
    }
}
