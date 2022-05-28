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
     *  в переменных указан символ начала алфавита и длинна алфавита
     *  в множестве frequentChar содержатся наиболее частые буквы алфавитов применяемые в человекочитаемом тексте
     */
    static final  char fromKyr = 'Ё';
    static final  char countKyr = 81;
    static final  char fromEn = '!';
    static final  char countEn = 93;


    static final HashSet <Character> frequentChar = new HashSet<>(){{
        add('a');add('e');add('i');add('o');add('t');add('s');add('n');add('h');add('r');add(' ');
        add('о');add('е');add('а');add('и');add('н');add('т');add('с');add('р');add('в');add('л');
    }};


    /**
     *
     * @param fromFile - что шифруем
     * @param toFile - куда шифруем
     * @param key - чм шифруем
     * @param freqChar - если передать не null  то  тут окажется частота символов
     * @return   всегда возвращает true, если не выбросило исключение
     */
    public static boolean encodeFile(Path fromFile, Path toFile, int key, Map<Character,Integer> freqChar){
        if (fromFile == null || toFile == null) throw new IllegalArgumentException("Неправильно указаны пути к файлам");;
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(fromFile));
             OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(toFile))) {
            int length;
            char[] chars = new char[1024];
            while ((length = in.read(chars)) > 0) {
                codeCharArray(chars,length,key,freqChar);
                out.write(chars,0, length);
            }

        } catch (IOException ex) {
            throw new IllegalArgumentException( "Ошибка чтения/записи файла при шифровании ", ex);
        }
        return true;
    }

    public static boolean encodeFile(Path fromFile, Path toFile, int key){
       return encodeFile(fromFile,toFile,key,null);
    }

    private static char codeChar(char ch, int key){
            if (ch>=fromKyr && ch<fromKyr+countKyr){// кирилица utf-8
                return  (char)(((ch+key-fromKyr+countKyr*10)%(countKyr))+fromKyr);
            }else if (ch>=fromEn && ch<fromEn+countEn){ // big En
                return (char)(((ch+key-fromEn+countEn*10)%(countEn))+fromEn);
            }
            return ch;
    }


    private static void codeCharArray(char[] chars, int length, int key, Map<Character,Integer> freqChar){
        for(int i = 0; i<length; i++){
            chars[i] = codeChar(chars[i], key);
            if (freqChar!=null){
                char chLower = Character.toLowerCase(chars[i]);
                // считаем тут частоту символов
                if(freqChar.containsKey(chLower)){
                    freqChar.put(chLower,freqChar.get(chLower)+1);
                }else{
                    freqChar.put(chLower,1);
                }
            }
        }
    }

    public static int bruteForseFile(Path fromFilePath, Path toDirPath) {
        if (fromFilePath == null || toDirPath == null ) throw new IllegalArgumentException("Неправильно указаны пути к файлам");
        String fileName = fromFilePath.getFileName().toString();
        String ext="";
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0){
            ext = fileName.substring(fileName.lastIndexOf(".") );
            fileName=fileName.substring(0, fileName.lastIndexOf("."));
        }

        int maxPossible = 0;
        int possibleKey = 0;
        Map<Character,Integer> freqChar= new HashMap<>();

        for (int i = 0; i > (- countEn); i--) {
            freqChar.clear();
            Path newFile = toDirPath.resolve(fileName + " KEY=" + (-i) + ext);
            encodeFile(fromFilePath, newFile, i, freqChar);

            int possible =0;
            int charCount =freqChar.size();
            if (charCount>10)charCount = 10;

            for(int j=0; j<charCount; j++){
                char c = pollMaxValue(freqChar);
                if (frequentChar.contains(c))
                    possible++;
            }
            if (maxPossible<possible){
                maxPossible=possible;
                possibleKey = i;
            }
        }
        if(maxPossible>4) { //нашли наиболее вероятный ключ
            // encodeFile(fromFilePath, toDirPath.resolve("$" + maxPossible +" Key = "+(-possibleKey)+ fileName + ext), possibleKey );
            return -possibleKey;
        }
        return -1;
    }

    private static  char pollMaxValue(Map<Character,Integer> map){
        int maxValue = 0;
        char maxValChar = 0;
        for (Map.Entry<Character, Integer> entry: map.entrySet())  {
            if (entry.getValue()>maxValue){
                maxValue = entry.getValue();
                maxValChar = entry.getKey();
            }
        }
        map.remove(maxValChar);
        return maxValChar;
    }

    public static int encodeAnalysis(Path fromFilePath) {
        if (fromFilePath == null  )   throw new IllegalArgumentException("Неправильно указаны пути к файлам");;
        int maxPossible = 0;
        int possibleKey = 0;
        Map<Character,Integer> freqChar= new HashMap<>();
        for (int i = 0; i > (- countEn); i--) {
            freqChar.clear();
            calcCharInFileAfterDecode(fromFilePath,i,freqChar);
            int possible =0;
            int charCount = freqChar.size();
            if (charCount>10)charCount = 10;
            for(int j=0; j<charCount; j++){
                char c = pollMaxValue(freqChar);
                if (frequentChar.contains(c))
                    possible++;
            }
            if (maxPossible<possible){
                maxPossible=possible;
                possibleKey = i;
            }
        }
        if(maxPossible>4) { //нашли наиболее вероятный ключ
            return -possibleKey;
        }
        return -1;
    }

    private static void calcCharInFileAfterDecode(Path fromFilePath, int key, Map<Character, Integer> freqChar) {
        if (fromFilePath == null )  throw new IllegalArgumentException("Неправильно указаны пути к файлам");;
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(fromFilePath))) {
            int length;
            char[] chars = new char[1024];
            while ((length = in.read(chars)) > 0) {
                codeCharArray(chars,length,key,freqChar);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException( "Ошибка чтения файла ", ex);
        }
    }
}
