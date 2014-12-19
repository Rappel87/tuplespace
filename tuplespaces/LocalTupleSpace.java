package tuplespaces;

import java.util.HashMap;
import java.util.LinkedList;

public class LocalTupleSpace implements TupleSpace {
  // Add stuff here.
  private HashMap<Integer, LinkedList<String[]>> searchMap;

  public LocalTupleSpace () {
    searchMap = new HashMap<Integer,  LinkedList<String[]>>();
  }

  public String[] get(String... pattern) {
    return fetch (false, pattern);
  }

  public String[] read(String... pattern) {
    // how to use this sort by size because a pattern can means that the size of the string to find is not fix ?
    return fetch (true, pattern);
  }

  public void put(String... tuple) {
    // if the LinkedList not exists create it
    String[] toPut = tuple.clone();

    synchronized (searchMap) {
      if (!searchMap.containsKey(tuple.length)) {
        searchMap.put(tuple.length, new LinkedList<String[]>());
        searchMap.notifyAll();
      }
    }
    LinkedList<String[]> tupleList = searchMap.get(tuple.length);
    synchronized (tupleList) {
      tupleList.add(toPut);
      tupleList.notifyAll();
    }


    // 1) come from the application
    // put it in internal data strcture ?

    // 2) come from the server
    // put message from the proxy
    // put in internal structure in any case

    // 3) come from the proxy
    // receive ACK from put or ANSWER from get
    // so put in internal structure in any case
  }


  private String[] fetch (Boolean read, String... pattern)
  {
    String[] answer = new String[pattern.length];
    Boolean match = false;
    Boolean found = true;

    synchronized (searchMap) {
      if (!searchMap.containsKey(pattern.length))
        try {
          searchMap.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
    }
    LinkedList<String[]> tupleList = searchMap.get(pattern.length);

    synchronized (tupleList) {
      while(!match) {
        for (String[] tuple : tupleList) {
          for(int i=0; i< pattern.length; i++) {
            if (!(tuple[i].equals (pattern[i]) || pattern[i] == null)) {
              found = false;
              break;
            }
          }
          if (found == true) {
            answer = tuple.clone();
            if (!read)
            {
              tupleList.remove(tuple);
            }
            match = true;
            break;
          }
          else
          {
            found = true;
          }
        }
        if (!match)
        {
          try {
            tupleList.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

      }
    }
    return answer;
  }

}
