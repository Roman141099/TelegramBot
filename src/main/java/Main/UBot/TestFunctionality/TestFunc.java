package Main.UBot.TestFunctionality;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class TestFunc {
    public static void main(String[] args) {
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        List<String> l = List.of("a", "b", "c");
        String list = g.toJson(l);
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> result = g.fromJson(list, type);
        System.out.println(result);
    }
}
