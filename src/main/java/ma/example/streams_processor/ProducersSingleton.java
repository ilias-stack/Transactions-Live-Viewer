package ma.example.streams_processor;

public class ProducersSingleton {
    private static Producer transactionsProducer;

    private static Producer fraudsProducer;

    public static Producer getTransactionsProducer() {
        if (transactionsProducer==null) transactionsProducer = new Producer("localhost:9092", "transactions-input");
        return transactionsProducer;
    }

    public static Producer getFraudsProducer() {
        if (fraudsProducer==null) fraudsProducer = new Producer("localhost:9092", "fraud-alerts");
        return fraudsProducer;
    }
}
