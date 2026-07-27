package lab;

import com.eclipsesource.json.Json;

import java.util.function.Consumer;

public class EchoServer {
    void run() {
        Node node = new Node();
        node.on("echo", new Consumer<Message>() {
            public void accept(Message msg) {
                node.reply(msg, Json.object()
                        .add("type", "echo_ok")
                        .add("echo", msg.body.getString("echo", null)));
            }
        });
        node.run();
    }
}
