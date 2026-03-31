/**
 * Interface for objects that can broadcast messages.
 * Implemented by ServiceManager.
 */
public interface Sendable {
    void sendMessage(String message);
}
