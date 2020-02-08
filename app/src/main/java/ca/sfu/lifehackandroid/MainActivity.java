package ca.sfu.lifehackandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdpt;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] intentFiltersArray;

    private String siteLink;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent nfcIntent = new Intent(this, getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        nfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);

        IntentFilter tagIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            tagIntentFilter.addDataType("text/plain");
            intentFiltersArray = new IntentFilter[]{tagIntentFilter};
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        nfcAdpt = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdpt == null) {
            Toast.makeText(this, "NFC not supported", Toast.LENGTH_LONG).show();
            finish();
        }

        if (!nfcAdpt.isEnabled()) {
            Toast.makeText(this, "Enable NFC before using the app", Toast.LENGTH_LONG).show();
        }
    }

    protected void onResume() {
        super.onResume();
        nfcAdpt.enableForegroundDispatch(this, nfcPendingIntent, intentFiltersArray, null);
        displayData(NFCMessageOperations.resolveIntent(getIntent()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdpt.disableForegroundDispatch(this);
    }

     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         setIntent(intent);
         displayData(NFCMessageOperations.resolveIntent(intent));
     }

    private void displayData(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<NFCMessage> records = NFCMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++) {
            NFCMessage record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }

        TextView textView = findViewById(R.id.linkText);
        textView.setText(builder.toString());
        siteLink = builder.toString();
    }
}
