package com.hackathon.pcx;

import android.os.Environment;

import com.hackathon.AndroidUtilities;
import com.hackathon.AppFolderMaker;
import com.hackathon.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by INFIi on 3/14/2017.
 */

public class PCXDecoderCore {

    private FileInputStream fis;
    private String input;
    private String encoding;
    private String password;
    private int compressionCode;
    private DecodedFrame decodedFrame;
    private long frameCount;
    private File cache;
    public PCXDecoderCore(String input,File cacheDir) throws Exception{
        File tmp=new File(AppFolderMaker.getCacheDirectory(AndroidUtilities.context),"cp.dat");
        if(tmp.exists())tmp.delete();
        byte[] b=AndroidUtilities.decryptFile(Utils.KEY,input);
        FileOutputStream tfs=new FileOutputStream(tmp);
        tfs.write(b,0,b.length);
        this.input= Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp.dat";
        this.cache =cacheDir;
        Log.write("Input File is:"+input+"\nTmp File"+this.input);
        PCXDecompressor.decompressFile(tmp,this.input);
        Log.write("Decompressed");
        fis=new FileInputStream(this.input);
        Log.write("Loaded file");
        decodedFrame=new DecodedFrame();

        byte[] extension=new byte[5];
        fis.read(extension,0,5);
        Log.write("Reading extension:"+new String(extension,Charset.defaultCharset()));

        if(!new String(extension,Charset.defaultCharset()).equals(Utils.HEADER)){
            throw new UnsupportedEncodingException("Unsupported file format");
        }
        fis.skip(3);
        byte[] enc=new byte[10];                        //encoding length 10bytes
        fis.read(enc,0,enc.length);
        encoding=new String(enc,Charset.defaultCharset()).replaceAll("\\s","");     //remove padded whitespaces
        Log.write("Encoding is;"+encoding);
        fis.skip(3);
        byte[] ps=new byte[8];
        fis.read(ps,0,8);
        password=new String(ps,Charset.defaultCharset());
        Log.write("password is :"+password);
        //fis.skip(8);                                    //skip password for now TODO: get the encrypted password and decrypt it from jni call using conceal.
        fis.skip(6);
        frameCount=getSize();
        Log.write("FrameCount:"+frameCount);
        fis.skip(9);                                    //skip EOS,start of frame and frame size next bytes are frame length until we encounter EOS
        tmp.delete();
    }

    private String getPasswordString(){
        try {
            byte[] bytes = new byte[1];
            List<Byte> valueBytes = new ArrayList<>();
            bytes = new byte[8];
            fis.read(bytes, 0, 8);
            String op = new String(bytes, Charset.defaultCharset());
            if (op.equals(Utils.PROTECTION_PASSWORD)) {
                return op;
            } else {

                Log.write(new String(bytes, Charset.defaultCharset()));

                boolean fe=false,fp=false,fw=false;
                while (!(fe&&fp&&fw)) {
                    bytes = new byte[1];
                    fis.read(bytes, 0, 1);
                    if(fe){
                        if(bytes[0]==(byte)'P')fp=true;
                        else {
                            fe=false;
                            valueBytes.add((byte)'E');
                        }
                    }
                    if(fe&&fp){
                        if(bytes[0]==(byte)'W'){
                            fw=true;
                            continue;
                        }
                        else {
                            fp=false;fe=false;
                            valueBytes.add((byte)'E');
                            valueBytes.add((byte)'P');
                        }
                    }
                    if (bytes[0] != (byte) 'E') {
                        valueBytes.add(bytes[0]);
                        Log.write(new String(new byte[]{valueBytes.get(valueBytes.size() - 1)}, Charset.defaultCharset()));
                    }else {
                        fe=true;
                    }
                }
                //fis.skip(2);
                byte[] valB = new byte[valueBytes.size()];
                for (int i = 0; i < valueBytes.size(); i++) {
                    valB[i] = valueBytes.get(i);
                }
                String re = new String(valB, Charset.defaultCharset());
                Log.write("Long val:s" + re);

                return re;
            }
        }catch (Exception e){
            Log.write(e.getMessage());
        }
        return "00000000";
    }
    public boolean hasPassword(){
        return !password.equals(Utils.PROTECTION_PASSWORD);
    }
    public String getPassword() {
        return password;
    }

    public long getFrameCount() {
        return frameCount;
    }

    public String getEncoding() {
        return encoding;
    }

