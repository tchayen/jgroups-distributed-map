package com.company;

import java.io.Serializable;

public class MapMessage implements Serializable {
    public MessageType type;
    public String key;
    public Integer value;

    public MapMessage(MessageType type, String key, Integer value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public MapMessage(MessageType type, String key) {
        this.type = type;
        this.key = key;
    }
}
