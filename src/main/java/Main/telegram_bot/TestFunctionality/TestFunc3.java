package Main.telegram_bot.TestFunctionality;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestFunc3 {
    public static void main(String[] args) {
        Arrays.stream(distributionOf(new int[]{10, 1000, 2})).forEach(o -> System.out.print(o + " "));
    }
    public static int[] distributionOf(int[] golds) {
        List<Integer> dist = Arrays.stream(golds).boxed().collect(Collectors.toList());
        int[] exit = new int[2];
        int index = 0;
        while (dist.size() != 0){
            if(dist.size() < 2){
                exit[index % 2] += dist.get(0);
                break;
            }
            exit[index++ % 2] += bestChoice(dist);
        }
        return exit;
    }
    private static int bestChoice(List<Integer> list){
        int max = Collections.max(list);
        List<Integer> fromLeft = list.subList(1, list.size());
        if(!fromLeft.get(0).equals(max) && !fromLeft.get(fromLeft.size() - 1).equals(max)){
            int exit = list.get(0);
            list.remove(0);
            return exit;
        }
        int exit = fromLeft.get(fromLeft.size() - 1);
        list.remove(Integer.valueOf(exit));
        return exit;
    }
}
