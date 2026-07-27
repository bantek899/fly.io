package lab;

import com.eclipsesource.json.JsonObject;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class UniqueIdServer {
    private AtomicLong id = new AtomicLong();

    void run() {
        Node node = new Node();

        node.on("generate", new Consumer<Message>() {
            public void accept(Message msg) {
                long res = id.getAndIncrement();
                node.reply(msg, new JsonObject()
                        .add("type", "generate_ok")
                        .add("in_reply_to", msg.body.getLong("msg_id", 0))
                        .add("id", res+"-"+node.nodeId));
            }
        });

        node.run();
    }
}

