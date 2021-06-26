package com.succorfish.geofence.blecalculation;
import static com.succorfish.geofence.blecalculation.ByteConversion.convertIntegertTWOBytes;

public class ServerConfiguration {
    public static byte [] startFristPacket_ServerConfiguration(int totalNumberOfPackets_ServerAddress,
                                                               int serverPort,
                                                               int keepIntervalAlive)
    {
        byte[] serverPortArray=convertIntegertTWOBytes(serverPort);
        byte[] startpacket=new byte[16];
        startpacket[0]= (byte) 0XC4;//COMMAND
        /**
         * data length
         * -----------
         * opcode=1
         * TotalNumber of Server Packets(Message Packets)=totalNumberOfPackets_ServerAddress
         * Server Port=2
         * Keeep aliveInterval=1
         */
        int totalLength=1+totalNumberOfPackets_ServerAddress+2+1;
        startpacket[1]= (byte) totalLength;//data length
        startpacket[2]= (byte) 0x01;//OPCODE
        startpacket[3]= (byte) totalNumberOfPackets_ServerAddress;
        int indexStart=4;
        for (int i = 0; i <serverPortArray.length ; i++) {
            startpacket[4]=serverPortArray[i];
            indexStart++;
        }
        int keepAliverTervalPosition=serverPortArray.length+4;
        startpacket[keepAliverTervalPosition]= (byte) keepIntervalAlive;

        return startpacket;
    }

    public static byte [] serverConfiguration_ServerPacket(int packetNumber, String dataToBePassed){
        byte[] individualDataArray = dataToBePassed.getBytes();
        byte[] serverPacket=new byte[16];
        serverPacket[0]= (byte) 0XC4;//command

        /**
         * data length
         * opCode
         * packet Number
         * Server address
         */
        int totalLength= 1+1+individualDataArray.length;
        serverPacket[1]= (byte) totalLength;//command
        serverPacket[2]= (byte) 0x02;//command
        serverPacket[3]= (byte) packetNumber;//command
        int dataToBeFilledPosition=4;
        for (int i = 0; i < individualDataArray.length; i++) {
            serverPacket[dataToBeFilledPosition]=individualDataArray[i];
            dataToBeFilledPosition++;
        }
        return serverPacket;
    }
}
