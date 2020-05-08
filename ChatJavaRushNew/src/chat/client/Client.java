package chat.client;

import chat.Connection;
import chat.ConsoleHelper;
import chat.Message;
import chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread {
        public void run() {
            ConsoleHelper.writeMessage("Введите адрес сервера.");
            String serverAddress = getServerAddress();
            ConsoleHelper.writeMessage("Введите порт сервера.");
            int port = getServerPort();
            try {
                Socket socket = new Socket(serverAddress, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType type = message.getType();
                if (type != null) {
                    if (type.equals(MessageType.NAME_REQUEST)) {
                        ConsoleHelper.writeMessage(message.getData());
                        String userName = getUserName();
                        Message sendUserName = new Message(MessageType.USER_NAME, userName);
                        connection.send(sendUserName);
                    } else {
                        if (type.equals(MessageType.NAME_ACCEPTED)) {
                            notifyConnectionStatusChanged(true);
                            break;
                        } else {
                            throw new IOException("Unexpected chat.MessageType");
                        }
                    }
                } else {
                    throw new IOException("Unexpected chat.MessageType");
                }

            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType type = message.getType();
                String data = message.getData();
                if (type != null) {
                    if (type.equals(MessageType.TEXT)) {
                        processIncomingMessage(data);
                    } else {
                        if (type.equals(MessageType.USER_ADDED)) {
                            informAboutAddingNewUser(data);
                        } else {
                            if (type.equals(MessageType.USER_REMOVED)) {
                                informAboutDeletingNewUser(data);
                            } else {
                                throw new IOException("Unexpected chat.MessageType");
                            }
                        }
                    }
                } else {
                    throw new IOException("Unexpected chat.MessageType");
                }
            }
        }

    }

    public static void main(String[] args) {
//        new Client().run();
        new ClientGuiController().run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this){
                wait();
            }
            if (clientConnected) {
                ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
                while (clientConnected) {
                    String message = ConsoleHelper.readString();
                    if (message.equals("exit")) {
                        break;
                    } else {
                        if (shouldSendTextFromConsole()) {
                            sendTextMessage(message);
                        }
                    }
                }
            } else {
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Подключение к серверу было прервано.");
        }
    }

    protected String getServerAddress() {
        String serverAddress = ConsoleHelper.readString();
        return serverAddress;
    }

    protected int getServerPort() {
        int serverPort = ConsoleHelper.readInt();
        return serverPort;
    }

    protected String getUserName() {
        String userName = ConsoleHelper.readString();
        return userName;
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
        } catch (IOException e) {
            clientConnected = false;
            ConsoleHelper.writeMessage("Сообщение не отправлено!");
        }
    }
}
