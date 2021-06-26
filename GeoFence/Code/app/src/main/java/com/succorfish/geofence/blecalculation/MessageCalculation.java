package com.succorfish.geofence.blecalculation;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import static com.succorfish.geofence.blecalculation.Blecalculation.intToBytes;
import static com.succorfish.geofence.blecalculation.ByteConversion.convert_TimeStampTo_4bytes;
import static com.succorfish.geofence.utility.Utility.ConvertStringToByteArray;

public class MessageCalculation {
    /*public static int countNumberofTextMessages(String totoalNumberOfChacters){
        int count=totoalNumberOfChacters.length();
        double exact_value=count/12;
        double entireValue=Math.ceil(exact_value);
        int total_Number_charcter= (int) entireValue;
        return total_Number_charcter;
    }*/

    public static byte [] startMessagepacket_message(int totalNumber_Of_MessagePacket,int total_String_Length_TextMessages,String timeStamp,String SequenceNumber){
        /**
         * filing time stamp and Sequence number to 4 bytes.
         */
        byte [] startPacket=new byte[16];
        byte [] timeStampArray=convert_TimeStampTo_4bytes(Integer.parseInt(timeStamp));
        byte [] sequenceArray=convert_TimeStampTo_4bytes(Integer.parseInt(SequenceNumber));
        byte command= (byte) 0xb1;
        byte upcode= (byte) 0x01;
        /**
         * opcode
         * total number of message packets
         * total length of messagetext
         * timeStamp
         * timeSequence
         * Note:- HardCoded as 8 bcoz of timestamp and SequenceNumber.
         */
            byte data_length= (byte) 0x0b;
        startPacket[0]=(byte)command;// command
        startPacket[1]=data_length;// datalength
        startPacket[2]=upcode;// upcode
        startPacket[3]= (byte) totalNumber_Of_MessagePacket;//total number of message packets
        startPacket[4]= (byte) total_String_Length_TextMessages;//total length of text message
        /**
         * Adding TimeStamp to the startPacket Array.
         */
        int Start_Adding_TimeStamp_Array=5;
        for (int i = 0; i <timeStampArray.length ; i++) {
            startPacket[Start_Adding_TimeStamp_Array]=timeStampArray[i];
            Start_Adding_TimeStamp_Array++;
        }
        /**
         * Adding Sequence Number to the startPacket Array
         */
        int start_AddingSequenceArray_Index=5+timeStampArray.length;
        for (int i = 0; i <sequenceArray.length ; i++) {
            startPacket[start_AddingSequenceArray_Index]=sequenceArray[i];
            start_AddingSequenceArray_Index++;
        }
        return  startPacket;
    }

    public static byte [] endMessagePacket(int totoalDataPacket,String GSM_IRIDIUM){
        /**
         * totoalDataPacket= total message packets+1
         */
        byte [] endPacket=new byte[16];
        endPacket[0]=(byte) 0xb1;// command
        endPacket[1]=(byte) 0x02;// Data Length
        endPacket[2]=(byte) 0x03;// Upcode
        if(GSM_IRIDIUM.equalsIgnoreCase("GSM")){
            endPacket[3]=(byte) 0x01;// GSM
        }else {
            endPacket[3]=(byte) 0x02;// IRIDIUM
        }
        return endPacket;
    }


    public static byte [] messageDataArray(int packetNumber,int String_length,String dataToBepassed){
        byte[] individualDataArray = dataToBepassed.getBytes();
        byte [] message_array=new byte[16];
        message_array[0]=(byte)0xb1;//command
        /**
         * length:-
         * opcode
         * packet number
         * message
         * Hard coding it as 3 as its fixed.
         */
        byte length= (byte) (1+1+individualDataArray.length);
        message_array[1]=(byte)length;//length
        message_array[2]=(byte)0x02;//Upcode
        message_array[3]= (byte)packetNumber;//packet number
        int index_FilledTo_MessageArray=4;
        for (int i = 0; i <individualDataArray.length ; i++) {
            message_array[index_FilledTo_MessageArray]=individualDataArray[i];
            index_FilledTo_MessageArray++;
        }
        return message_array;
    }

    public static byte [] incommingMessageACK(String sequenceNumber,byte channelId,byte response){

        byte [] sequenceArray=convert_TimeStampTo_4bytes(Integer.parseInt(sequenceNumber));
        byte [] message_array=new byte[16];
        message_array[0]=(byte)0xb2;//command
        /**
         * Data length is fixed i.e 6 bytes.
         * Sequence Number= 4 bytes;
         * Channel ID=1 bytes
         * ACK =1 bytes.
         */
        message_array[1]=(byte)0x06;//command
        int index_FilledTo_MessageArray=2;
        for (int i = 0; i <sequenceArray.length ; i++) {
            message_array[index_FilledTo_MessageArray]=sequenceArray[i];
            index_FilledTo_MessageArray++;
        }

        int positionToAddChannelId=2+sequenceArray.length;
        message_array[positionToAddChannelId]=channelId;
        message_array[positionToAddChannelId+1]=response;
             return  message_array;
    }
}
