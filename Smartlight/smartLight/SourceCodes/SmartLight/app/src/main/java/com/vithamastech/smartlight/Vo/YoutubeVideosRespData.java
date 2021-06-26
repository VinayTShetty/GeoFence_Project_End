package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

public class YoutubeVideosRespData implements Serializable {

    String response ="";
    String message ="";
    List<VoYouTubeVideos> data;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<VoYouTubeVideos> getData() {
        return data;
    }

    public void setData(List<VoYouTubeVideos> data) {
        this.data = data;
    }
}
