package com.hackathon.textEditor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hackathon.AndroidUtilities;
import com.hackathon.AppFolderMaker;
import com.hackathon.Log;
import com.hackathon.R;
import com.hackathon.pcx.*;
import com.hackathon.pcx.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 234,PICK_MAC=255;
    private Uri filePath;

    private RichEditor mEditor;
    private TextView mPreview;
    private String data;
    private String macFile;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_layout);
        mEditor = (RichEditor) findViewById(R.id.editor);
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(22);
        mEditor.setEditorFontColor(Color.RED);
        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("Insert text here...");
        //mEditor.setInputEnabled(false);

        findViewById(R.id.action_add_mac).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View pop=View.inflate(EditActivity.this,R.layout.popup,null);
                final Dialog dialog=new Dialog(EditActivity.this);
                dialog.setContentView(pop);
                dialog.setTitle("Enter Password");
                dialog.setCancelable(true);

                final EditText editText=(EditText)pop.findViewById(R.id.set_password);
                final EditText editName=(EditText)pop.findViewById(R.id.set_name);
                pop.findViewById(R.id.skip_p).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name=editName.getText().toString();
                        if(name==null||name.equals("")){
                            Toast.makeText(EditActivity.this,"Enter name.",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(!name.endsWith(".pcx"))name+=".pcx";
                        saveToFile("00000000",name);
                        dialog.dismiss();
                    }
                });
                pop.findViewById(R.id.set_p).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String name=editName.getText().toString();
                            if(!name.endsWith(".pcx"))name+=".pcx";
                            String password = editText.getText().toString();
                            saveToFile(password,name);
                            dialog.dismiss();
                        }catch (Exception e){
                            Log.write(e.getLocalizedMessage());
                        }
                    }
                });
                dialog.show();
            }
        });
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditActivity.this.onBackPressed();
            }
        });
        mPreview = (TextView) findViewById(R.id.preview);
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override public void onTextChange(String text) {
                mPreview.setText(text);
                data=text;
            }
        });

        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.redo();
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override public void onClick(View v) {
                mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override public void onClick(View v) {
                mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
               showFileChooser();
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.insertLink("https://google.com", "google");
            }
        });
        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.insertTodo();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MAC && resultCode == RESULT_OK && data != null && data.getData() != null) {
            macFile=AndroidUtilities.getPath(data.getData());
            if(macFile==null){
                Toast.makeText(EditActivity.this,"Invalid file try again",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(EditActivity.this,"Successfully added file",Toast.LENGTH_LONG).show();
                findViewById(R.id.action_add_mac).setVisibility(View.GONE);
            }
            return;

        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            String path=AndroidUtilities.getPath(filePath);
            if(path==null){
                Toast.makeText(EditActivity.this,"Invalid file try again",Toast.LENGTH_LONG).show();
            }
            File img=new File(path);
            try {
                Bitmap bitmap= BitmapFactory.decodeFile(path);
                String b64= com.hackathon.textEditor.Utils.toBase64(bitmap);
                String type="data:img/png"+";base64,";
                mEditor.insertImage(type,b64,
                        "image");

            } catch (Exception e) {
                Log.write(e.getMessage());
            }
        }
    }
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/png");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    private void showMACChooser() {
        Intent intent = new Intent();
        intent.setType("text/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File with mac address and gmail account"), PICK_MAC);
    }

    private void saveToFile(final String password, final String name){
        final ProgressDialog dialog=new ProgressDialog(EditActivity.this);
        dialog.setTitle("Saving");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setMessage("Please Wait...");
        dialog.show();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File cache, inputFile;
                    final File r = new File("/sdcard/demo");
                    r.mkdirs();
                    inputFile = new File(r, name);
                    try {
                        cache = AppFolderMaker.createCacheFolderFor(EditActivity.this, "demo");
                        cache.mkdirs();
                        inputFile.createNewFile();
                    } catch (Exception e) {

                    }
                    Log.write("Starting encoder");
                    PCXEncoderCore pcxEncoder = new PCXEncoderCore(Charset.defaultCharset().displayName(), inputFile.getAbsolutePath(), password);
                    Log.write("Initialised encoder\n");
                    if(macFile!=null){
                        List<String> fls=new ArrayList<>();
                        fls.add(macFile);
                        PCXEncoderCore.Script script= new PCXEncoderCore.Script(fls,".txt");
                        List<PCXEncoderCore.Script> scripts=new ArrayList<>();
                        scripts.add(script);
                        pcxEncoder.addFrameData(data,scripts,(byte)0);
                    }
                    else pcxEncoder.addFrameData(data, null, (byte) 0);
                    pcxEncoder.saveAllFrames();
                    pcxEncoder.closeFile();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            Toast.makeText(EditActivity.this,"Saved File",Toast.LENGTH_LONG).show();
                            EditActivity.this.finish();
                        }
                    });
                }catch (Exception e){
                    Log.write(e.getMessage());
                }
            }
        });
        thread.start();
    }
}

