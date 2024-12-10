package ma.example.entities;

import java.util.Date;

public class TransactionsBuilder {
    private String userId;
    private float amount;
    private Date timestamp;

    public TransactionsBuilder userId(String userId){
        this.userId = userId;
        return this;
    }

    public TransactionsBuilder amount(float amount){
        this.amount = amount;
        return this;
    }

    public TransactionsBuilder timestamp(Date timestamp){
        this.timestamp = timestamp;
        return this;
    }

    public Transaction build(){
        return new Transaction(userId,amount,timestamp);
    }

}
