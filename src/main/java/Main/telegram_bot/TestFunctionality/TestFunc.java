package Main.telegram_bot.TestFunctionality;

import Main.telegram_bot.design.weather.BigWeatherForecast;
import com.google.gson.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

public class TestFunc{
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("localhost", 443);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        bw.write(s);
        bw.close();
    }
}
