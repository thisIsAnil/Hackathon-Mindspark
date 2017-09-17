package com.hackathon.pcx;

import com.hackathon.AndroidUtilities;
import com.hackathon.AppFolderMaker;
import com.hackathon.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by INFIi on 3/1/2017.
 */

public class PCXEncoderCore {

    static final int K64=64*1024;
    FileOutputStream fos;
    List<Frame> frames=new ArrayList<>();
    String encoding,output;
    byte[] startBytes;
    public PCXEncoderCore(String encoding, String output){
        this.encoding=Charset.isSupported(encoding)?encoding:Charset.defaultCharset().displayName();
        this.output=output;
        try {
            File f=new File(output);
            f.createNewFile();
            fos=new FileOutputStream(output);
            startBytes=Utils.getHeader(encoding);
        }catch (Exception e){
            Log.write(e.getMessage());}
       // strings.add(s.getBytes());
    }
    public PCXEncoderCore(String encoding, String output,String password){
        this.encoding=Charset.isSupported(encoding)?encoding:Charset.defaultCharset().displayName();
        this.output=output;
        try {
            Log.write("password len: "+password.length());
            File f=new File(output);
            f.createNewFile();
            fos=new FileOutputStream(output);
            startBytes=Utils.getHeader(encoding,password);
        }catch (Exception e){
            Log.write(e.getMessage());}
        // strings.add(s.getBytes());
    }

    //general format to store any kind of data is data length->data->data.footer where data can text, list of images,videos,gifs etc
    public void addFrameData(String text,List<Script> scripts,byte compressionCode) throws Exception{
        Frame frame=new Frame();

        frame.compressionCode=compressionCode;
    long size=text==null?(long)0:(long)text.getBytes().length;
        frame.textSize=size;
        if(text!=null){
            frame.text=text;
            Log.write(text+"\n");
        }
        frame.textFooter=Utils.END_OF_TEXT_DATA.getBytes();
        Log.write("TextData Added");
        frame.scriptsSize=scripts==null?0:scripts.size();
        if(scripts!=null){
            frame.scripts=getDataBytesFromScripts(scripts);
        }
        frame.scriptsFooter=(Utils.END_OF_SCRIPT_DATA).getBytes(Charset.forName(encoding));
        frame.frameDataFooter=(Utils.END_OF_FRAME_DATA).getBytes(Charset.forName(encoding));
        frame.frameFooter=(Utils.END_OF_FRAME).getBytes(Charset.forName(encoding));
        Log.write("Frame Data Written\nAppending Header");

        frame=appendHeaders(frame);
        Log.write("Header Appended");
        frames.add(frame);
    }
    private Frame appendHeaders(Frame frame) throws Exception{
        long[] sizes=getFrameSize(frame);
        Log.write("Calculated all sizes");
        String frameData=Utils.getFrameDataHeader(frame.compressionCode);
        frame.framedataHeader=frameData.getBytes();

        frameData=Utils.START_OF_TEXT_DATA+Utils.START_OF_SIZE+sizes[1]+Utils.END_OF_SIZE;
        frame.textHeader=frameData.getBytes();
        Log.write("Text Header Appended:"+new String(frame.textHeader,Charset.defaultCharset()));


        frame.scriptsHeader=getHeaderFor(Utils.START_OF_SCRIPT_DATA,sizes[6],frame.scriptsSize);
        long hlen=computeFrameHeadersLength(frame)+sizes[0];
        frame.frameHeader =Utils.getFrameHeader(hlen,encoding);
        Log.write("Frame Len="+new String(frame.frameHeader,Charset.defaultCharset()));
        return frame;


    }
    private byte[] getHeaderFor(String h_start,long length,long size){
        return (h_start+Utils.START_OF_SIZE+length+"//_//"+size+Utils.END_OF_SIZE).getBytes();
    }
    private long computeFrameHeadersLength(Frame f){
        return f.framedataHeader.length+f.textHeader.length
                //+f.imageHeader.length+f.gifHeader.length+f.videoHeader.length+f.audioHeader.length
                + f.frameDataFooter.length +f.textFooter.length
                //+f.imageFooter.length+f.gifFooter.length+f.videoFooter.length+f.audioFooter.length
                +f.scriptsHeader.length+f.scriptsFooter.length+f.frameFooter.length;
    }
    public void closeFile(){
        try{
            fos.close();
        }catch (Exception e){}
    }
    public void saveAllFrames() throws Exception{
        File fs=new File(AppFolderMaker.getCacheDirectory(AndroidUtilities.context),"tmp.dat");
        if(fs.exists())fs.delete();
        fs.createNewFile();
        fos=new FileOutputStream(fs);
        fos.write(startBytes,0,startBytes.length);
        byte[] frameCount=(Utils.START_OF_SIZE+frames.size()+Utils.END_OF_SIZE).getBytes();
        fos.write(frameCount,0,frameCount.length);
        Log.write("Frame Count:"+new String(frameCount,Charset.defaultCharset()));
        for(Frame f:frames)writeFrameToFile(f);
        Log.write("Written all frame.\nsending to compressor");
        File fs2=new File(AppFolderMaker.getCacheDirectory(AndroidUtilities.context),"cp.dat");
        if(fs2.exists())fs2.delete();
        PCXCompressor.compressFile(fs,fs2.getAbsolutePath());
        Log.write("Compressed successfully");
        FileInputStream fis=new FileInputStream(fs2.getAbsoluteFile());
        AndroidUtilities.encryptFile(Utils.KEY,fis,(int)fs2.length(),output);
        fs.delete();
        fs2.delete();

    }
    private void writeFrameToFile(Frame frame) throws Exception{
            fos.write(frame.frameHeader,0,frame.frameHeader.length);
            fos.write(frame.framedataHeader,0,frame.framedataHeader.length);
            fos.write(frame.textHeader);
            byte[] texts=frame.text.getBytes(Charset.forName(encoding));
            fos.write(texts);
            Log.write("Text written to file:");
            fos.write(frame.textFooter);
            Log.write("Text Footer Written");
            fos.write(frame.scriptsHeader);
            for(int i=0;i<frame.scripts.size();i++){
                Databytes db=frame.scripts.get(i);
                Log.write("Script File"+i+"Contains"+db.bytes.size()+"byte[]\n");
                for(int j=0;j<db.bytes.size();j++){
                    fos.write(db.bytes.get(j));
                }
            }
            Log.write("Script Data Written");
            fos.write(frame.scriptsFooter);
            fos.write(frame.frameDataFooter);
            fos.write(frame.frameFooter);
            Log.write("Frame Written Completely");
    }
    private long[] getFrameSize(Frame f){
        long i_size=0;long v_size=0;long g_size=0;long a_size=0;long s_size=0;
        long size=0;
        long t_size=f.textSize;

        s_size=computeSizeFromList(f.scripts);
        size=i_size+g_size+v_size+a_size+t_size+s_size;

        return new long[]{size,t_size,i_size,g_size,v_size,a_size,s_size};
    }
    private long computeSizeFromList(List<Databytes> f){
        long i_size=0;
        if(f==null||f.size()==0)return 0;
        for(int i=0;i<f.size();i++){
            Databytes db=f.get(i);
            i_size+=db.size();
        }
        return i_size;
    }
    private List<Databytes> getDataBytesFromScripts(List<Script> scripts){
        List<Databytes> databytes=new ArrayList<>();
        for(Script s:scripts){
            databytes.add(getDataByteFromScript(s));
        }
        return databytes;
    }
    private Databytes getDataByteFromScript(Script script){
            Databytes databytes = new Databytes();
            long len = 0;
            for (String s : script.script) {
                databytes.bytes.add(s.getBytes(Charset.forName(encoding)));
            }
            byte[] header = (Utils.START_OF_SCRIPT+Utils.START_OF_PATH + script.type + Utils.END_OF_PATH + Utils.START_OF_SIZE + len + Utils.END_OF_SIZE).getBytes(Charset.forName(encoding));
            databytes.bytes.add(0, header);
            byte[] footer=(Utils.END_OF_SCRIPT).getBytes();
            databytes.bytes.add(footer);
            return databytes;

    }

