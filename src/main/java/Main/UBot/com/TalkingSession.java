package Main.UBot.com;

import org.telegram.telegrambots.meta.api.objects.Message;

public class TalkingSession extends Session{
    private boolean defMessageSent;
    @Override
    public String nextStep(String inputTxt, Message message) {
        inputTxt = inputTxt.toLowerCase();
        if(!defMessageSent){
            defMessageSent = true;
            return "Well, ok, lets talk, how are you?";
        }
        if(inputTxt.startsWith("ты") && inputTxt.split(" ").length == 2){
            return "Сам ты " + inputTxt.split(" ")[1];
        }else if(inputTxt.split(" ").length > 2)return "Я тебя уже выследил, сука\nЗа тобой едут";
        terminateAllProcesses();
        return "Отъебись сука";
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = defMessageSent = false;
    }
}
