package client;

import utils.Connection;
import utils.Message;
import utils.MessageType;

import java.io.IOException;
import java.net.Socket;

import static utils.ConsoleHelper.*;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread {

        @Override
        public void run() {
            try {
                Socket socket = new Socket(getServerAddress(),getServerPort());
                Client.this.connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException cl) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                } else {
                    throw new IOException("Unexpected Utils.MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType type = message.getType();
                String data = message.getData();
                if (type == MessageType.TEXT) {
                    processIncomingMessage(data);
                } else if (type == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(data);
                } else if (type == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(data);
                } else {
                    throw new IOException("Unexpected Utils.MessageType");
                }
            }
        }

        protected void processIncomingMessage(String message) {
            writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            writeMessage(userName + " is connected to chat");
        }

        protected void informAboutDeletingNewUser(String userName) {
            writeMessage(userName + " left the chat");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }
    }

    public void run() {
        Thread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                writeMessage("Something went wrong connecting the server");
                System.exit(0);
            }
        }
        if (clientConnected) {
            writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else writeMessage("Произошла ошибка во время работы клиента.");

        while (clientConnected) {
            String input = readString();
            if (input.equals("exit")) {
                break;
            } else if (shouldSendTextFromConsole()) {
                sendTextMessage(input);
            }
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress() {
        writeMessage("Plese, enter the server address");
        return readString();
    }

    protected int getServerPort() {
        writeMessage("Please, enter the server port");
        return readInt();
    }

    protected String getUserName() {
        writeMessage("Please, enter desired username");
        return readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException ioEx) {
            writeMessage("Произошла ошибка при отправке сообщения");
            clientConnected = false;
        }
    }
}
