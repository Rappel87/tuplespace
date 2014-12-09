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
        String[] message_tuple = this.t.get("message", this.channel, null, null);
        String[] channel_tuple = this.t.get("channel",this.channel,null,null);

        int msg_count = Integer.parseInt(channel_tuple[2]);
        System.out.println("get next message : count"+ msg_count +" value : "+message_tuple[3]);


        int read = Integer.parseInt(message_tuple[2]);
        String message = message_tuple[3];
       // System.out.println("get message "+ channel +" : "+ message);
        if (read > 1) {
            message_tuple[2] = Integer.toString(read-1);
            t.put(message_tuple);
        }
        else
            channel_tuple[2] = Integer.toString(msg_count-1);
        this.t.put(channel_tuple);
        return message;
    }

	public void closeConnection() {
        String[] chann_tuple = t.get("channel",channel,null,null);
        int msg_count = Integer.parseInt(chann_tuple[2]);
        int msg_listener = Integer.parseInt(chann_tuple[3])-1;
        chann_tuple[3] = Integer.toString(msg_listener);
        System.out.println(msg_count);

        // update message reader_count
        String[][] messages = new String[msg_count][4];
        for (int i=0; i<msg_count;i++) {
            messages[i] = t.get("message",channel,null,null);
            System.out.println(" close connection :"+messages[i][2]);
        }
        for (int i=0; i<msg_count;i++) {
            messages[i][2] = Integer.toString(Integer.parseInt(messages[i][2]) - 1);
            t.put(messages[i]);
        }
        t.put(chann_tuple);
    }
}
