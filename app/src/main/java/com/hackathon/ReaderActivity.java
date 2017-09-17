package com.hackathon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hackathon.pcx.DecodedFrame;
import com.hackathon.pcx.PCXDecoderCore;
import com.hackathon.pcx.Utils;
import com.hackathon.textEditor.EditActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Created by Aniket on 17-Sep-17.
 */

public class ReaderActivity  extends Activity {
    private String pcxPath;
    private File inputFile;
    private File cache;
    private WebView tv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity);
        try {
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                pcxPath = AndroidUtilities.UriToPath(getIntent().getData());
            } else {
                pcxPath = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            }
            if (pcxPath == null) {
                Toast.makeText(this, "Error While Fetching.Try Again", Toast.LENGTH_LONG).show();
                return;
            }
            if (!pcxPath.endsWith(".pcx")) {
                Toast.makeText(this, "Unsupported File Format", Toast.LENGTH_LONG).show();
                return;
            }
            inputFile=new File(pcxPath);

            cache = AppFolderMaker.createCacheFolderFor(ReaderActivity.this,inputFile.getName().substring(0,8) );
            cache.mkdirs();

            findViewById(R.id.more_viewer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createWebPrintJob(tv);
                }
            });
            tv=(WebView)findViewById(R.id.viewer_main);
            tv.getSettings().setBuiltInZoomControls(true);
            tv.getSettings().setDisplayZoomControls(false);
            tv.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {


                }
            });
            tv.getSettings().setJavaScriptEnabled(true);
            final PCXDecoderCore decoder = new PCXDecoderCore(inputFile.getAbsolutePath(), cache);
            if(decoder.hasPassword()){
                View pop=View.inflate(ReaderActivity.this,R.layout.password_enter,null);
                final Dialog dialog=new Dialog(ReaderActivity.this);
                dialog.setContentView(pop,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                dialog.setTitle("Enter Password");
                dialog.setCancelable(false);
                dialog.show();
                final EditText pwd=(EditText)pop.findViewById(R.id.pass_acc);
                pop.findViewById(R.id.get_p).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String pass = pwd.getText().toString();
                            if(pass==null||pass.equals("")){
                                Toast.makeText(ReaderActivity.this, "Password cannot be empty", Toast.LENGTH_LONG).show();
                                pwd.setText("");
                            }else {
                                //pass= new String(Base64.decode(pass,Base64.NO_WRAP));
                                if (pass.equals(decoder.getPassword())) {
                                    decodeFile(decoder);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(ReaderActivity.this, "Invalid Password", Toast.LENGTH_LONG).show();
                                    pwd.setText("");
                                }
                            }
                        }catch (Exception e){
                            Log.w("ReaderActivity#getP",e.getMessage());
                        }
                    }
                });

            }else {
                decodeFile(decoder);
               }
        }catch (Exception e){
            Log.w("ReaderActivity \t",e.getMessage());
        }
    }
    @SuppressLint("NewApi")
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
            PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Document";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
        //mPrintJobs.add(printJob);
    }
    private void decodeFile(final PCXDecoderCore decoder){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Log.write("Decoder Initialised");
                    int n = (int) decoder.getFrameCount();
                    for (int i = 0; i < n; i++) {
                        DecodedFrame decodedFrame = decoder.getFrame(i);
                        extractHtml(decodedFrame);
                    }
                    /*String op = "";
                    for (String s : texts) op += "\n" + s;
                    Log.write("\n\n" + op);
                    String encoding=decoder.getEncoding();
                   // Spanned text= Html.fromHtml(op);
                    tv.loadDataWithBaseURL(null,op,"text/html",encoding,null);*/
                    decoder.closeFile();
                    if(!verifymac()){
                        new AlertDialog.Builder(ReaderActivity.this).setMessage("MAC not verfied.File will be deleted").setTitle("Error").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                        tv.setVisibility(View.GONE);
                    }else {
                        Toast.makeText(ReaderActivity.this,"Verfied Mac",Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.write(e.getMessage());
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.loadUrl("file:///" + cache.getAbsolutePath() + "/main.html");
                        }
                    });
                }

            }
        });
        thread.start();

    }
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
    private boolean verifymac(){
        String[] files=cache.list();
        boolean doesContain=false;
        for(String s:files){
            if(s.endsWith(".txt")){
                doesContain=true;
                try {
                    File in = new File(s);
                    FileInputStream fis = new FileInputStream(in.getAbsoluteFile());
                    byte[] b = new byte[(int) in.length()];
                    fis.read(b, 0, b.length);
                    String data = new String(b, Charset.defaultCharset());
                    if (data.contains(getMacAddr())) {
                        return true;
                    }
                }catch (Exception e){
                    Log.write(e.getMessage());
                }
            }
        }
        return !doesContain;
    }
    private void extractHtml(DecodedFrame decodedFrame) throws Exception{
        String html=decodedFrame.getText();
        //html=AndroidUtilities.decryptString(Utils.KEY,html);
        File htmlsrc=new File(cache,"main.html");
        if(htmlsrc.exists())htmlsrc.delete();
        htmlsrc.createNewFile();
        FileOutputStream fos=new FileOutputStream(htmlsrc);
        byte[] bytes=html.getBytes();
        fos.write(bytes,0,bytes.length);
        fos.close();
        }

}
