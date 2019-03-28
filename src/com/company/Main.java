package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Pattern containsKeyPattern = Pattern.compile("^containsKey\\(([a-z]+)\\);$");
    private static Pattern getPattern = Pattern.compile("^get\\(\"([a-z]+)\"\\);");
    private static Pattern putPattern = Pattern.compile("^put\\(\"([a-z]+)\", (\\d+)\\);$");
    private static Pattern removePattern = Pattern.compile("^remove\\((\\d+)\\);$");
    private static Pattern allPattern = Pattern.compile("^all\\(\\);$");
    private static Pattern quitPattern = Pattern.compile("^quit\\(\\);$");

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        try (DistributedMap map = new DistributedMap("halo-czy-ktos-mnie-slyszy")) {
            Map<Pattern, BiConsumer<String, Integer>> patterns = Map.of(
                containsKeyPattern, (key, value) -> System.out.println(map.containsKey(key)),
                getPattern, (key, value) -> System.out.println(map.get(key)),
                putPattern, (key, value) -> {
                    map.put(key, value);
                    System.out.println(String.format("%s <- %d", key, value));
                },
                removePattern, (key, value) -> {
                    System.out.println(String.format("%s <- null", key));
                    map.remove(key);
                },
                allPattern, (key, value) -> {
                    Map<String, Integer> m = map.all();
                    for (Map.Entry<String, Integer> item : m.entrySet()) {
                        System.out.println(String.format("%s = %d", item.getKey(), item.getValue()));
                    }
                },
                quitPattern, (key, value) -> {
                    System.out.println("Quitting...");
                    map.close();
                    System.exit(0);
                }
            );

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = bufferedReader.readLine();
                patterns.forEach((pattern, action) -> {
                    Matcher m = pattern.matcher(line);
                    if (m.find()) {
                        action.accept(
                            m.groupCount() > 0 ? m.group(1) : null,
                            m.groupCount() > 1 ? Integer.parseInt(m.group(2)) : null
                        );
                    }
                });
            }
        }
    }
}
