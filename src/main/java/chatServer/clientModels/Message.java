package chatServer.clientModels;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable, Comparable<Message>{
    private int id;
    private String fromMobile;
    private String fromName;
    private String toMobile;
    private String message;
    private LocalDateTime timestamp;
    private static final long serialVersionUID = 6529685329237757620L;

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromMobile() {
        return fromMobile;
    }

    public void setFromMobile(String fromMobile) {
        this.fromMobile = fromMobile;
    }

    public String getToMobile() {
        return toMobile;
    }

    public void setToMobile(String toMobile) {
        this.toMobile = toMobile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Message message) {
        return getTimestamp().compareTo(message.getTimestamp());
    }
}
