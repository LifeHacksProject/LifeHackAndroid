package ca.sfu.lifehackandroid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.URI;



public class DocumentReader extends AppCompatActivity {

    TextView pathShow;
    Button getPath;
    private String docPath;

    public String getDocPath() {
        return docPath;
    }

    Intent findFile;

    // 9:06 txt_PathShow
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:

                if (resultCode == RESULT_OK) {

                    String path = data.getData().getPath();
                    docPath = path;
                    pathShow.setText(docPath);
                }
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_reader);

        pathShow = (TextView)findViewById(R.id.filepath);
        getPath = (Button)findViewById(R.id.select_file);

        getPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                findFile = new Intent(Intent.ACTION_GET_CONTENT);
                findFile.setType("*/*");
                startActivityForResult(findFile, 10);
            }
        });

    }




}
