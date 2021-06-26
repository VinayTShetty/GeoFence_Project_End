package com.succorfish.geofence.customObjects;
public class IncommingMessagePacket {
    private String command;
    private String dataLength;
    private String opCode;
    private String totalNumberOfPackets;
    private String totalLengthOfTextmessage;
    private String timeStamp;
    private String sequenceNumber;
    private String messagepacketCommand;
    private String messagePacketDataLength;
    private String messagePacketopCode;
    private String messagePacketNumber;
    private String messageData;
    private String endPacektCommand;
    private String endpacketDatalength;
    private String endpacketUpcode;
    private String endpacketChannelID;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDataLength() {
        return dataLength;
    }

    public void setDataLength(String dataLength) {
        this.dataLength = dataLength;
    }

    public String getOpCode() {
        return opCode;
    }

    public void setOpCode(String opCode) {
        this.opCode = opCode;
    }

    public String getTotalNumberOfPackets() {
        return totalNumberOfPackets;
    }

    public void setTotalNumberOfPackets(String totalNumberOfPackets) {
        this.totalNumberOfPackets = totalNumberOfPackets;
    }

    public String getTotalLengthOfTextmessage() {
        return totalLengthOfTextmessage;
    }

    public void setTotalLengthOfTextmessage(String totalLengthOfTextmessage) {
        this.totalLengthOfTextmessage = totalLengthOfTextmessage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getMessagepacketCommand() {
        return messagepacketCommand;
    }

    public void setMessagepacketCommand(String messagepacketCommand) {
        this.messagepacketCommand = messagepacketCommand;
    }

    public String getMessagePacketDataLength() {
        return messagePacketDataLength;
    }

    public void setMessagePacketDataLength(String messagePacketDataLength) {
        this.messagePacketDataLength = messagePacketDataLength;
    }

    public String getMessagePacketopCode() {
        return messagePacketopCode;
    }

    public void setMessagePacketopCode(String messagePacketopCode) {
        this.messagePacketopCode = messagePacketopCode;
    }

    public String getMessagePacketNumber() {
        return messagePacketNumber;
    }

    public void setMessagePacketNumber(String messagePacketNumber) {
        this.messagePacketNumber = messagePacketNumber;
    }

    public String getMessageData() {
        return messageData;
    }

    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    public String getEndPacektCommand() {
        return endPacektCommand;
    }

    public void setEndPacektCommand(String endPacektCommand) {
        this.endPacektCommand = endPacektCommand;
    }

    public String getEndpacketDatalength() {
        return endpacketDatalength;
    }

    public void setEndpacketDatalength(String endpacketDatalength) {
        this.endpacketDatalength = endpacketDatalength;
    }

    public String getEndpacketUpcode() {
        return endpacketUpcode;
    }

    public void setEndpacketUpcode(String endpacketUpcode) {
        this.endpacketUpcode = endpacketUpcode;
    }

    public String getEndpacketChannelID() {
        return endpacketChannelID;
    }

    public void setEndpacketChannelID(String endpacketChannelID) {
        this.endpacketChannelID = endpacketChannelID;
    }
}
