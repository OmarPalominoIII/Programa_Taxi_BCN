package models;

import services.Sendable;

public class ConsoleMessenger implements Sendable {

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }
}
