package ru.javarush.ivlev.caesarcipher;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Encapsulant {
    /**
     * Класс для шифрования
     * в константах указан символ начала алфавита и длинна алфавита
     * в множестве MOST_COMMON_CHAR содержатся наиболее частые буквы алфавитов применяемые в человекочитаемом тексте
     */
    private static final char START_CYRILLIC_CHAR = 'Ё';
    private static final char COUNT_CYRILLIC_CHAR = 81;
    private static final char START_ASCII_CHAR = '!';
    private static final char COUNT_ASCII_CHAR = 93;
    private static final Set<Character> MOST_COMMON_CHAR = new HashSet<>();
    static {
        MOST_COMMON_CHAR.add('a');
        MOST_COMMON_CHAR.add('e');
        MOST_COMMON_CHAR.add('i');
        MOST_COMMON_CHAR.add('o');
        MOST_COMMON_CHAR.add('t');
        MOST_COMMON_CHAR.add('s');
        MOST_COMMON_CHAR.add('n');
        MOST_COMMON_CHAR.add('h');
        MOST_COMMON_CHAR.add('r');
        MOST_COMMON_CHAR.add(' ');
        MOST_COMMON_CHAR.add('о');
        MOST_COMMON_CHAR.add('е');
        MOST_COMMON_CHAR.add('а');
        MOST_COMMON_CHAR.add('и');
        MOST_COMMON_CHAR.add('н');
        MOST_COMMON_CHAR.add('т');
        MOST_COMMON_CHAR.add('с');
        MOST_COMMON_CHAR.add('р');
        MOST_COMMON_CHAR.add('в');
        MOST_COMMON_CHAR.add('л');
    }
    private static final int BUFFER_SIZE = 1024;
    private static final int MAX_LETTERS_COUNT_FOR_CHECK = MOST_COMMON_CHAR.size()/2;
    private static final int MIN_COUNT_POPULAR_CHAR = 4;

    public static void encodeFile(Path fromFile, Path toFile, int key) {
        encodeFile(fromFile, toFile, key, null);
    }

    public static void encodeFile(Path fromFile, Path toFile, int key, Map<Character, Integer> symbolFrequency) {
        if (fromFile == null || toFile == null) throw new PathAccessException("Path is NULL");
        if (fromFile.equals(toFile))throw new PathAccessException("From and new paths is equals");
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(fromFile));
             OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(toFile))) {
            int length;
            char[] chars = new char[BUFFER_SIZE];
            while ((length = in.read(chars)) > 0) {
                encodeSymbols(chars, length, key, symbolFrequency);
                out.write(chars, 0, length);
            }
        } catch (IOException ex) {
            throw new PathAccessException("IO Exception read/write encoding", ex);
        }        
    }

    public static int bruteForceFile(Path fromFilePath){
        return bruteForceFile(fromFilePath,null);
    }

    public static int bruteForceFile(Path fromFilePath, Path toDirPath) {
        if (fromFilePath == null )
            throw new PathAccessException("Path is NULL");
        String fileName = getFileName (fromFilePath.getFileName().toString());
        String fileExt = getFileExtension(fromFilePath.getFileName().toString());
        int maxPossible = 0;
        int possibleKey = 0;

        for (int i = 0; i > (-COUNT_ASCII_CHAR); i--) {
            Map<Character, Integer> symbolFrequency = new HashMap<>();
            if(toDirPath==null) {
                calcCharInFileAfterDecode(fromFilePath, i, symbolFrequency);
            }else {
                Path newFile = toDirPath.resolve(fileName + " KEY=" + (-i) + fileExt);
                encodeFile(fromFilePath, newFile, i, symbolFrequency);
            }
            int possible = 0;
            int charCount = MAX_LETTERS_COUNT_FOR_CHECK ;
            if (symbolFrequency.size() < MAX_LETTERS_COUNT_FOR_CHECK) charCount = symbolFrequency.size();

            for (int j = 0; j < charCount; j++) {
                char c = pollMaxValue(symbolFrequency);
                if (MOST_COMMON_CHAR.contains(c))
                    possible++;
            }
            if (maxPossible < possible) {
                maxPossible = possible;
                possibleKey = i;
            }
        }
        if (maxPossible > MIN_COUNT_POPULAR_CHAR) {
            return -possibleKey;
        }
        return -1;
    }

    private static void encodeSymbols(char[] chars, int length, int key, Map<Character, Integer> symbolFrequency) {
        for (int i = 0; i < length; i++) {
            chars[i] = encodeSymbol(chars[i], key);
            if (symbolFrequency != null) {
                char chLower = Character.toLowerCase(chars[i]);
                if (symbolFrequency.containsKey(chLower)) {
                    symbolFrequency.put(chLower, symbolFrequency.get(chLower) + 1);
                } else {
                    symbolFrequency.put(chLower, 1);
                }
            }
        }
    }

    private static char encodeSymbol(char symbol, int key) {
        if (symbol >= START_CYRILLIC_CHAR && symbol < START_CYRILLIC_CHAR + COUNT_CYRILLIC_CHAR) {
            return (char) (((symbol + key - START_CYRILLIC_CHAR + COUNT_CYRILLIC_CHAR * 10) % (COUNT_CYRILLIC_CHAR)) + START_CYRILLIC_CHAR);
        } else if (symbol >= START_ASCII_CHAR && symbol < START_ASCII_CHAR + COUNT_ASCII_CHAR) {
            return (char) (((symbol + key - START_ASCII_CHAR + COUNT_ASCII_CHAR * 10) % (COUNT_ASCII_CHAR)) + START_ASCII_CHAR);
        }
        return symbol;
    }

    private static String getFileExtension(String fileName){
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return  fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    private static String getFileName(String fileName){
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
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

    private static void calcCharInFileAfterDecode(Path fromFilePath, int key, Map<Character, Integer> freqChar) {
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(fromFilePath))) {
            int length;
            char[] chars = new char[BUFFER_SIZE];
            while ((length = in.read(chars)) > 0) {
                encodeSymbols(chars, length, key, freqChar);
            }
        } catch (IOException ex) {
            throw new PathAccessException("IOException read file "+fromFilePath, ex);
        }
    }
}

