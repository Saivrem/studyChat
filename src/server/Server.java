package server;

import utils.Connection;
import utils.Message;
import utils.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static utils.ConsoleHelper.readInt;
import static utils.ConsoleHelper.writeMessage;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread {
        Socket socket;

        @Override
        public void run() {
            writeMessage(String.format("Соединение установлено с %s", socket.getRemoteSocketAddress()));
            try (Connection connection = new Connection(socket)) {
                String userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                try {
                    serverMainLoop(connection, userName);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                synchronized (connectionMap) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            synchronized (connectionMap) {
                boolean success = false;
                Message message;
                do {
                    connection.send(new Message(MessageType.NAME_REQUEST));
                    message = connection.receive();
                    if (message.getType() == MessageType.USER_NAME) {
                        if (message.getData() != null && !message.getData().equals("")) {
                            if (!connectionMap.containsKey(message.getData())) {
                                success = true;
                            }
                        }
                    }
                } while (!success);
                connectionMap.put(message.getData(), connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return message.getData();
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            synchronized (connectionMap) {
                for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                    Message message = new Message(MessageType.USER_ADDED, pair.getKey());
                    if (!userName.equals(pair.getKey())) {
                        connection.send(message);
                    }
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.TEXT) {
                    Message toForward = new Message(MessageType.TEXT, String.format("%s: %s", userName, message.getData()));
                    sendBroadcastMessage(toForward);
                } else {
                    writeMessage("Сообщение об ошибке");
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        synchronized (connectionMap) {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()
            ) {
                try {
                    pair.getValue().send(message);
                } catch (IOException e) {
                    writeMessage("Не удалось отправить сообщение");
                }
            }
        }
    }

    public static void main(String[] args) {
        int port;
        if (args.length == 0) {
            port = 9090;
        } else {
            port = Integer.parseInt(args[0]);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            writeMessage("Произошла ошибка");
        }
    }
}
