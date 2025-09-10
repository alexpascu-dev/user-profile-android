package com.example.myprofile.ui.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.example.myprofile.R;

public class BarcodeScanHelper {
    private static final String TAG = "BarcodeScanHelper";

    private final Context context;
    private final BarcodeScanListener listener;
    private final BroadcastReceiver barcodeReceiver;

    public interface BarcodeScanListener {
        void onBarcodeScanned(String data);
        void onError(String error);
    }

    public BarcodeScanHelper(Context context, BarcodeScanListener listener) {
        this.context = context;
        this.listener = listener;

        barcodeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast intent: " + intent.getAction());

                String action = intent.getAction();
                if (action != null) {
                    Log.d(TAG, "Intent action: " + action);
                    Log.d(TAG, "Expected action: " + context.getString(R.string.activity_intent_filter_action));

                    if (action.equals(context.getString(R.string.activity_intent_filter_action))) {
                        Log.d(TAG, "Action matches! Processing barcode data...");

                        try {
                            String decodedData = intent.getStringExtra(
                                    context.getString(R.string.datawedge_intent_key_data)
                            );

                            Log.d(TAG, "Decoded data: " + decodedData);

                            if (decodedData != null && !decodedData.isEmpty()) {
                                Log.d(TAG, "Calling onBarcodeScanned with data: " + decodedData);
                                listener.onBarcodeScanned(decodedData);
                            } else {
                                Log.w(TAG, "No data received from scanner");
                                listener.onError("No data received from scanner.");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing barcode scan", e);
                            listener.onError("Error processing barcode scan: " + e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Action doesn't match expected action");
                    }
                } else {
                    Log.w(TAG, "Received intent with null action");
                }
            }
        };
    }

    public void triggerScan() {
        try {
            Intent scanIntent = new Intent();
            String action = context.getString(R.string.datawedge_intent_action);
            String trigger = context.getString(R.string.datawedge_intent_trigger);

            scanIntent.setAction(action);
            scanIntent.putExtra(trigger, "START_SCANNING");

            Log.d(TAG, "Triggering scan with action: " + action);
            Log.d(TAG, "Trigger extra: " + trigger + " = START_SCANNING");
            Log.d(TAG, "Sending broadcast intent: " + scanIntent);

            context.sendBroadcast(scanIntent);

            // Show a debug toast
            Toast.makeText(context, "Scan triggered - check logs", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error triggering scan", e);
            listener.onError("Error triggering scan: " + e.getMessage());
        }
    }

    public void registerReceiver() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            String action = context.getString(R.string.activity_intent_filter_action);
            filter.addAction(action);

            Log.d(TAG, "Registering receiver for action: " + action);

            context.registerReceiver(barcodeReceiver, filter, Context.RECEIVER_EXPORTED);

            Log.d(TAG, "Receiver registered successfully");

            // Show debug toast
            Toast.makeText(context, "Barcode receiver registered", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error registering receiver", e);
            listener.onError("Error registering receiver: " + e.getMessage());
        }
    }

    public void unregisterReceiver() {
        try {
            Log.d(TAG, "Unregistering barcode receiver");
            context.unregisterReceiver(barcodeReceiver);
            Log.d(TAG, "Receiver unregistered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }
    }
}