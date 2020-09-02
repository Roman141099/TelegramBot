package Main.UBot.com;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("User ID")
    private final Long userChatId;
    @SerializedName("Session settings")
    private Session currentSession;

    public User() {
        userChatId = 0L;
        currentSession = null;
    }

    public Long getUserChatId() {
        return userChatId;
    }

    public User(Long userChatId) {
        this.userChatId = userChatId;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public User(Long userChatId, Session currentSession) {
        this.userChatId = userChatId;
        this.currentSession = currentSession;
    }

    @Override
    public String toString() {
        return "User{id=" + userChatId + ", current session=" + currentSession + '}';
    }
}
