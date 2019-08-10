package client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static utils.ConsoleHelper.writeMessage;

public class BotClient extends Client {
    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            if (message.contains(":")) {
                String userName = message.split(": ")[0];
                String text = message.split(": ")[1];
                String pattern;
                switch (text) {
                    case "дата":
                        pattern = "d.MM.YYYY";
                        break;
                    case "день":
                        pattern = "d";
                        break;
                    case "месяц":
                        pattern = "MMMM";
                        break;
                    case "год":
                        pattern = "YYYY";
                        break;
                    case "время":
                        pattern = "H:mm:ss";
                        break;
                    case "час":
                        pattern = "H";
                        break;
                    case "минуты":
                        pattern = "m";
                        break;
                    case "секунды":
                        pattern = "s";
                        break;
                    default:
                        return;
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                sendTextMessage(String.format("Информация для %s: %s", userName, simpleDateFormat.format(Calendar.getInstance().getTime())));
            }
        }
    }


    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected String getServerAddress() {
        return "127.0.0.1";
    }

    @Override
    protected int getServerPort() {
        return 9090;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        int num = (int) (Math.random() * 100);
        return String.format("date_bot_%d", num);
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
