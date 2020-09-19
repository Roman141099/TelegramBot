package Main.UBot.com;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultLocation;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//1. Поправить кнопки городов
//2. Сделать удаление последних
//3. Привести обработку CallBack в норм вид
//4. Сделать переход не в главное меню настроек а назад
public class MainBotController extends TelegramLongPollingBot {
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
    private final Map<String, BiConsumer<Message, User>> middleOpers = Map.of("погода сейчас", currentWeather,
            "поговорим?", letsTalk, "настройки", settingsRequest, "прогноз на 7 дней", bigWeatherRequest);

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasCallbackQuery()){
            Pattern hasItem = Pattern.compile("<(.+?)>");
            CallbackQuery callbackQuery = update.getCallbackQuery();
            EditMessageText edm = new EditMessageText();
            EditMessageReplyMarkup edmk = new EditMessageReplyMarkup();
            edm.setChatId(callbackQuery.getMessage().getChatId()).
                    setMessageId(callbackQuery.getMessage().getMessageId());
            if(callbackQuery.getData().startsWith("/DetailsOn")){
                InlineKeyboardMarkup inlineButs = new InlineKeyboardMarkup();
                InlineKeyboardButton iButton = new InlineKeyboardButton("Short").
                        setCallbackData("/ShortOn" + callbackQuery.getData().substring(callbackQuery.getData().indexOf('<')));
                edmk.setChatId(callbackQuery.getMessage().getChatId());
                edmk.setMessageId(callbackQuery.getMessage().getMessageId());
                inlineButs.setKeyboard(List.of(List.of(iButton)));
                edmk.setReplyMarkup(inlineButs);
                Matcher matcher = hasItem.matcher(callbackQuery.getData());
                if(matcher.find())
                    edm.setText(Weather.getWeatherNow(matcher.group(1)).orElseThrow(NotFoundForecastException::new).toString());
            }else if(callbackQuery.getData().startsWith("/ShortOn")){
                InlineKeyboardMarkup inlineButs = new InlineKeyboardMarkup();
                InlineKeyboardButton iButton = new InlineKeyboardButton("Details").
                        setCallbackData("/DetailsOn" + callbackQuery.getData().substring(callbackQuery.getData().indexOf('<')));
                edmk.setChatId(callbackQuery.getMessage().getChatId());
                edmk.setMessageId(callbackQuery.getMessage().getMessageId());
                inlineButs.setKeyboard(List.of(List.of(iButton)));
                edmk.setReplyMarkup(inlineButs);
                Matcher matcher = hasItem.matcher(callbackQuery.getData());
                if(matcher.find())
                    edm.setText(Weather.getWeatherNow(matcher.group(1)).orElseThrow(NullPointerException::new).shortDescription());
            }
            try {
                execute(edm);
                execute(edmk);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }else if(update.hasMessage()) {
            Message message = update.getMessage();
            if(message.hasSticker() || message.hasDocument()){
                SendMessage sm = new SendMessage();
                sm.setText("Давай еще!");
                sm.setChatId(message.getChatId());
                SendVoice sv = new SendVoice();
                try {
                    sv.setVoice("Nice!", new FileInputStream("D:\\Рабочий стол\\nice.mp3"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                sv.setChatId(message.getChatId());
                try {
                    execute(sv);
                    execute(sm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            String userMessage = "";
            if(message.hasText())userMessage = message.getText().toLowerCase().trim();
//            System.out.println("Активность от " + sessionState.get(message.getChatId()));
            User user = new User(message.getChatId());
            if (!sessionState.containsKey(user.getUserChatId())) {
                Chat userChat = message.getChat();
                user.setFirstName(userChat.getFirstName() != null ? userChat.getFirstName() : "Unknown");
                user.setLastName(userChat.getLastName() != null ? userChat.getLastName() : "Unknown");
                sessionState.put(message.getChatId(), user);
                //Recording new user to Json
                startBot(message);
                recordInfo(Paths.get(URL_TO_DB));
//                System.out.println("Создался новый user, id=" + message.getChatId());
            } else {
                user = sessionState.get(message.getChatId());
                if (userMessage.equals("ok")) {
                    if (user.citiesList().size() == 0) {
                        user.setCurrentSession(new CityChoosingSession());
                    } else {
                        sendMessage(message, "Список моих возможностей⬇", defaultKeyboard);
                    }
                }
//                System.out.println("User from data base activated, id=" + user.getUserChatId());
            }
            if (user.getCurrentSession() == null || !user.getCurrentSession().isSessionOpened()) {
                if (middleOpers.get(userMessage) != null) middleOpers.get(userMessage).accept(message, user);
            } else {
                if (userMessage.equals("|home|")) goHome.accept(message, user);
                else {
                    continueSession.accept(message, user);
//                    System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
                }
            }
        }
    }

    private void buttonsWeather(SendMessage sendMessage, User user) {
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        KeyboardRow homeRow = new KeyboardRow();

        KeyboardRow locationRow = new KeyboardRow();
        KeyboardButton kb = new KeyboardButton();
        locationRow.add(kb);
        kb.setRequestLocation(true);
        kb.setText("\uD83D\uDDFAЛокация");


        homeRow.add("|HOME|");
        if(user.citiesList().size() == 0)rkm.setKeyboard(List.of(locationRow, homeRow));
        else if(user.citiesList().size() == 1){
            homeRow.add(user.citiesList().get(0));
            rkm.setKeyboard(List.of(locationRow, homeRow));
        }else if(user.citiesList().size() > 1){
            int[] indAndCount = {0,0};
            List<String> buttonsList = new ArrayList<>(user.citiesList());
            Map<Integer, List<String>> butsByIndex = buttonsList.stream().collect(Collectors.groupingBy(
                    o -> {
                        if(indAndCount[1] >= 2){
                            indAndCount[1] = 1;
                            return ++indAndCount[0];
                        }
                        indAndCount[1]++;
                        return indAndCount[0];
                    },
                    HashMap::new,
                    Collectors.toList()
            ));
            System.out.println(butsByIndex);
            List<KeyboardRow> boards = butsByIndex.values().stream().map(o -> {
                KeyboardRow kRow = new KeyboardRow();
                kRow.addAll(o);
                return kRow;
            }).collect(Collectors.toList());
            boards.add(0, homeRow);
            boards.add(0, locationRow);
            rkm.setKeyboard(boards);
        }
        rkm.setResizeKeyboard(true);
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



    private void settingsButtons(SendMessage sendMessage, User user){
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

    private void bigWeatherRequesting(Message message, User user){
        user.setCurrentSession(new DailyForecastSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsWeather);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void talkingRequesting(Message message, User user) {
        user.setCurrentSession(new TalkingSession());
        sendMessage(message, user.getCurrentSession().nextStep(message.getText(), message, user), buttonsLetsTalk);
        recordInfo(Paths.get(URL_TO_DB));
    }

    private void settingsRequesting(Message message, User user){
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
        String startMessage = "Привет! Я - твой бот. \n" +
                "Во время использования моих возможностей я могу сохранять всю предоставленную мне информацию в базу данных. Не стоит беспокоиться насчет этого, \n" +
                "я делаю это для статистики. Нажимая кнопку 'OK' ты подтверждаешь соглащения о предоставлении данных. Разработчик @romagrigoriew";

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
        sendMessage(message, "Список моих возможностей⬇", defaultKeyboard);
        recordInfo(Paths.get(URL_TO_DB));
//        System.out.println("Сессия открыта " + user.getCurrentSession().isSessionOpened());
    }

    private void setMainKeyBoard(SendMessage sM, User user) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(false);

        KeyboardRow weatherKey = new KeyboardRow();
        weatherKey.add(new KeyboardButton("Погода сейчас"));

        KeyboardRow letsTalk = new KeyboardRow();
        letsTalk.add("Поговорим?");

        KeyboardRow settings = new KeyboardRow();
        settings.add("Настройки");

        KeyboardRow dailyForecast = new KeyboardRow();
        dailyForecast.add("Прогноз на 7 дней");

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

//        System.out.println(Phrases.SENDED_FROM_BOT + "\n" + '<' + text + '>');
        //Sets keyboard to current session
        buttons.accept(sMObj, sessionState.get(msg.getChatId()));

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
        try (FileReader reader = new FileReader(path.toString())) {
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
