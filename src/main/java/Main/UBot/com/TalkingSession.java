package Main.UBot.com;

public class TalkingSession extends Session{
    @Override
    public String nextStep(String inputTxt) {
        terminateAllProcesses();
        return "You are asshole";
    }

    @Override
    public void terminateAllProcesses() {
        sessionOpened = false;
    }
}
