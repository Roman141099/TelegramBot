package Main.telegram_bot.design.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//Check case and spaces at left and right side!
public class Operations {
    public static float levenshteinAlgo(String current, String input){
        if (current.equals(input))return 1f;
        int[][] array = new int[current.length() + 1][input.length() + 1];
        for (int i = 0;i < array[0].length;i++){
            array[0][i] = i;
        }
        for (int i = 0;i < array.length;i++){
            array[i][0] = i;
        }
        for (int i = 1; i < array.length; i++) {
            int[] sum;
            for (int j = 1; j < array[i].length; j++) {
                int digit = current.charAt(i - 1) == input.charAt(j - 1) ? 0 : 1;
                sum = new int[]{array[i][j - 1] + digit, array[i - 1][j - 1] + digit, array[i - 1][j] + digit};
                array[i][j] = Collections.min(IntStream.of(sum).boxed().collect(Collectors.toList()));
            }
        }
        Arrays.stream(array).forEach(o -> System.out.println(Arrays.toString(o)));
        int kef = array[array.length - 1][array[0].length - 1];
        System.out.println("Расстояние Левенштейна : " + kef);
        return (1.0f - ((float) kef / (float) current.length()));
    }
}
