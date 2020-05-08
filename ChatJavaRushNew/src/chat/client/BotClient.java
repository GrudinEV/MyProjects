package chat.client;

import chat.ConsoleHelper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String[] nameAndMessage = message.split(": ");
            if (nameAndMessage.length == 2) {
                Calendar calendar = new GregorianCalendar();
                Date date = calendar.getTime();
                String dateString = null;
                switch (nameAndMessage[1]) {
                    case "дата":
                        dateString = new SimpleDateFormat("d.MM.YYYY").format(date);
                        break;
                    case "день":
                        dateString = new SimpleDateFormat("d").format(date);
                        break;
                    case "месяц":
                        dateString = new SimpleDateFormat("MMMM").format(date);
                        break;
                    case "год":
                        dateString = new SimpleDateFormat("YYYY").format(date);
                        break;
                    case "время":
                        dateString = new SimpleDateFormat("H:mm:ss").format(date);
                        break;
                    case "час":
                        dateString = new SimpleDateFormat("H").format(date);
                        break;
                    case "минуты":
                        dateString = new SimpleDateFormat("m").format(date);
                        break;
                    case "секунды":
                        dateString = new SimpleDateFormat("s").format(date);
                        break;
                }
                if (dateString != null) {
                    sendTextMessage("Информация для " + nameAndMessage[0] + ": " + dateString);
                }
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        String botName = "date_bot_" + (int) (Math.random() * 100);
        return botName;
    }

    public static void main(String[] args) {
        new BotClient().run();
    }
}
