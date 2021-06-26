package com.vithamastech.smartlight.PowerSocketUtils;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Socket implements Serializable {

    public int socketId;
    public int socketState;
    public String socketName;
    public boolean shouldWaitForOutput;
    public boolean isEnabled = true;
    public int imageType;

    public Socket(int socketId, int socketState, String socketName, int imageType) {
        this.socketId = socketId;
        this.socketState = socketState;
        this.socketName = socketName;
        this.imageType = imageType;
    }

    public Socket(int socketId, int socketState, String socketName) {
        this.socketId = socketId;
        this.socketState = socketState;
        this.socketName = socketName;
        this.imageType = 0;
    }

    public Socket(){
        this.imageType = 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Socket) {
            return this.socketId == ((Socket) obj).socketId;
        }
        return false;
    }
}