package io.confluent.demo.aircraft.utils;

import org.json.JSONObject;

public class PrettyPrint {

    public static void producerRecord(String clientId, String topicName, long partition, long offset, String key, String value) {
        System.out.print(ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_GREEN);
        System.out.printf(
                 clientId +
                        " producing record to topic " +
                        ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_PURPLE +
                        topicName +
                        ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_GREEN +
                        " partition [%d] @ offset %d" + ColouredSystemOutPrintln.ANSI_RESET,
                partition, offset);
        System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_BLUE);
        System.out.println("key = " + key);
        JSONObject json = new JSONObject(value);
        System.out.print("value = " + json.toString(8));
        System.out.println(ColouredSystemOutPrintln.ANSI_RESET);
    }

    public static void consumerRecord(String groupId, String clientId, String topicName, long partition, long offset, String key, String value) {
        System.out.print(ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_GREEN);
        System.out.printf(
                clientId +" @ " + groupId +
                        " consuming record from topic " +
                        ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_PURPLE +
                        topicName +
                        ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_GREEN +
                        " partition [%d] @ offset %d" + ColouredSystemOutPrintln.ANSI_RESET,
                partition, offset);
        if (groupId.equals("OnGroundService"))
            System.out.print("\n" + ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_YELLOW);
        else if (groupId.equals("InFlightService"))
            System.out.print("\n" + ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_CYAN);
        else
            System.out.print("\n" + ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_CYAN);

        System.out.println("key = " + key);
        JSONObject json = new JSONObject(value);
        System.out.print("value = " + json.toString(8));
        System.out.println(ColouredSystemOutPrintln.ANSI_RESET);
    }
}
