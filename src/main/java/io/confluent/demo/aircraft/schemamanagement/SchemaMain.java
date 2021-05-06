package io.confluent.demo.aircraft.schemamanagement;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class SchemaMain {

    private final static MediaType SCHEMA_CONTENT =
            MediaType.parse("application/vnd.schemaregistry.v1+json");

    // from https://dzone.com/articles/kafka-avro-serialization-and-the-schema-registry
    final static OkHttpClient client = new OkHttpClient();


    public static void postSchema(String schema, String subjectName) throws IOException {
        Request request = new Request.Builder()
                .post(RequestBody.create(SCHEMA_CONTENT, schema))
                .url(".../subjects/" + subjectName+ "/versions")
                .build();

        String output = client.newCall(request).execute().body().string();
        System.out.println("postSchema output: " + output);
    }


    public static void main(String args[]) {

        final OkHttpClient client = new OkHttpClient();



    }

}