    public DecodedFrame getNextFrame(DecodedFrame decodedFrame) throws Exception{

        decodedFrame=getFrameMetaData(decodedFrame);
        decodedFrame=decodeText(decodedFrame);
        /*decodedFrame=decodeImage(decodedFrame);
        decodedFrame=decodeGif(decodedFrame);
        decodedFrame=decodeVideo(decodedFrame);
        decodedFrame=decodeAudio(decodedFrame);*/
        decodedFrame=decodeScipts(decodedFrame);
        return decodedFrame;

    }
    public void closeFile(){
        try{
            fis.close();
            File f=new File("/sdcard/tmp.dat");
            f.delete();
        }catch (Exception e){}
    }
    public DecodedFrame getFrame(int n) throws Exception{
            DecodedFrame decodedFrame=new DecodedFrame();
            moveToFrame(n);
            return getNextFrame(decodedFrame);
    }
    public void moveToFrame(int n) throws Exception{

        fis=new FileInputStream(input);

        byte[] extension=new byte[5];
        fis.read(extension,0,extension.length);

        if(!new String(extension).equals(Utils.HEADER)){
            throw new UnsupportedEncodingException("Unsupported file format.Not a PCX File");
        }
        fis.skip(3);
        skip(10);
        fis.skip(3);                                    //skip password for now
        //getPasswordString();
        fis.skip(8);
        fis.skip(6);
        frameCount=getSize();
        Log.write("SkipTo :"+n);
        Log.write("Calculated frameCount"+frameCount);
        fis.skip(6);                                    //skip comprCode,start of frame and frame size next bytes are frame length until we encounter EOS

        for(int i=0;i<n;i++) {
            skip(getSize());
            skip(6);
        }


    }
    private DecodedFrame decodeText(DecodedFrame decodedFrame) throws Exception{
        decodedFrame.textLength=getSize();
        if(decodedFrame.textLength==0){
            decodedFrame.text="";
            return decodedFrame;
        }
        Log.write("Text Length"+(int)decodedFrame.textLength);
        byte[] textBytes=new byte[(int)decodedFrame.textLength];
        fis.read(textBytes,0,textBytes.length);
        String s=new String(textBytes,Charset.forName(encoding));
        decodedFrame.text=s;
        Log.write("Text is"+s);
        skip(9);

        return decodedFrame;
    }
    private DecodedFrame getFrameMetaData(DecodedFrame decodedFrame) throws Exception{
        decodedFrame.frameLength=getSize();
        Log.write("Frame Length"+decodedFrame.frameLength);
        skip(3);
        byte[] bytes=new byte[2];
        fis.read(bytes,0,2);
        decodedFrame.compressionCode=bytes[0];
        Log.write("Compression Code"+new String(bytes,Charset.defaultCharset()));
        skip(6);
        return decodedFrame;

    }

    private DecodedFrame decodeScipts(DecodedFrame decodedFrame) throws Exception {
        String s=getSizeString();
        if(s.equals("0")){
            decodedFrame.scriptsCount=0;
            decodedFrame.scriptsSize=0;
            decodedFrame.scripts=new ArrayList<>();
            skip(12);
            return decodedFrame;
        }
        String[] longs=s.split("//_//");
        Log.write("Scripts split "+longs.length);
        if(longs.length<2)throw new Exception("Could not get length and count of script");
        decodedFrame.scriptsSize=Long.parseLong(longs[0]);
        decodedFrame.scriptsCount=Long.parseLong(longs[1]);
        skip(6);
        for(int i=0;i<decodedFrame.scriptsCount;i++) {
            String type=getSizeString();
            File f = createFileFromBytes(cache.getAbsolutePath()+"/"+type);
            if (f != null) decodedFrame.scripts.add(f);
            skip(9);
        }
        skip(3);
        Log.write("Done all scripts");
        return decodedFrame;

    }



    private File createFileFromBytes(String output) throws Exception{

        long size=getSize();
        if(size==0)return null;
        int inc=1;
        List<byte[]> fileData=new ArrayList<>();
        int max=100000000;
        int it=(int)size/(max);
        int rem=(int)size%(max);
        for(int i=0;i<it;i++){
            byte[] bytes=new byte[max];
            fis.read(bytes,0,bytes.length);
            fileData.add(bytes);
        }
        if(rem!=0){
            byte[] bytes=new byte[rem];
            fis.read(bytes,0,bytes.length);
            fileData.add(bytes);
        }
        File img=new File(output);
        if(img.exists())img.delete();
        img.createNewFile();
        FileOutputStream fos=new FileOutputStream(img);
        for(byte[] b:fileData){
            fos.write(b,0,b.length);
        }
        return img;

    }
    private void skip(long n){
        if(n<1)return;
        long s;
        try{
            s=fis.skip(n);
        }catch (Exception e){
            s=-1;
        }
        if(s==-1||s!=n){
            for(int i=0;i<n;i++){
                try {
                    fis.read();
                }catch (Exception e){

                }
            }
        }
    }
    private String getSizeString() throws Exception {
        byte[] bytes=new byte[1];
        List<Byte> valueBytes=new ArrayList<>();
        bytes=new byte[1];
        fis.read(bytes,0,1);

        if(bytes[0]==(byte)'E'){
            skip(2);
            return "0";
        }
        else valueBytes.add(bytes[0]);

        Log.write(new String(bytes,Charset.defaultCharset()));

        while (bytes[0]!=(byte)'E'){
            bytes=new byte[1];
            fis.read(bytes,0,1);
            if(bytes[0]!=(byte)'E') {
                valueBytes.add(bytes[0]);
                Log.write(new String(new byte[]{valueBytes.get(valueBytes.size()-1)}, Charset.defaultCharset()));
            }
        }
        fis.skip(2);
        byte[] valB=new byte[valueBytes.size()];
        for(int i=0;i<valueBytes.size();i++){
            valB[i]=valueBytes.get(i);
        }
        String re=new String(valB, Charset.defaultCharset());
        Log.write("Long val:"+re);

        return re;

    }

    private long getSize() throws Exception{
        return Long.parseLong(getSizeString());
    }
}
