package ru.javarush.ivlev.caesarcipher;


import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Encapsulant {
    static final  char fromKyr = 'Ё';
    static final  char countKyr = 81;
    static final  char fromEn = '!';
    static final  char countEn = 93;



    public static boolean encodeFile(Path fromFile, Path toFile, int key){
        if (fromFile == null || toFile == null) return false;
        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(fromFile));
             OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(toFile))) {
            int length;
            char[] chars = new char[1024];
            while ((length = in.read(chars)) > 0) {
                codeCharArray(chars,length,key);
                out.write(chars, 0, length);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException( "Ошибка чтения/записи при шифровании ", ex);
        }
        return false;
    }


    private static void codeCharArray(char[] chars, int length, int key){
        for(int i = 0; i<length; i++){
            if (chars[i]>=fromKyr && chars[i]<fromKyr+countKyr){// кирилица utf-8
                chars[i]= (char)(((chars[i]+key-fromKyr)%(countKyr))+fromKyr);
            }else if (chars[i]>=fromEn && chars[i]<fromEn+countEn){ // big En
                chars[i]= (char)(((chars[i]+key-fromEn)%(countEn))+fromEn);
            }
        }
    }

    public static boolean bruteForseFile(Path fromFilePath, Path toDirPath) {

        Path fileName =  fromFilePath.getFileName();
        for (int i = 1; i < countEn; i++) {
            Path newFile = toDirPath.resolve(fileName);
            encodeFile(fromFilePath, newFile,i);

        }
        return false;
    }
}
