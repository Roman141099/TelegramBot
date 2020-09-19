package Main.UBot.com;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class User {
    @SerializedName("User firstname")
    private String firstName;
    @SerializedName("User lastname")
    private String lastName;
    @SerializedName("User ID")
    private final Long userChatId;
    @SerializedName("Session settings")
    private Session currentSession;
    @SerializedName("Priority cities list")
    private final List<String> citiesList;
    @SerializedName("Phone number")
    private String phoneNumber;
    @SerializedName("Last searched city")
    private String currentRequestedCity;


    public User() {
        userChatId = 0L;
        currentSession = null;
        firstName = "DefaultFirstName";
        lastName = "DefaultLastName";
        citiesList = new ArrayList<>(5);
    }

    public User(Long userChatId) {
        this.userChatId = userChatId;
        citiesList = new ArrayList<>(5);
    }

    public User(Long userChatId, Session currentSession, String firstName, String lastName, List<String> citiesList) {
        this.userChatId = userChatId;
        this.currentSession = currentSession;
        this.firstName = firstName;
        this.lastName = lastName;
        this.citiesList = citiesList;
    }

    public String getCurrentRequestedCity() {
        return currentRequestedCity;
    }

    public void setCurrentRequestedCity(String currentRequestedCity) {
        this.currentRequestedCity = currentRequestedCity;
    }

    public List<String> citiesList() {
        return citiesList;
    }

    public String getFirstName() {
        return firstName;
    }

    public void addCity(String addedCity) {
        citiesList.add(addedCity);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getUserChatId() {
        return userChatId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    @Override
    public String toString() {
        return "User{id=" + userChatId + ", current session=" + currentSession + '}';
    }

    public String getLastName() {
        return lastName;
    }
}
