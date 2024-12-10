package ma.example;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import ma.example.helpers.DataGenerator;
import ma.example.entities.Transaction;
import ma.example.streams_processor.Consumer;
import ma.example.streams_processor.Producer;
import ma.example.streams_processor.ProducersSingleton;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

public class Main {

    private static Consumer getConsumer(String topic, Consumer.OnMessageReception onMessageReception){
        Consumer consumer = new Consumer("localhost:9092", topic,"group-1");
        consumer.setOnMessageReception(onMessageReception);
        return consumer;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Producer transactionsProducer = ProducersSingleton.getTransactionsProducer();
        Producer fraudsProducer = ProducersSingleton.getFraudsProducer();

        // Initialize InfluxDB Client
        String influxToken = FileUtils.readFileToString(
                new File(Main.class.getClassLoader().getResource("token.txt").toURI()),
                "UTF-8"
        );
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086"
                ,influxToken.toCharArray(),
                "my-org",
                "my-bucket");
        WriteApi writeApi = influxDBClient.makeWriteApi();

        // transactions Consumer setup
        {
            Consumer transactionConsumer = getConsumer( "transactions-input",
                    (key, value) -> {
                        var transaction = Transaction.parseToObject(value);
                        String lineProtocol = String.format(Locale.US,"transactions,userId=%s amount=%f,timestamp=%d", key, transaction.getAmount(), transaction.getTimestamp().getTime());
                        writeApi.writeRecord(WritePrecision.MS,lineProtocol);

                        System.out.printf("Transaction saved: key=%s, value=%s%n", key, value);
                    });
            transactionConsumer.start();
        }

        // frauds Consumer setup
        {
            Consumer fraudsConsumer = getConsumer("fraud-alerts",
                    (key, value) -> {
                        var transaction = Transaction.parseToObject(value);
                        String lineProtocol = String.format(Locale.US,"frauds,userId=%s amount=%f,timestamp=%d", key, transaction.getAmount(), transaction.getTimestamp().getTime());
                        writeApi.writeRecord(WritePrecision.MS,lineProtocol);

                        System.out.printf("Fraud saved: key=%s, value=%s%n", key, value);
            });
            fraudsConsumer.start();
        }

        while (true) {
            Transaction t = DataGenerator.generateRandomTransaction();

            String key = t.getUserId();
            String value = t.toString();
            transactionsProducer.sendMessage(key, value);

            // Fraudulent transaction check
            if (t.getAmount() < 10_000) {
                fraudsProducer.sendMessage(key, value);
            }

            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                transactionsProducer.close();
                fraudsProducer.close();
                e.printStackTrace();
            }
        }
    }
}