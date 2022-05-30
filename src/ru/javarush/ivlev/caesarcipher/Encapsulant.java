package ru.javarush.ivlev.caesarcipher;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Encapsulant {
    /**
     * Класс для шифрования
     * в переменных указан символ начала алфавита и длинна алфавита
     * в множестве frequentChar содержатся наиболее частые буквы алфавитов применяемые в человекочитаемом тексте
     */
    private static final char fromKyr = 'Ё';
    private static final char countKyr = 81;
    private static final char fromEn = '!';
    private static final char countEn = 93;


    static final HashSet<Character> frequentChar = new HashSet<>() {{
        add('a');
        add('e');
        add('i');
        add('o');
        add('t');
        add('s');
        add('n');
        add('h');
        add('r');
        add(' ');
        add('о');
        add('е');
        add('а');
        add('и');
        add('н');
        add('т');
        add('с');
        add('р');
        add('в');
        add('л');
    }};
    private static final int BUFFER_SIZE = 1024;
    private static final int MAX_LETTERS_COUNT_FOR_CHECK = 10;

    /**
     * @param fromFile - что шифруем
     * @param toFile   - куда шифруем
     * @param key      - чм шифруем
     * @param freqChar - если передать не null  то  тут окажется частота символов
     * @return всегда возвращает true, если не выбросило исключение
     */
    public static boolean encodeFile(Path fromFile, Path toFile, int key, Map<Character, Integer> freqChar) {
        if (fromFile == null || toFile == null) throw new PathAccessException("Неправильно указаны пути к файлам");
        ;
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(fromFile));
             OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(toFile))) {
            int length;
            char[] chars = new char[1024];
            while ((length = in.read(chars)) > 0) {
                codeCharArray(chars, length, key, freqChar);
                out.write(chars, 0, length);
            }

        } catch (IOException ex) {
            throw new PathAccessException("Ошибка чтения/записи файла при шифровании ", ex);
        }
        return true;
    }

    public static boolean encodeFile(Path fromFile, Path toFile, int key) {
        return encodeFile(fromFile, toFile, key, null);
    }

    private static char codeChar(char encodedСhar, int key) {
        if (encodedСhar >= fromKyr && encodedСhar < fromKyr + countKyr) {// кирилица utf-8
            return (char) (((encodedСhar + key - fromKyr + countKyr * 10) % (countKyr)) + fromKyr);
        } else if (encodedСhar >= fromEn && encodedСhar < fromEn + countEn) { // big En
            return (char) (((encodedСhar + key - fromEn + countEn * 10) % (countEn)) + fromEn);
        }
        return encodedСhar;
    }


    private static void codeCharArray(char[] chars, int length, int key, Map<Character, Integer> freqChar) {
        for (int i = 0; i < length; i++) {
            chars[i] = codeChar(chars[i], key);
            if (freqChar != null) {
                char chLower = Character.toLowerCase(chars[i]);
                // считаем тут частоту символов
                if (freqChar.containsKey(chLower)) {
                    freqChar.put(chLower, freqChar.get(chLower) + 1);
                } else {
                    freqChar.put(chLower, 1);
                }
            }
        }
    }


    private static String getFileExt(String fileName){
        String fileExt = "";
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            fileExt = fileName.substring(fileName.lastIndexOf("."));
        }
        return fileExt;
    }

    private static String getFileName(String fileName){
        String res = fileName;
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            res = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return res;
    }


    public static int bruteForceFile(Path fromFilePath, Path toDirPath) {
        if (fromFilePath == null || toDirPath == null)
            throw new PathAccessException("Неправильно указаны пути к файлам");
        String fileName = getFileName (fromFilePath.getFileName().toString());
        String fileExt = getFileExt(fromFilePath.getFileName().toString());

        int maxPossible = 0;
        int possibleKey = 0;
        Map<Character, Integer> freqChar = new HashMap<>();

        for (int i = 0; i > (-countEn); i--) {
            freqChar.clear();
            Path newFile = toDirPath.resolve(fileName + " KEY=" + (-i) + fileExt);
            encodeFile(fromFilePath, newFile, i, freqChar);

            int possible = 0;
            int charCount = freqChar.size();
            if (charCount > MAX_LETTERS_COUNT_FOR_CHECK) charCount = MAX_LETTERS_COUNT_FOR_CHECK;

            for (int j = 0; j < charCount; j++) {
                char c = pollMaxValue(freqChar);
                if (frequentChar.contains(c))
                    possible++;
            }
            if (maxPossible < possible) {
                maxPossible = possible;
                possibleKey = i;
            }
        }
        if (maxPossible > 4) { //нашли наиболее вероятный ключ
            //  не знаю, правильно ли сохранять удачно расшифрованный фаил отдельно или достаточно указать ключ для расшифровки
            // encodeFile(fromFilePath, toDirPath.resolve("$" + maxPossible +" Key = "+(-possibleKey)+ fileName + ext), possibleKey );
            return -possibleKey;
        }
        return -1;
    }

    private static char pollMaxValue(Map<Character, Integer> map) {
        int maxValue = 0;
        char maxValChar = 0;
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxValChar = entry.getKey();
            }
        }
        map.remove(maxValChar);
        return maxValChar;
    }

    public static int encodeAnalysis(Path fromFilePath) {
        if (fromFilePath == null) throw new PathAccessException("Неправильно указаны пути к файлам");
        int maxPossible = 0;
        int possibleKey = 0;
        Map<Character, Integer> freqChar = new HashMap<>();
        for (int i = 0; i > (-countEn); i--) {
            freqChar.clear();
            calcCharInFileAfterDecode(fromFilePath, i, freqChar);
            int possible = 0;
            int charCount = freqChar.size();
            if (charCount > MAX_LETTERS_COUNT_FOR_CHECK) charCount = MAX_LETTERS_COUNT_FOR_CHECK;
            for (int j = 0; j < charCount; j++) {
                char c = pollMaxValue(freqChar);
                if (frequentChar.contains(c))
                    possible++;
            }
            if (maxPossible < possible) {
                maxPossible = possible;
                possibleKey = i;
            }
        }
        if (maxPossible > 4) { //нашли наиболее вероятный ключ
            return -possibleKey;
        }
        return -1;
    }

    private static void calcCharInFileAfterDecode(Path fromFilePath, int key, Map<Character, Integer> freqChar) {
        if (fromFilePath == null) throw new PathAccessException("Неправильно указаны пути к файлам");
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(fromFilePath))) {
            int length;
            char[] chars = new char[BUFFER_SIZE];
            while ((length = in.read(chars)) > 0) {
                codeCharArray(chars, length, key, freqChar);
            }
        } catch (IOException ex) {
            throw new PathAccessException("Ошибка чтения файла ", ex);
        }
    }
}
