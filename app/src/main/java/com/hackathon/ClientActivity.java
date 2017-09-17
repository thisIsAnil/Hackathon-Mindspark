package com.hackathon;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * Created by Aniket on 17-Sep-17.
 */

public class ClientActivity extends Activity {
    private WebView webView;
    private StorageReference riversRef;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        riversRef = mStorageRef.child("files/test.pcx");

        ProgressDialog dialog=new ProgressDialog(ClientActivity.this);
        dialog.setMessage("Downloading...");
        dialog.setTitle("Download");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        final File localFile = new File(AppFolderMaker.getDownloadFile(),"test.pcx");
        if(localFile.exists())localFile.delete();
        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Intent intent=new Intent(ClientActivity.this,ReaderActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT,localFile.getAbsolutePath());
                        startActivity(intent);
                        Toast.makeText(ClientActivity.this, "Downloaded file ", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ClientActivity.this, "Failed to download ", Toast.LENGTH_LONG).show();
            }
        });
    }
}
