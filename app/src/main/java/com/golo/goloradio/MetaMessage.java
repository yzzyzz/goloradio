package com.golo.goloradio;

public class MetaMessage {
    public final String message;

    public static MetaMessage getInstance(String message) {
        return new MetaMessage(message);
    }

    public MetaMessage(String message) {
        this.message = message;
    }
}
