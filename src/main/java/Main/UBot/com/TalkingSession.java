package Main.UBot.com;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class TalkingSession extends Session {
    private boolean defMessageSent;
    private transient final List<String> buttons = new ArrayList<>(Arrays.asList("|HOME|", "Ты сука"));
    private transient final BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        inputTxt = inputTxt.toLowerCase();
        if(!defMessageSent){
            defMessageSent = true;
            return "Ну хуле, давай побазарим";
        }
        if(inputTxt.startsWith("ты") && inputTxt.split(" ").length == 2){
            return "Сам ты " + inputTxt.split(" ")[1];
        }else if(inputTxt.split(" ").length > 2)return "Я тебя уже выследил, сука\nЗа тобой едут";
        terminateAllProcesses();
        return "Отъебись сука";
    }

    @Override
    public BiConsumer<SendMessage, User> getButtonsMarkUp() {
        return buttonsMarkUp;
    }

    private void setButtons(SendMessage sendMessage, User user){
        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        rkm.setResizeKeyboard(true);
        KeyboardRow kRow = new KeyboardRow();
        kRow.addAll(buttons);
        rkm.setKeyboard(Arrays.asList(kRow));
        sendMessage.setReplyMarkup(rkm);
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = defMessageSent = false;
    }
}
