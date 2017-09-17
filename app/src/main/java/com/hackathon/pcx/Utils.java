package com.hackathon.pcx;

import java.nio.charset.Charset;

/**
 * Created by INFIi on 3/1/2017.
 */

public class Utils {
    public static final String KEY="hackathon";
    final  static long size=0;
    static final String HEADER="PCX-1";
    static final String VERSION="0.9";
    static final String LAYOUT_FLAGS_CODE="LFC";
    static final String ENCODING= "UTF-8     ";
    static final String PROTECTION_PASSWORD="00000000";

    static final String START_OF_PWD="SPW";
    static final String END_OF_PWD="EPW";

    //Frame count SOS len EOS
    //FRame structure
    static final String START_OF_FRAME="SOF";

    static final String START_OF_SIZE="SOS";
    static final String SIZE=String.valueOf(size);
    static final String END_OF_SIZE="EOS";
    //local layout flag 32 bit
    static final int LOCAL_LAYOUT_ID_DEFAULT=0;                 //16 bit value max 2^16 layout support
    static final boolean USE_LOCAL_LAYOUT_BG_COLOR=false;        //1 bit
    static final boolean USE_LOCAL_LAYOUT_DEFAULT=false;        //1 bit
    static final boolean USE_LOCAL_TEXT_SIZE_DEFAULT=false;     //1 bit
    static final boolean USE_LOCAL_TEXT_COLOR=false;            //1 bit
    static final boolean USE_LOCAL_FONT_SIZE=false;             //1 bit
    static final boolean USE_LOCAL_TEXT_ANIMATION=false;        //1 bit
    static final boolean USE_LOCAL_IMAGE_ANIMATION=false;       //1 bit
    static final boolean USE_LOCAL_WATERMARK=false;             //1 bit
    static final byte RESERVED_LLFC_BITS=(byte)0;             // 8 bits


    static final String START_OF_FRAME_DATA="SFD";
    static final String COMPRESSION_ALGO_CODE="0";
    static final String START_OF_TEXT_DATA="STD";          //64 bits
    static final String END_OF_TEXT_DATA="ETD";            //64 bits
    static final String START_OF_SCRIPT_DATA="SCD";
    static final String START_OF_SCRIPT="SCR";
    static final String START_OF_PATH="SCP";
    static final String END_OF_PATH="ECP";
    static final String END_OF_SCRIPT="ECR";
    static final String END_OF_SCRIPT_DATA="ECD";
    static final String END_OF_FRAME_DATA="EFD";

    static final String END_OF_FRAME="EOF";

    public static byte[] getHeader(String encoding){

        byte[] bytes=(Utils.HEADER+Utils.VERSION+Utils.ENCODING+Utils.START_OF_PWD+Utils.PROTECTION_PASSWORD+Utils.END_OF_PWD).getBytes();
        return bytes;
    }
    public static byte[] getHeader(String encoding,String password){
        byte[] bytes=(Utils.HEADER+Utils.VERSION+Utils.ENCODING+START_OF_PWD+password+END_OF_PWD).getBytes();
        return bytes;
    }
    public static byte[] getFrameHeader(long size,String encoding){

        byte[] bytes=(Utils.START_OF_FRAME+Utils.START_OF_SIZE+size+Utils.END_OF_SIZE).getBytes(Charset.forName(encoding));
        return bytes;
    }
    public static String getFrameDataHeader(int compressionCode){
        return Utils.START_OF_FRAME_DATA+Utils.COMPRESSION_ALGO_CODE+compressionCode;
    }


}
