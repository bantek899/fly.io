package lab;


class Main {
    
    public static void main(String[] args) {
        final String server = args[0];

        switch (server) {
            case "echo" -> new EchoServer().run();
            case "generate" -> new UniqueIdServer().run();
            case "broadcast" -> new BroadcastServer().run();
        }
    }
}

