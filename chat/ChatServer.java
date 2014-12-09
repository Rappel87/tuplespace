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
        System.out.println("write message "+ channel +" : "+ message);
        String[] chann_tuple = t.get("channel", channel, null, null);
        int msg_count = Integer.parseInt(chann_tuple[2]);
        int nbr_listener = Integer.parseInt(chann_tuple[3]);
        if ( msg_count >= rows)
        //delete one message to not exceed rows limit, buffer to handler later
            t.get("message",channel,null,null);
        else
            msg_count++;
        t.put("message",channel,Integer.toString(nbr_listener),message);
        t.put("channel", channel, Integer.toString(msg_count),Integer.toString(nbr_listener));

	}

	public ChatListener openConnection(String channel) {
        String[] chann_tuple = t.get("channel",channel,null,null);
        //update channel listener
        int msg_count = Integer.parseInt(chann_tuple[2]);
        int msg_listener = Integer.parseInt(chann_tuple[3])+1;
        chann_tuple[3] = Integer.toString(msg_listener);

        // update messages read_count
        System.out.println("message_count" + msg_count);
        String[][] messages = new String[msg_count][4];
        for (int i=0; i<msg_count;i++) {
            messages[i] = t.get("message",channel,null,null);
            System.out.println(" open connection :"+messages[i][2]);
        }
        for (int i=0; i<msg_count;i++) {
            messages[i][2] = Integer.toString((Integer.parseInt(messages[i][2])) + 1);
            t.put(messages[i]);
        }

        ChatListener listener = new ChatListener(channel, this.t);
        t.put(chann_tuple);


        return listener;
	}
}
