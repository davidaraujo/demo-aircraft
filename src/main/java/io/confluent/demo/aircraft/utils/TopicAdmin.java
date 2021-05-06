package io.confluent.demo.aircraft.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class TopicAdmin {

    public static void createTopic(Properties props, String topicName) throws ExecutionException, InterruptedException {

        AdminClient adminClient = AdminClient.create(props);
        boolean topicExists = adminClient.listTopics().names().get().contains(topicName);

        if (!topicExists) {
            int partitions = new Integer(props.getProperty("num.partitions"));
            int replication = new Integer(props.getProperty("replication.factor"));
            NewTopic newTopic = new NewTopic(topicName, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }
    }
}
