package Main.UBot.com;

public class Operations {
    public static float levenshteinAlgo(String current, String input){
        if (current.equalsIgnoreCase(input))return 1f;
        current = current.toLowerCase();
        input = input.toLowerCase();
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
                int digit = 1;
                if(current.charAt(i - 1) == input.charAt(j - 1)){
                    digit = 0;
                }
                sum = new int[]{array[i][j - 1] + digit, array[i - 1][j - 1] + digit, array[i - 1][j] + digit};
                int minIndex = 0;
                for (int k = 0;k < sum.length;k++){
                    if(sum[minIndex] > sum[k]){
                        minIndex = k;
                    }
                }
                array[i][j] = sum[minIndex];
            }
        }
        int kef = array[array.length - 1][array[0].length - 1];
        System.out.println("Расстояние Левенштейна : " + kef);
        return (1.0f - ((float) kef / (float) current.length()));
    }
}
