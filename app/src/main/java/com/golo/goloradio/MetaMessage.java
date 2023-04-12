package com.golo.goloradio;


enum MessageType
{
    META_CHANGE, PLAYING_STATE_CHANGE, BLUE;
}

public class MetaMessage {
    public final MessageType type;
    public final String message;

    public final int play_state;

    public static MetaMessage getInstance(MessageType type,String message) {
        return new MetaMessage(type,message);
    }

    public MetaMessage(MessageType type,String message) {
        this.type = type;
        this.message = message;
        this.play_state = -1;
    }

    public MetaMessage(MessageType type,int message) {
        this.type = type;
        this.play_state = message;
        this.message = "";
    }
}