    private Databytes getDataBytesFromFile(File b,String h_start,String h_end){
        List<byte[]> datas=new ArrayList<>();
        byte[] bytes=null;
        byte[] h=(h_start+Utils.START_OF_SIZE+(long)b.length()+Utils.END_OF_SIZE).getBytes();
        datas.add(h);
        try {
            long len=b.length();
            int max=100000000;
            int loop=(int)(len/max);
            int offset=(int)(len%max);
            Log.write("Loop:"+loop+" Offset:"+offset+"  File Size:"+len);
            FileInputStream fis = new FileInputStream(b);
            for(int i=0;i<loop;i++){
                bytes=new byte[max];
                int id=fis.read(bytes,0,max);
                datas.add(bytes);
            }
            if(offset!=0){
                byte[] bytes1=new byte[offset];
                fis.read(bytes1,0,offset);
                datas.add(bytes1);
            }

        }catch (Exception e){
            Log.write(e.getMessage()+"\nFailed for:"+b.getName());
        }finally {
            h=(h_end).getBytes();
            datas.add(h);

        }
        Databytes databyt=new Databytes();
        databyt.bytes=datas;
        return databyt;

    }
    public class Databytes {
        List<byte[]> bytes=new ArrayList<>();
        public int size(){

            int size=0;
            for(byte[] b:bytes){
                size+=b.length;
            }
            return size;
        }
    }
    public static class Script{
        public List<String> script;
        public String type;
        public Script(List<String> script,String type){
            this.script=script;
            this.type=type;
        }
    }

}
