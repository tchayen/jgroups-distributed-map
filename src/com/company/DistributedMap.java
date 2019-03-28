package com.company;

import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DistributedMap implements Receiver, SimpleStringMap, AutoCloseable {
    private JChannel jChannel;
    private HashMap<String, Integer> map;

    public DistributedMap(String channelId) throws Exception {
        jChannel = new JChannel();
        jChannel.setReceiver(this);
        jChannel.connect(channelId);
        jChannel.getState(null, 0);
        map = new HashMap<>();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        try {
            jChannel.send(new Message(null, new MapMessage(MessageType.PUT, key, value)));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Integer remove(String key) {
        try {
            jChannel.send(new Message(null, new MapMessage(MessageType.REMOVE, key)));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return map.get(key);
    }

    public Map<String, Integer> all() {
        return map;
    }

    @Override
    public void close() {
        jChannel.close();
    }

    @Override
    public void receive(Message message) {
        MapMessage messageType = message.getObject();
        switch (messageType.type) {
            case PUT:
                map.put(messageType.key, messageType.value);
                break;
            case REMOVE:
                map.remove(messageType.key);
                break;
            default:
                System.out.println("Unrecognized message type!");
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (map) {
            Util.objectToStream(map, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        synchronized (map) {
            map = Util.objectFromStream(new DataInputStream(input));
        }
    }

    @Override
    public void viewAccepted(View view) {
        if (view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(jChannel, (MergeView) view);
            handler.start();
        }
    }

    private static class ViewHandler extends Thread {
        JChannel channel;
        MergeView view;

        private ViewHandler(JChannel channel, MergeView view) {
            this.channel = channel;
            this.view = view;
        }

        public void run() {
            Vector<View> subgroups = (Vector<View>) view.getSubgroups();
            View tmpView = subgroups.firstElement();
            Address localAddress = channel.getAddress();
            if (!tmpView.getMembers().contains(localAddress)) {
                System.out.println("Dropping state...");
                try {
                    channel.getState(null, 10000);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
                System.out.println("Doing nothing...");
            }
        }
    }
}
