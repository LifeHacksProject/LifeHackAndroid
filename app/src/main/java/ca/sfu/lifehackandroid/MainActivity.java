package ca.sfu.lifehackandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdpt;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] intentFiltersArray;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;

    private String siteLink;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Button getPath = findViewById(R.id.selectButton);
        getPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent findFile = new Intent(Intent.ACTION_GET_CONTENT);
                findFile.setType("*/*");
                startActivityForResult(findFile, 10);
            }
        });

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

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            uploadSelectedFile();
        } else {
            signInAnonymously();
        }
    }

    private void uploadSelectedFile() {
        StorageReference ref = mStorageRef.child("resumefolder/" + filePath.getLastPathSegment());

        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
//                        Uri downloadUrl = ref.getDownloadUrl();
                        Log.d("FILE UPLOADED", filePath.getLastPathSegment());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        uploadSelectedFile();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("TAG", "signInAnonymously:FAILURE", exception);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {

                Uri path = data.getData();
                if (path != null) {
                    filePath = path;
                    TextView fileText = findViewById(R.id.fileText);
                    fileText.setText(path.getLastPathSegment());
                }
            }
        }
    }
}
