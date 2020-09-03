package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MainBotController extends TelegramLongPollingBot {
    private static final String URL_TO_DB = "src/main/resources/Data bases/sessionStates.json";
    private static final Gson jsonSessionDeserializer = new GsonBuilder().setPrettyPrinting().
            registerTypeAdapter(Session.class, new SessionDeserializer()).create();
    private static final Map<Long, User> sessionState;
    static {
        sessionState = uploadInfo(Paths.get(URL_TO_DB));
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        System.out.println("Активность от " + sessionState.get(message.getChatId()));
        User user = new User(message.getChatId());
        if (!sessionState.containsKey(user.getUserChatId())) {
            Chat userChat = message.getChat();
            user.setFirstName(userChat.getFirstName() != null ? userChat.getFirstName() : "Unknown");
            user.setLastName(userChat.getLastName() != null ? userChat.getLastName() : "Unknown");
            sessionState.put(message.getChatId(), user);
            //Recording new user to Json
            recordInfo(Paths.get(URL_TO_DB));//OK

            System.out.println("Создался новый user, id=" + message.getChatId());
        } else {
            user = sessionState.get(message.getChatId());
            System.out.println("User from data base activated, id=" + user.getUserChatId());
        }
        String mesTxt = message.getText().trim();
        if (mesTxt.equals("<HOME>")) {
//            user.getCurrentSession().setSessionOpened(false);
            user.getCurrentSession().terminateAllProcesses();
            sendMessage(message, "Choose operation or lets talk!");
            recordInfo(Paths.get(URL_TO_DB));
            System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
        }
        if (user.getCurrentSession() == null || !user.getCurrentSession().isSessionOpened()) {
            if (mesTxt.equalsIgnoreCase("/start")) {
                startBot(message);
                System.out.println(Phrases.SENDED_FROM_USER + "\n<" + message.getText() + '>');
                System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
            }
            if (mesTxt.equalsIgnoreCase("ok")) {
                sendMessage(message, "Choose operation or lets talk!");
                System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
            }
            if (mesTxt.equalsIgnoreCase("weather now")) {
                user.setCurrentSession(new WeatherSession());
                sendMessage(message, user.getCurrentSession().nextStep(message.getText()));
                recordInfo(Paths.get(URL_TO_DB));
                System.out.println("Сессия открыта=" + user.getCurrentSession().isSessionOpened() + ", " + user);
            }
            if(mesTxt.equalsIgnoreCase("lets talk")){
                user.setCurrentSession(new TalkingSession());
                sendMessage(message, user.getCurrentSession().nextStep(message.getText()));
                recordInfo(Paths.get(URL_TO_DB));
            }
            if (mesTxt.toLowerCase().startsWith("ты")) {
                String[] ar = mesTxt.split(" ");
                sendMessage(message, ar.length > 1 ? "Сам ты " + ar[1].toLowerCase() : "Не понял прикола");
            }
        } else {
            sendMessage(message, user.getCurrentSession().nextStep(message.getText()));
            recordInfo(Paths.get(URL_TO_DB));
            System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
        }
    }

    private void startBot(Message msg) {
        ReplyKeyboardMarkup startMarkUp = new ReplyKeyboardMarkup();
        SendMessage startMsg = new SendMessage();
        final String startMessage = "Hello! I am multi functionality bot. You can use my functional as how you want. \n" +
                "During your using of this bot we can save your unique id and other information. Don't care about this, \n" +
                "we do it for statistics.Pressing OK button you confirm this message and private policy. Founder @romagrigoriew";
        startMsg.setText(startMessage);

        System.out.println(Phrases.SENDED_FROM_BOT + "\n<" + startMsg + ">");

        startMsg.setChatId(msg.getChatId());
        startMarkUp.setOneTimeKeyboard(true);
        startMarkUp.setResizeKeyboard(true);
        KeyboardRow startRow = new KeyboardRow();
        startRow.add("OK");
        startMarkUp.setKeyboard(List.of(startRow));
        startMsg.setReplyMarkup(startMarkUp);
        try {
            execute(startMsg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void setKeyBoard(SendMessage sM) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(false);

        KeyboardRow weatherKey = new KeyboardRow();
        weatherKey.add(new KeyboardButton("Weather now"));

        KeyboardRow homeButtons = new KeyboardRow();
        homeButtons.add(new KeyboardButton("<HOME>"));

        List<KeyboardRow> keys = new ArrayList<>();
        keys.add(weatherKey);
        keys.add(homeButtons);
        markup.setKeyboard(keys);
        markup.setOneTimeKeyboard(false);
        sM.setReplyMarkup(markup);
    }

    @Override
    public String getBotUsername() {
        return "ConcurrentBot";
    }

    @Override
    public String getBotToken() {
        return "1279337934:AAGCjnDbRaboqvcMugWhNODDzp1T50SxogQ";
    }

    private void sendMessage(Message msg, String text) {
        SendMessage sMObj = new SendMessage();
        sMObj.enableMarkdown(true);
        sMObj.setChatId(msg.getChatId());
        sMObj.setText(text);

        System.out.println(Phrases.SENDED_FROM_BOT + "\n" + '<' + text + '>');

        setKeyBoard(sMObj);
        try {
            execute(sMObj);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static Map<Long, User> uploadInfo(Path path) {
        Type mapType = new TypeToken<HashMap<Long, User>>() {
        }.getType();
        Map<Long, User> usersDb = Collections.emptyMap();
        try {
            usersDb = jsonSessionDeserializer.fromJson(new FileReader(path.toString()), mapType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return usersDb;
    }

    private static void recordInfo(Path path) {
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(jsonSessionDeserializer.toJson(sessionState));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
