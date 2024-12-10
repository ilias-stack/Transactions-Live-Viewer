package ma.example.streams_processor;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.text.ParseException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
public class Consumer extends Thread {
    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    private OnMessageReception onMessageReception;
    private volatile boolean running = true;

    public Consumer(String bootstrapServers, String topic, String groupId) {
        this.topic = topic;

        // Kafka consumer configuration
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));
    }

    public void setOnMessageReception(OnMessageReception onMessageReception) {
        this.onMessageReception = onMessageReception;
    }

    @Override
    public void run() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                records.forEach(record -> {
                    try {
                        onMessageReception.process(record.key(), record.value());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });

                Thread.sleep(1000); // Sleep for 1s
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer thread interrupted.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    public void stopConsumer() {
        running = false;
        this.interrupt();
    }

    @FunctionalInterface
    public interface OnMessageReception {
        void process(String key, String value) throws ParseException;
    }
}