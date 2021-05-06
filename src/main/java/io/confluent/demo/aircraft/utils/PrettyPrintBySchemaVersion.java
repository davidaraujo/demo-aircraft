package io.confluent.demo.aircraft.utils;

import org.json.JSONObject;

public class PrettyPrintBySchemaVersion {

    public static void json(int schemaVersion, String key, String value) {
        if (schemaVersion == 1)
            System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_BLUE);
        else if (schemaVersion == 2)
            System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_PURPLE);
        else if (schemaVersion == 3)
            System.out.print("\n" + ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_YELLOW);
        else
            System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_BLUE);

        System.out.println("key = " + key);
        JSONObject json = new JSONObject(value);
        System.out.print("value = " + json.toString(8));

        System.out.println(ColouredSystemOutPrintln.ANSI_RESET);
    }
}
