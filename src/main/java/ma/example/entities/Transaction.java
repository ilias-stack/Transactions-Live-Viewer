package ma.example.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Transaction {
    private final  String userId;
    private final float amount;
    private final Date timestamp;
    private static final SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    public Transaction(String userId, float amount, Date timestamp) {
        this.userId = userId;
        this.amount = amount;
        this.timestamp = timestamp;
    }


    public String getUserId() {
        return userId;
    }

    public float getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public static TransactionsBuilder builder(){
        return new TransactionsBuilder();
    }

    @Override
    public String toString() {
        String formattedDate = dateFormat.format(timestamp);
        return "{" +
                "'userId':'" + userId + '\'' +
                ", 'amount':" + amount +
                ", 'timestamp':'" + formattedDate +
                "'}";
    }

    public static Transaction parseToObject(String jsonString) throws ParseException {
        String[] strAttributesArray = jsonString.replace("'","")
                .replace("{","").replace("}","").split(",");

        Object[] attributes = new Object[3];
        int i=0;
        for (var strAttribute : strAttributesArray) {
            String[] attributeValue = strAttribute.split(":");
            if (strAttribute.contains("amount")) attributes[i++] = Float.parseFloat(attributeValue[1]);
            else if (strAttribute.contains("timestamp"))
                attributes[i++]=dateFormat.parse(
                        String.join(":", Arrays.copyOfRange(attributeValue,1,attributes.length+1))
                ) ;
            else attributes[i++] = attributeValue[1];
        }
        return new Transaction((String) attributes[0],(float) attributes[1],(Date) attributes[2]);
    }
}
