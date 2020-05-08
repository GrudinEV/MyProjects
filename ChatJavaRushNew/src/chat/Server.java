package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread {
        private Socket socket;

        private Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String userName = null;
            SocketAddress socketAddress = null;
            System.out.println("Установлено соединение с удалённым адресом " + socket.getRemoteSocketAddress() + ".");
            try (Connection connection = new Connection(socket)){
                socketAddress = connection.getRemoteSocketAddress();
                userName = serverHandshake(connection);
                Message userAdded = new Message(MessageType.USER_ADDED, userName);
                sendBroadcastMessage(userAdded);
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом" + socketAddress + ".");
            }
            if (userName != null) {
                connectionMap.remove(userName);
                Message userRemoved = new Message(MessageType.USER_REMOVED, userName);
                sendBroadcastMessage(userRemoved);
            }
            ConsoleHelper.writeMessage("Соединение с удалённым адресом закрыто.");
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message nameRequest = new Message(MessageType.NAME_REQUEST, "Добро пожаловать в чат! Представьтесь пожалуйста.");
            boolean b = false;
            connection.send(nameRequest);
            String nameUser = null;
            while (!b) {
                Message name = connection.receive();
                if (name.getType().equals(MessageType.USER_NAME)) {
                    nameUser = name.getData();
                    if (nameUser != null && !nameUser.equals("") && !connectionMap.containsKey(nameUser)) {
                        connectionMap.put(nameUser, connection);
                        Message nameAccepted = new Message(MessageType.NAME_ACCEPTED, "Ваше имя принято, " + nameUser + ". Приятного общения!");
                        connection.send(nameAccepted);
                        b = true;
                    } else {
                        Message nameRequestReplay = new Message(MessageType.NAME_REQUEST, "Пользователь с таким именем уже существует, или введено несоотвествующее имя. Прошу ввести Ваше имя повторно.");
                        connection.send(nameRequestReplay);
                    }
                } else {
                    Message nameRequestReplay = new Message(MessageType.NAME_REQUEST, "Вы не ввели имя. Прошу ввести Ваше имя повторно.");
                    connection.send(nameRequestReplay);
                }
            }
            return nameUser;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : connectionMap.keySet()) {
                if (!name.equals(userName)) {
                    Message sendNameAddedUser = new Message(MessageType.USER_ADDED, name);
                    connection.send(sendNameAddedUser);
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String str = userName + ": " + message.getData();
                    Message newMessage = new Message(MessageType.TEXT, str);
                    sendBroadcastMessage(newMessage);
                } else {
                    String str = "Принятое сообщение на является chat.MessageType.TEXT";
                    ConsoleHelper.writeMessage(str);
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Сообщение пользователю " + pair.getKey() + " не было отправлено.");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = ConsoleHelper.readInt();
        try (ServerSocket server = new ServerSocket(port)){
            System.out.println("Сервер запущен.");
            try {
                while (true) {
                    Socket socket = server.accept();
                    new Handler(socket).start();
                }
            } catch (Exception e) {
                System.out.println("Произошла ошибка!");
            }
        }
    }
}
