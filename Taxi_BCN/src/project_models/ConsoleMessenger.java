package project_models;

public class ConsoleMessenger implements Sendable{

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }
}
