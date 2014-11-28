package chat;

import tuplespaces.TupleSpace;

public class ChatListener {

    private TupleSpace t;
    private String channel;

    public ChatListener(String channel, TupleSpace t) {
        this.channel = channel;
        this.t = t;
    }

	public String getNextMessage() {

		String[] message_tuple = this.t.get("message", channel, null, null);
        String[] channel_tuple = t.get("channel",this.channel,null,null);

        int msg_count = Integer.parseInt(channel_tuple[2]);
        int nbr_listener = Integer.parseInt(channel_tuple[3]);

        int read = Integer.parseInt(message_tuple[2]);
        String message = message_tuple[3];

        this.t.put("channel",this.channel,Integer.toString(msg_count-1),Integer.toString(nbr_listener));
        if (read > 1) {
            message_tuple[2] = Integer.toString(read-1);
            t.put(message_tuple);
        }
        
        return message;
	}

	public void closeConnection() {
        String[] chann_tuple = t.get("channel",channel,null,null);
        int msg_count = Integer.parseInt(chann_tuple[2]);
        int msg_listener = Integer.parseInt(chann_tuple[3])-1;
        chann_tuple[3] = Integer.toString(msg_listener);
        t.put(chann_tuple);
//        String[msg_count][] msg_history;
//        for (int i=0; i<msg_count;i++)
//            String[][i] =

        // decrement read_count in every message of the channel

    }
}
