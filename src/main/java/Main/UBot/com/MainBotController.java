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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainBotController extends TelegramLongPollingBot {
    private static final String URL_TO_DB = "src/main/resources/Data bases/sessionStates.json";
    private static final Gson jsonSessionDeserializer = new GsonBuilder().setPrettyPrinting().
            registerTypeAdapter(Session.class, new SessionDeserializer()).create();
    private static final Map<Long, User> sessionState;

    static {
        sessionState = uploadInfo(Paths.get(URL_TO_DB));
    }

    //References for sessions
    private final BiConsumer<Message, User> talkAboutWeather = this::talkAboutWeather;
    private final BiConsumer<Message, User> letsTalk = this::letsTalk;
    private final BiConsumer<Message, User> continueSession = this::continueSession;
    private final BiConsumer<Message, User> goHome = this::goHome;
    //References for keyboards
    private final Consumer<SendMessage> buttonsWeather = this::buttonsWeather;
    private final Consumer<SendMessage> buttonsLetsTalk = this::buttonsLetsTalk;
    private final Consumer<SendMessage> defaultKeyboard = this::setMainKeyBoard;
    private final Map<String, BiConsumer<Message, User>> middleOpers = Map.of("weather now", talkAboutWeather,
            "lets talk", letsTalk);

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String userMessage = message.getText().toLowerCase().trim();
        System.out.println("Активность от " + sessionState.get(message.getChatId()));
        User user = new User(message.getChatId());
        if (!sessionState.containsKey(user.getUserChatId())) {
            Chat userChat = message.getChat();
            user.setFirstName(userChat.getFirstName() != null ? userChat.getFirstName() : "Unknown");
            user.setLastName(userChat.getLastName() != null ? userChat.getLastName() : "Unknown");
            sessionState.put(message.getChatId(), user);
            //Recording new user to Json
            startBot(message);
            recordInfo(Paths.get(URL_TO_DB));
            System.out.println("Создался новый user, id=" + message.getChatId());
        } else {
            user = sessionState.get(message.getChatId());
            if (userMessage.equals("ok")) {
                sendMessage(message, "Choose operation or lets talk!", defaultKeyboard);
            }
            System.out.println("User from data base activated, id=" + user.getUserChatId());
        }
        if (user.getCurrentSession() == null || !user.getCurrentSession().isSessionOpened()) {
            if (middleOpers.get(userMessage) != null) middleOpers.get(userMessage).accept(message, user);
        } else {
            if (userMessage.equals("|home|")) goHome.accept(message, user);
            else {
                continueSession.accept(message, user);
                System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
            }
        }
    }

    private void buttonsWeather(SendMessage sendMessage) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        KeyboardRow homeRow = new KeyboardRow();
        if(Arrays.asList(WeatherSession.wrongNameCityMessage).contains(sendMessage.getText())){
            homeRow.add("Самара");
        }
        homeRow.add("|HOME|");
        rkm.setResizeKeyboard(true);
        rkm.setKeyboard(List.of(homeRow));
        sendMessage.setReplyMarkup(rkm);
    }

    private void buttonsLetsTalk(SendMessage sendMessage) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        KeyboardRow homeRow = new KeyboardRow();
        homeRow.add("|HOME|");
        rkm.setResizeKeyboard(true);
        rkm.setKeyboard(List.of(homeRow));
        sendMessage.setReplyMarkup(rkm);
    }

    private void talkAboutWeather(Message message, User user) {
        user.setCurrentSession(new WeatherSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message), buttonsWeather);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void letsTalk(Message message, User user) {
        user.setCurrentSession(new TalkingSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message), buttonsLetsTalk);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void continueSession(Message message, User user) {
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message), buttonsWeather);
        recordInfo(Paths.get(URL_TO_DB));
        if (!user.getCurrentSession().isSessionOpened())
            sendMessage(message, "Choose operation or lets talk!", defaultKeyboard);
    }


    private void startBot(Message msg) {
        ReplyKeyboardMarkup startMarkUp = new ReplyKeyboardMarkup();
        SendMessage startMsg = new SendMessage();
        String startMessage = "Hello! I am multi functionality bot. You can use my functional as how you want. \n" +
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

    private void goHome(Message message, User user) {
        user.getCurrentSession().terminateAllProcesses();
        sendMessage(message, "Choose operation or lets talk!", defaultKeyboard);
        recordInfo(Paths.get(URL_TO_DB));
        System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
    }

    private void setMainKeyBoard(SendMessage sM) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(false);

        KeyboardRow weatherKey = new KeyboardRow();
        weatherKey.add(new KeyboardButton("Weather now"));

        KeyboardRow letsTalk = new KeyboardRow();
        letsTalk.add("Lets talk");

        List<KeyboardRow> keys = new ArrayList<>();
        keys.add(weatherKey);
        keys.add(letsTalk);

        markup.setKeyboard(keys);
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

    private void sendMessage(Message msg, String text, Consumer<SendMessage> buttons) {
        SendMessage sMObj = new SendMessage();
        sMObj.enableMarkdown(true);
        sMObj.setChatId(msg.getChatId());
        sMObj.setText(text);

        System.out.println(Phrases.SENDED_FROM_BOT + "\n" + '<' + text + '>');
        //Sets keyboard to current session
        buttons.accept(sMObj);

        try {
            execute(sMObj);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static Map<Long, User> uploadInfo(Path path) {
        Type mapType = new TypeToken<HashMap<Long, User>>() {}.getType();
        Map<Long, User> usersDb = Collections.emptyMap();
        try (FileReader reader = new FileReader(path.toString())){
            usersDb = jsonSessionDeserializer.fromJson(reader, mapType);
        } catch (IOException e) {
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
