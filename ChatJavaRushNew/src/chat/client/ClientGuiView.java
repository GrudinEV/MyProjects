package chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGuiView {
    private final ClientGuiController controller;

    private JFrame frame = new JFrame("Чат"); //Создание объекта класса JFrame, окно с рамкой.
    private JTextField textField = new JTextField(50); //Создание объекта текстового однострочного поля длиной 50 символов (колонок?)
                                                                //для ввода пользователем текста в чат
    private JTextArea messages = new JTextArea(10, 50); //Создание объекта текстового многострочного поля
                                                                        //высотой 10 строк и шириной 50 колонок
    private JTextArea users = new JTextArea(10, 10);

    public ClientGuiView(ClientGuiController controller) {
        this.controller = controller;
        initView();
    }

    private void initView() {
        textField.setEditable(false); //Делаем все поля не редактируемыми.
        messages.setEditable(false);
        users.setEditable(false);

        frame.getContentPane().add(textField, BorderLayout.NORTH); //Добавляем в окно текстовые поля
        frame.getContentPane().add(new JScrollPane(messages), BorderLayout.WEST);
        frame.getContentPane().add(new JScrollPane(users), BorderLayout.EAST);
        frame.pack(); //Устанавливаем размер окна, чтобы вместились все добавленные комоненты, в нашем случае текстовые поля
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Выбираем действие окна при нажатии на крестик окно должно закрываться
        frame.setVisible(true); //После всех настроек делаем окно видимым.

        textField.addActionListener(new ActionListener() { //добавляем к однострочному текстовому полю слушатель событий
            public void actionPerformed(ActionEvent e) {
                controller.sendTextMessage(textField.getText()); //При нажатии клавиши Ентер контролле отправляет напечатанный текст
                textField.setText(""); //После отправки текст в поле устанавливается пустым
            }
        });
    }

    public String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Введите адрес сервера:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
    }

    public int getServerPort() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame,
                    "Введите порт сервера:",
                    "Конфигурация клиента",
                    JOptionPane.QUESTION_MESSAGE);
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Был введен некорректный порт сервера. Попробуйте еще раз.",
                        "Конфигурация клиента",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getUserName() {
        return JOptionPane.showInputDialog(
                frame,
                "Введите ваше имя:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
    }

    public void notifyConnectionStatusChanged(boolean clientConnected) {
        textField.setEditable(clientConnected);
        if (clientConnected) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Соединение с сервером установлено",
                    "Чат",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Клиент не подключен к серверу",
                    "Чат",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public void refreshMessages() {
        messages.append(controller.getModel().getNewMessage() + "\n");
    }

    public void refreshUsers() {
        ClientGuiModel model = controller.getModel();
        StringBuilder sb = new StringBuilder();
        for (String userName : model.getAllUserNames()) {
            sb.append(userName).append("\n");
        }
        users.setText(sb.toString());
    }
}
