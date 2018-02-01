package com.example.errortime.kati_command;

/**
 * Created by Error Time on 11/30/2017.
 */

public class ChatMessage {
    private String content;
    private boolean isMine;

    public ChatMessage(String content, boolean isMine) {
        this.content = content;
        this.isMine = isMine;
    }

    public String getContent() {
        return content;
    }

    public boolean isMine() {
        return isMine;
    }
}
