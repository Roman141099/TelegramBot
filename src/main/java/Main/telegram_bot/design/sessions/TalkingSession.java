package Main.telegram_bot.design.sessions;

import Main.telegram_bot.design.utils.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
/**
Session doesn't works!
 */
public class TalkingSession extends Session {
    private boolean defMessageSent;
    private transient final List<String> buttons = new ArrayList<>(Arrays.asList("|HOME|"));
    private transient final BiConsumer<SendMessage, User> buttonsMarkUp = this::setButtons;
    @Override
    public String nextStep(String inputTxt, Message message, User user) {
        return null;
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
