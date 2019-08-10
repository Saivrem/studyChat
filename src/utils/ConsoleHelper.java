package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {

    private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        try {
            return consoleReader.readLine();
        } catch (IOException ioEx) {
            writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            readString();
        }
        return readString();
    }

    public static int readInt() {
        try {
            return Integer.parseInt(readString());
        } catch (NumberFormatException nfEx) {
            writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            readInt();
        }
        return readInt();
    }
}
