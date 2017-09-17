package com.hackathon;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Aniket on 16-Sep-17.
 */

public class AndroidUtilities {

    public static Context context=Application.applicationContext;

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            Application.applicationHandler.post(runnable);
        } else {
            Application.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        Application.applicationHandler.removeCallbacks(runnable);
    }
    public static void encryptFile(String key, InputStream fis,int length,String out) throws KeyChainException, CryptoInitializationException, IOException {
        Log.write("Recieved request to encrypt file");
        KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
        Log.write("Key chaim initialised");
        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
        if(!crypto.isAvailable()){
            Log.w("conceal ","Not available");
            return;
        }
        Log.w("conceal ","reading "+length+" bytes from file");
        byte[] value = new byte[length];
        fis.read(value, 0, value.length);
        Log.write("Read from IS");
        File oF=new File(out);
        oF.createNewFile();
        OutputStream cryptoStream = crypto.getCipherOutputStream(new FileOutputStream(oF), Entity.create(key));
        Log.write("Got mac stream");
        cryptoStream.write(value);
        Log.write("Written to mac stream");
        cryptoStream.close();
        fis.close();

    }

    public static byte[] decryptFile(String key, String path) throws KeyChainException, CryptoInitializationException, IOException {
        KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
        FileInputStream fis=new FileInputStream(path);
        InputStream cryptoStream = crypto.getCipherInputStream(fis, Entity.create(key));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while ((read = cryptoStream.read(buffer)) != -1) {
            bout.write(buffer, 0, read);
        }
        cryptoStream.close();
        Log.write("File size is "+bout.toByteArray().length);
        return bout.toByteArray();
    }
    public static byte[] decryptFromAssets(String key, InputStream fis) throws KeyChainException, CryptoInitializationException, IOException {
        KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
        InputStream cryptoStream = crypto.getCipherInputStream(fis, Entity.create(key));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while ((read = cryptoStream.read(buffer)) != -1) {
            bout.write(buffer, 0, read);
        }
        cryptoStream.close();
        Log.write("File size is "+bout.toByteArray().length);
        return bout.toByteArray();
    }
    public static String encryptString(String key, String value) throws KeyChainException, CryptoInitializationException, IOException {
        KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStream cryptoStream = crypto.getCipherOutputStream(bout, Entity.create(key));
        cryptoStream.write(value.getBytes("UTF-8"));
        cryptoStream.close();
        String result = Base64.encodeToString(bout.toByteArray(), Base64.DEFAULT);
        bout.close();
        return result;
    }

    public static String decryptString(String key, String value) throws KeyChainException, CryptoInitializationException, IOException {
        KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
        ByteArrayInputStream bin = new ByteArrayInputStream(Base64.decode(value, Base64.DEFAULT));
        InputStream cryptoStream = crypto.getCipherInputStream(bin, Entity.create(key));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while ((read = cryptoStream.read(buffer)) != -1) {
            bout.write(buffer, 0, read);
        }
        cryptoStream.close();
        String result = new String(bout.toByteArray(), "UTF-8");
        bin.close();
        bout.close();
        return result;
    }
    @SuppressLint("NewApi")
    public static String getPath(final Uri uri) {
        try {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (isKitKat && DocumentsContract.isDocumentUri(Application.applicationContext, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(Application.applicationContext, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    switch (type) {
                        case "image":
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            break;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            split[1]
                    };

                    return getDataColumn(Application.applicationContext, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(Application.applicationContext, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            Log.e(e);
        }
        return null;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                String value = cursor.getString(column_index);
                if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith("file://")) {
                    return null;
                }
                return value;
            }
        } catch (Exception e) {
            Log.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String UriToPath(Uri selectedImageUri){

        String fileManagerString = selectedImageUri.getPath();
        String selectedImage = getPath(selectedImageUri);
        String filePath=null;
        if (fileManagerString != null) {
            filePath = fileManagerString;
        } else if (selectedImage != null) {
            filePath = selectedImage;
        }
        return filePath;
    }

}
