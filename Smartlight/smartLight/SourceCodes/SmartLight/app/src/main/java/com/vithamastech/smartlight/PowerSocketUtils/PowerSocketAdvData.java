package com.vithamastech.smartlight.PowerSocketUtils;

public class PowerSocketAdvData {

    private static final int TTL_DATA_LEN = 1;
    private static final int SEQUENCE_NUMBER_LEN = 2;
    private static final int SELF_DEVICE_ID_LEN = 2;
    private static final int DEVICE_ID_LEN = 2;
    private static final int CRC_LEN = 2;
    private static final int OPCODE_LEN = 2;
    private static final int DEVICE_ADDR_LEN = 6;
    private static final int DEVICE_TYPE_LEN = 2;

    private int ttl; // Time to live
    private int sequenceNumber;
    private int selfDeviceId;
    private int deviceId;
    private int crc; // Cyclic Redundancy check - To check data packet integrity
    private int opcode;
    private String deviceAddress;
    private int deviceType;

    public static PowerSocketAdvData parse(byte[] glosmartAdvPacket) {
        PowerSocketAdvData glosmartAdvData = null;
        if (glosmartAdvPacket != null) {
            try {
                int sourceIdx = 0;
                int dataLength = 0;
                int destinationIdx = 0;

                glosmartAdvData = new PowerSocketAdvData();

                // Extract ttl
                sourceIdx = 0;
                dataLength = TTL_DATA_LEN;
                destinationIdx = sourceIdx + dataLength;
                byte[] ttlBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                int ttl = ByteConverter.convertByteArrayToInt(ttlBytes);

                // Extract Sequence Number
                sourceIdx = destinationIdx;
                dataLength = SEQUENCE_NUMBER_LEN;
                destinationIdx = sourceIdx + dataLength;

                byte[] sequenceNumberBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                int sequenceNumber = ByteConverter.convertByteArrayToInt(sequenceNumberBytes);

                // Extract Self Device ID
                sourceIdx = destinationIdx;
                dataLength = SELF_DEVICE_ID_LEN;
                destinationIdx = sourceIdx + dataLength;

                byte[] selfDeviceIdBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                int selfDeviceId = ByteConverter.convertByteArrayToInt(selfDeviceIdBytes);

                // Extract Device ID
                sourceIdx = destinationIdx;
                dataLength = DEVICE_ID_LEN;
                destinationIdx = sourceIdx + dataLength;

                byte[] deviceIDBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                int deviceId = ByteConverter.convertByteArrayToInt(deviceIDBytes);

                // Extract CRC
                sourceIdx = destinationIdx;
                dataLength = CRC_LEN;
                destinationIdx = sourceIdx + dataLength;

                byte[] crcBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                int crc = ByteConverter.convertByteArrayToInt(crcBytes);

                // Extract Opcode
                sourceIdx = destinationIdx;
                dataLength = OPCODE_LEN;
                destinationIdx = sourceIdx + dataLength;

                byte[] opcodeBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                int opcode = ByteConverter.convertByteArrayToInt(opcodeBytes);

                // Extract BluetoothMAC Address
                sourceIdx = destinationIdx;
                dataLength = DEVICE_ADDR_LEN;
                destinationIdx = sourceIdx + dataLength;

                byte[] deviceAddrBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                String deviceAddress = ByteConverter.getHexStringFromByteArray(deviceAddrBytes, true);
                deviceAddress = deviceAddress.replace(" ", ":");

                // Extract Device Type
                sourceIdx = destinationIdx;
                dataLength = DEVICE_TYPE_LEN;
                destinationIdx = sourceIdx + dataLength;

                byte[] deviceTypeBytes = ByteConverter.copyOfRange(glosmartAdvPacket, sourceIdx, destinationIdx);
                int deviceType = ByteConverter.convertByteArrayToInt(deviceTypeBytes);

                glosmartAdvData.ttl = ttl;
                glosmartAdvData.sequenceNumber = sequenceNumber;
                glosmartAdvData.selfDeviceId = selfDeviceId;
                glosmartAdvData.deviceId = deviceId;
                glosmartAdvData.crc = crc;
                glosmartAdvData.opcode = opcode;
                glosmartAdvData.deviceAddress = deviceAddress;
                glosmartAdvData.deviceType = deviceType;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return glosmartAdvData;
    }

    public int getTtl() {
        return ttl;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getSelfDeviceId() {
        return selfDeviceId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getCrc() {
        return crc;
    }

    public int getOpcode() {
        return opcode;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public int getDeviceType() {
        return deviceType;
    }
}