package lab;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class BroadcastServer {
    private final Collection<Integer> messages = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<String>> topology = new HashMap<>();
    private final Map<Integer, List<String>> log = new HashMap<>();

    void run() {
        Node node = new Node();

        node.on("topology", new Consumer<Message>() {
            public void accept(Message msg) {
                JsonObject topologyObject = msg.body.get("topology").asObject();

                for (JsonObject.Member member : topologyObject) {
                    String node = member.getName();
                    JsonArray neighboursArr = member.getValue().asArray();
                    List<String> neighbours = new ArrayList<>();
                    for (JsonValue v : neighboursArr) {
                        neighbours.add(v.asString());
                    }

                    topology.put(node, neighbours);
                }

//                System.out.println(topology);


                node.reply(msg, new JsonObject().add("type", "topology_ok"));
            }
        });

        node.on("broadcast", (msg) -> {
            Integer i = msg.body.getInt("message", 0);
            String id = node.nodeId;

            if (log.containsKey(i) && log.get(i).contains(id)) {
                return;
            }

            List<String> seen = log.getOrDefault(i, new ArrayList<>());
            seen.add(id);
            log.put(i, seen);

//            if (messages.contains(i)) {
//                return;
//            }
//
//            Collection<String> seen = Collections.synchronizedList(new ArrayList<>());
//            if (msg.body.get("seen") != null) {
//                JsonArray seenArr = msg.body.get("seen").asArray();
//                for (JsonValue v : seenArr) {
//                    seen.add(v.asString());
//                }
//            }
//
//            messages.add(i);
//            seen.add(id);

            messages.add(i);
            List<String> neighbours = topology.get(id);

//            for (String n : neighbours) {
////                if (!seen.contains(n)) {
//                    JsonArray jsonSeen = new JsonArray();
//                    synchronized (seen) {
//                        seen.forEach(jsonSeen::add);
//                    }
////                    node.send(n, new JsonObject().add("type", "broadcast").add("message", i).add("seen", jsonSeen));
//                    node.send(n, new JsonObject().add("type", "broadcast").add("message", i));
////                }
//
//            }

            for (String n : neighbours) {
                node.send(n, new JsonObject().add("type", "broadcast").add("message", i));
            }

            node.reply(msg, new JsonObject().add("type", "broadcast_ok"));
        });

        node.on("read", (msg) -> {
            JsonArray jsonMessages = new JsonArray();
            synchronized (messages) {
                messages.forEach(jsonMessages::add);
            }


            node.reply(msg, new JsonObject().add("type", "read_ok").add("messages", jsonMessages));
        });

        node.run();
    }
}
