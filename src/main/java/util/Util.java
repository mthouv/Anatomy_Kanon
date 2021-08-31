package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;

public class Util {

    public static int getRandomNumberInRange(int min, int max, Random r) {
        return r.nextInt((max - min) + 1) + min;
    }


    public static float getRandomFloatInRange(int min, int max) {
        Random r = new Random();
        float x = min + r.nextFloat() * ((max - 1) - min) + 1;
        return x;
    }


    public static <T> void flushListToFile(String filepath, List<T> list) throws IOException {
        Path p = Paths.get(filepath);
        for (T t : list) {
            String lineToWrite = t.toString() + "\n";
            Files.write(p, lineToWrite.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }

}
