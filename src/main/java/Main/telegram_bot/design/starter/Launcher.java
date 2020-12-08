package Main.telegram_bot.design.starter;

import Main.telegram_bot.design.sessions.*;
import Main.telegram_bot.design.utils.Phrases;
import Main.telegram_bot.design.utils.SessionDeserializer;
import Main.telegram_bot.design.utils.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Launcher extends TelegramLongPollingBot{
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    private static final String URL_TO_DB = "src/main/resources/Data bases/sessionStates.json";
    private static final Gson jsonSessionDeserializer = new GsonBuilder().setPrettyPrinting().
            registerTypeAdapter(Session.class, new SessionDeserializer()).create();
    private static final Map<Long, User> sessionState;

    static {
        sessionState = uploadInfo(Paths.get(URL_TO_DB));
    }

    //References for sessions
    private final BiConsumer<Message, User> currentWeather = this::weatherRequesting;
    private final BiConsumer<Message, User> letsTalk = this::talkingRequesting;
    private final BiConsumer<Message, User> continueSession = this::continueRequesting;
    private final BiConsumer<Message, User> goHome = this::goHome;
    private final BiConsumer<Message, User> settingsRequest = this::settingsRequesting;
    private final BiConsumer<Message, User> bigWeatherRequest = this::bigWeatherRequesting;
    //References for keyboards
    private final BiConsumer<SendMessage, User> buttonsWeather = this::buttonsWeather;
    private final BiConsumer<SendMessage, User> buttonsLetsTalk = this::buttonsLetsTalk;
    private final BiConsumer<SendMessage, User> defaultKeyboard = this::setMainKeyBoard;
    private final BiConsumer<SendMessage, User> buttonsSettings = this::settingsButtons;
    private final Map<String, BiConsumer<Message, User>> middleOpers = Map.of("\uD83C\uDF24погода сейчас", currentWeather,
            "поговорим?", letsTalk, "⚙настройки", settingsRequest, "\uD83D\uDD2Eпрогноз на 7 дней", bigWeatherRequest);
    private final Map<String, Consumer<CallbackQuery>> inlineOpers = Map.of(
            "/DetailsOn", InlineSessionWeather::sendDetails,
            "/ShortOn", InlineSessionWeather::sendShort,
            "/DetailsOnBigForecast", InlineSessionWeather::sendDetailsOnBigForecast,
            "/ShortOnBigForecast", InlineSessionWeather::sendShortOnBigForecast);

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String beginRequest = callbackQuery.getData().substring(0, callbackQuery.getData().indexOf('<'));
            if (inlineOpers.containsKey(beginRequest)) {
                inlineOpers.get(beginRequest).accept(callbackQuery);
            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasSticker() || message.hasDocument()) {
                SendMessage sm = new SendMessage();
                sm.setText("Давай еще!");
                sm.setChatId(message.getChatId());
                SendVoice sv = new SendVoice();
                try {
                    sv.setVoice("Nice!", new FileInputStream("D:\\Рабочий стол\\nice.mp3"));
                } catch (FileNotFoundException e) {
                    LOG.error("Exception with reading mp3 file", e);
                }
                sv.setChatId(message.getChatId());
                try {
                    execute(sv);
                    execute(sm);
                } catch (TelegramApiException e) {
                    LOG.error("Exception with reply to sticker", e);
                }
            }
            String userMessage = "";
            if (message.hasText()) userMessage = message.getText().toLowerCase().trim();
            User user = new User(message.getChatId());
            if (!sessionState.containsKey(user.getUserChatId())) {
                Chat userChat = message.getChat();
                user.setFirstName(userChat.getFirstName() != null ? userChat.getFirstName() : "Unknown");
                user.setLastName(userChat.getLastName() != null ? userChat.getLastName() : "Unknown");
                sessionState.put(message.getChatId(), user);
                LOG.info("Creating new User id={}", message.getChatId());
                //Recording new user to Json
                startBot(message);
                recordInfo(Paths.get(URL_TO_DB));
            } else {
                user = sessionState.get(message.getChatId());
                LOG.info("User id={} found in database", user.getUserChatId());
                if (userMessage.equals("ok")) {
                    if (user.citiesList().size() == 0) {
                        user.setCurrentSession(new CityChoosingSession());
                    } else {
                        sendMessage(message, "Список моих возможностей⬇", defaultKeyboard);
                    }
                }
            }
            if (user.getCurrentSession() == null || !user.getCurrentSession().isSessionOpened()) {
                if (middleOpers.get(userMessage) != null) middleOpers.get(userMessage).accept(message, user);
            } else {
                if (userMessage.equals("|home|")) goHome.accept(message, user);
                else {
                    continueSession.accept(message, user);
                }
            }

        }
    }

    private void buttonsWeather(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        rkm.setResizeKeyboard(true);

        KeyboardRow homeLocation = new KeyboardRow();
        homeLocation.add("|HOME|");
        KeyboardButton kb = new KeyboardButton();
        homeLocation.add(kb);
        kb.setRequestLocation(true);
        kb.setText("\uD83D\uDDFAЛокация");

        List<KeyboardRow> rows = user.citiesList().stream().map(o -> {
            KeyboardRow kr = new KeyboardRow();
            kr.add(o.getDisplay_name());
            return kr;
        }).collect(Collectors.toList());
        rows.add(0, homeLocation);
        rkm.setKeyboard(rows);
        sendMessage.setReplyMarkup(rkm);
    }

    private void buttonsLetsTalk(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        KeyboardRow homeRow = new KeyboardRow();
        homeRow.add("|HOME|");
        rkm.setResizeKeyboard(true);
        rkm.setKeyboard(List.of(homeRow));
        sendMessage.setReplyMarkup(rkm);
    }


    private void settingsButtons(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        rkm.setResizeKeyboard(true);
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.addAll(Arrays.asList("Язык", "Настройки города"));
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.addAll(Arrays.asList("Режим бота", "Остальное"));
        KeyboardRow lastRow = new KeyboardRow();
        lastRow.add("|HOME|");
        rkm.setKeyboard(List.of(firstRow, secondRow, lastRow));
        sendMessage.setReplyMarkup(rkm);
    }

    private void weatherRequesting(Message message, User user) {
        user.setCurrentSession(new WeatherSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsWeather);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void bigWeatherRequesting(Message message, User user) {
        user.setCurrentSession(new DailyForecastSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsWeather);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void talkingRequesting(Message message, User user) {
        user.setCurrentSession(new TalkingSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsLetsTalk);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void settingsRequesting(Message message, User user) {
        user.setCurrentSession(new SettingsSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsSettings);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void continueRequesting(Message message, User user) {
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), user.getCurrentSession().getButtonsMarkUp());
        recordInfo(Paths.get(URL_TO_DB));
        if (!user.getCurrentSession().isSessionOpened())
            sendMessage(message, "Список моих возможностей⬇", defaultKeyboard);
    }

    private void startBot(Message msg) {
        ReplyKeyboardMarkup startMarkUp = new ReplyKeyboardMarkup();
        SendMessage startMsg = new SendMessage();
        String startMessage = """
                Привет! Я - твой бот.\s
                Во время использования моих возможностей я могу сохранять всю предоставленную мне информацию в базу данных. Не стоит беспокоиться насчет этого,\s
                я делаю это для статистики. Нажимая кнопку 'OK' ты подтверждаешь соглашения о предоставлении данных. Разработчик @romagrigoriew""";

        startMsg.setText(startMessage);

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
            LOG.error("Can't send start message");
        }
    }

    private void goHome(Message message, User user) {
        user.getCurrentSession().terminateAllProcesses();
        sendMessage(message, "Список моих возможностей⬇", defaultKeyboard);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void setMainKeyBoard(SendMessage sM, User user) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(false);

        KeyboardRow weatherKey = new KeyboardRow();
        weatherKey.add(new KeyboardButton("\uD83C\uDF24Погода сейчас"));

        KeyboardRow letsTalk = new KeyboardRow();
        letsTalk.add("Поговорим?");

        KeyboardRow settings = new KeyboardRow();
        settings.add("⚙Настройки");

        KeyboardRow dailyForecast = new KeyboardRow();
        dailyForecast.add("\uD83D\uDD2EПрогноз на 7 дней");

        List<KeyboardRow> keys = new ArrayList<>();
        keys.add(weatherKey);
        keys.add(letsTalk);
        keys.add(settings);
        keys.add(dailyForecast);

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

    private void sendMessage(Message msg, String text, BiConsumer<SendMessage, User> buttons) {
        SendMessage sMObj = new SendMessage();
        sMObj.enableMarkdown(true);
        sMObj.setChatId(msg.getChatId());
        sMObj.setText(text);

        //Sets keyboard to current session

        buttons.accept(sMObj, sessionState.get(msg.getChatId()));

        try {
            execute(sMObj);
        } catch (TelegramApiException e) {
            LOG.error("Can't send message");
        }
    }

    private static Map<Long, User> uploadInfo(Path path) {
        Type mapType = new TypeToken<HashMap<Long, User>>() {}.getType();
        Map<Long, User> usersDb = Collections.emptyMap();
        try (FileReader reader = new FileReader(path.toString())) {
            usersDb = jsonSessionDeserializer.fromJson(reader, mapType);
        } catch (IOException e) {
            LOG.error("Can't upload info to database", e);
        }
        return usersDb;
    }

    private static void recordInfo(Path path) {
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(jsonSessionDeserializer.toJson(sessionState));
        } catch (IOException e) {
            LOG.error("Can't record info to database", e);
        }
    }
}
