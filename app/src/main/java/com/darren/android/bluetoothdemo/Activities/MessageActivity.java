package com.darren.android.bluetoothdemo.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.darren.android.bluetoothdemo.R;
import com.darren.android.bluetoothdemo.ViewModel.MessageViewModel;

import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MessageActivity";

    private MessageViewModel messageViewModel;

    private StringBuilder message;

    @BindView(R.id.receivedMessageTextView)
    TextView receivedMessageTV;
    @BindView(R.id.inputMessageEditText)
    EditText inputMessageET;
    @BindView(R.id.sendMessageButton)
    Button sendMessageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        messageViewModel = new MessageViewModel(getApplicationContext());

        message = new StringBuilder();
        LocalBroadcastManager.getInstance(this).registerReceiver(incomingMessageReceiver, new IntentFilter(getString(R.string.tag_incoming_message)));
    }

    @OnClick(R.id.sendMessageButton)
    public void onSendMessageClicked() {
        byte[] bytes = inputMessageET.getText().toString().getBytes(Charset.defaultCharset());
        messageViewModel.bluetoothMessageWrite(bytes);

        inputMessageET.setText("");
    }

    // Broadcast receiver for incoming message
    private final BroadcastReceiver incomingMessageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "incomingMessageReceiver: Receive incoming message");
            String text = intent.getStringExtra(getString(R.string.tag_message));
            message.append(text + "\n");

            receivedMessageTV.setText(message);
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: onDestroy called");
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(incomingMessageReceiver);
    }
}
