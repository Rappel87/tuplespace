package chat;

import tupleserver.TupleProxy;
import tuplespaces.TupleSpace;

import java.util.HashMap;

public class ChatServer {
	// Add stuff here.
    private TupleSpace t;
    private int rows;
    private String[] channelNames;
	
	public ChatServer(TupleSpace t, int rows, String[] channelNames) {
		this.t = t;
        this.rows = rows;
        this.channelNames = channelNames;
        for (String channel: channelNames)
           t.put("channel",channel,Integer.toString(0),Integer.toString(0));
	}

	public ChatServer(TupleSpace t) {
        this.t = t;
        // store internally the channels and a particular tuple channelNames
	}

	public String[] getChannels() {
		return this.channelNames;
	}

	public void writeMessage(String channel, String message) {
        String[] chann_tuple = t.read("channel", channel, null,null);
        int msg_count = Integer.parseInt(chann_tuple[2]);
        int nbr_listener = Integer.parseInt(chann_tuple[3]);
        if ( msg_count >= rows)
            //delete one message to not exceed rows limit, buffer to handler later
            t.get("message",channel,null,null);
        else {
            t.get("channel",channel,null,null);
            t.put("channel", channel, Integer.toString(msg_count + 1),Integer.toString(nbr_listener));
        }
        t.put("message",channel,Integer.toString(nbr_listener),message);
	}

	public ChatListener openConnection(String channel) {
        String[] chann_tuple = t.get("channel",channel,null,null);
        int msg_listener = Integer.parseInt(chann_tuple[3])+1;
        chann_tuple[3] = Integer.toString(msg_listener);
        t.put(chann_tuple);
        ChatListener listener = new ChatListener(channel, this.t);
        return listener;
	}
}
