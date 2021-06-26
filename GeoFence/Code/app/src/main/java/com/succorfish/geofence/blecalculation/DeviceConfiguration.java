package com.succorfish.geofence.blecalculation;

import java.nio.ByteBuffer;

import static com.succorfish.geofence.blecalculation.Blecalculation.into2Bytes;
import static com.succorfish.geofence.blecalculation.ByteConversion.convertToTwoBytes;

public class DeviceConfiguration {
    public static byte[] sendIntervalSeconds( int gsmInterval,
                                              int gsmTimeOut,
                                              int gpsInterval,
                                              int gpstimeout,
                                              int sateliteInterval,
                                              int sateliteTimeout,
                                              int cheapestModeMulitplier){
        byte [] intervalArray=new byte[16];
        byte [] gsmInterval_araay=convertToTwoBytes(gsmInterval);
        byte [] gsmTimeOut_araay=convertToTwoBytes(gsmTimeOut);
        byte [] gpsInterval_araay=convertToTwoBytes(gpsInterval);
        byte [] gpstimeout_araay=convertToTwoBytes(gpstimeout);
        byte [] sateliteInterval_araay=convertToTwoBytes(sateliteInterval);
        byte [] sateliteTimeout_araay=convertToTwoBytes(sateliteTimeout);
        byte totalLength= (byte) (
                gsmInterval_araay.length+
                gsmTimeOut_araay.length+
                gpsInterval_araay.length+
                gpstimeout_araay.length+
                sateliteInterval_araay.length+
                sateliteTimeout_araay.length+1);//HardCode as 1 because opcode takes 1 byte.
        //(for Opcode Constant one value)
        intervalArray[0]= (byte) 0xc1;//command
        intervalArray[1]= (byte) ((byte) totalLength);
        intervalArray[2]= (byte) 0x01;//updcode
        int counter=3;
        for (int i = 0; i <gsmInterval_araay.length ; i++) {
            intervalArray[counter]=gsmInterval_araay[i];
            counter++;
        }
        for (int i = 0; i <gsmTimeOut_araay.length ; i++) {
            intervalArray[counter]=gsmTimeOut_araay[i];
            counter++;
        }

        for (int i = 0; i <gpsInterval_araay.length ; i++) {
            intervalArray[counter]=gpsInterval_araay[i];
            counter++;
        }
        for (int i = 0; i <gpstimeout_araay.length ; i++) {
            intervalArray[counter]=gpstimeout_araay[i];
            counter++;
        }
        for (int i = 0; i <sateliteInterval_araay.length ; i++) {
            intervalArray[counter]=sateliteInterval_araay[i];
            counter++;
        }
        for (int i = 0; i <sateliteTimeout_araay.length ; i++) {
            intervalArray[counter]=sateliteTimeout_araay[i];
            counter++;
        }
        intervalArray[intervalArray.length-1]= (byte) cheapestModeMulitplier;
        return  intervalArray;
    }

    public static byte [] sendCheapsetModeRadioButtonValues(byte cheapestMode,
                                                            byte ultraLowPowerMode,
                                                            byte USBDownloadMode,
                                                            byte inridiumAlwaysOn,
                                                            byte iridiumEventsOn,
                                                            byte instantTamper,
                                                            byte wayPointToMovement){
        byte [] cheapestModeArray=new byte[16];
        cheapestModeArray[0]= (byte) 0xc1;//command
        cheapestModeArray[1]= (byte)   0x08;//data length
        cheapestModeArray[2]= (byte)0x02;//updcode
        cheapestModeArray[3]= (byte) cheapestMode;
        cheapestModeArray[4]= (byte) ultraLowPowerMode;
        cheapestModeArray[5]= (byte) USBDownloadMode;
        cheapestModeArray[6]= (byte) inridiumAlwaysOn;
        cheapestModeArray[7]= (byte) iridiumEventsOn;
        cheapestModeArray[8]= (byte) instantTamper;
        cheapestModeArray[9]= (byte) wayPointToMovement;
        return cheapestModeArray;
    }


    /**
     * Demo code to convert the int values to specific bytes.
     */
    public static String getHexValString(Integer val, int bytePercision){
        //  System.out.println("hex val of 260 in 4 bytes = " + getHexValString(1,2).replace(" ",""));
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(val));

        while(sb.length() < bytePercision*2){
            sb.insert(0,'0');// pad with leading zero
        }

        int l = sb.length(); // total string length before spaces
        int r = l/2; //num of rquired iterations

        for (int i=1; i < r;  i++){
            int x = l-(2*i); //space postion
            sb.insert(x, ' ');
        }
        return sb.toString().toUpperCase().replace(" ","");
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public static byte[] toBytes(short s) {
        return new byte[]{(byte)(s & 0x00FF),(byte)((s & 0xFF00)>>8)};
    }

    public static  byte [] sendData(){
        byte [] byteValue=new byte[16];
        String s="C11001FFFFFFFFFFFFFF";
        byteValue=s.getBytes();
        return byteValue;
    }

}


/**
 * commit code requires codde cleaing...
 */
