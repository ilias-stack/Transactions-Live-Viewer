package ma.example.helpers;

import ma.example.entities.Transaction;

import java.util.Date;
import java.util.Random;

public class DataGenerator {
    private static final int USERS_NUM = 100;
    private static String[] userIds = new String[USERS_NUM];

    static {
        random = new Random();

        for (int i = 0; i < USERS_NUM; i++) {
            // Formatting the string into user001... user100 in a nutshell having all userIds be the same length
            StringBuilder userCountId = new StringBuilder(String.valueOf((i+1)));
            while (userCountId.length()<3){
                userCountId.insert(0, "0");
            }
            userIds[i] = "user"+userCountId;
        }
    }

    private static final Random random;

    private static float generateRandomAmount(int ceiling){
        return (float) Math.random()*ceiling + 1;
    }

    private static String generateRandomUserId(){
        int userIndex = random.nextInt(userIds.length);
        return userIds[userIndex];
    }

    public static Transaction generateRandomTransaction(){
        return Transaction.builder()
                .userId(generateRandomUserId())
                .amount(generateRandomAmount(100_000))
                .timestamp(new Date())
                .build();
    }

}
