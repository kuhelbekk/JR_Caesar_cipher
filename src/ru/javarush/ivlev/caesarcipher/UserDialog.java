package ru.javarush.ivlev.caesarcipher;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;

public class UserDialog {
    Scanner console;
    PrintStream outputStream;
    String questionCode = "Нужно зашифровать файл? д/н (да или нет)";
    String questionDecode = "Нужно расшифровать файл? д/н (да или нет)";
    String questionDecodeIsKey = "Знаете ключ для расшифровки? д/н (да или нет)";
    String questionDecodeIsBrute = "Попробуем расшифровать файл без ключа? д/н (да или нет)";
    String questionSaveDir = "Укажите путь к папке для сохранения результатов.";
    String infoBrute = "Создана папка с вариантами расшифрованных файлов наиболее вероятный с именем - Good.txt";
    String questionOpen = "Укажите путь к исходному файлу.";
    String questionSaveAs = "Укажите путь для сохранения обработанного файла.";
    String questionFileExist ="Файл существует. Перезаписать?";
    String questionFilesInDirExist ="Файлы в директории  могут быть перезаписаны. Продолжить?";
    String questionKey = "Укажите ключ";
    String exit1 = "Извините, я умею только шифровать и расшифровывать.";
    String notUndestend = "Я Вас не понимаю.";


    public UserDialog(InputStream in, PrintStream out) {
        this.console = new Scanner(in);
        this.outputStream =  out;
    }

    public void startDialog() {
        if (questionYesNo(questionCode)) {
            codeDialog(true);
            return;
        }else{
            if (questionYesNo(questionDecode)) {
                if (questionYesNo(questionDecodeIsKey)) {
                    codeDialog(false);
                }else {
                    // bruteforse
                    if (questionYesNo(questionDecodeIsBrute)) {
                        bruteforseDialog();
                    }else {
                        return;
                    }
                }
                return;
            }else{
                outputStream.println(exit1);
                return;
            }
        }

    }



    private boolean questionYesNo(String question){
        for(int i=0 ; i<3; i++){
            outputStream.println(question);
            String ans = console.nextLine();
            if ("д".equalsIgnoreCase(ans)||"l".equalsIgnoreCase(ans)) {
                return true;
            }else if( "н".equalsIgnoreCase(ans)||"y".equalsIgnoreCase(ans))  {
                return false;
            }
            outputStream.println(notUndestend);
        }
        return false;
    }




    private Path requestFile(String question, boolean createNewFile){
        Path filePath ;
        for (int i = 0; i < 3; i++) {
            outputStream.println(question);
            String file = console.nextLine();
            try {
                filePath = Path.of(file);

                if (Files.exists(filePath)){
                    if (createNewFile) {
                        if (questionYesNo(questionFileExist)) {
                            return filePath;
                        } else {
                            return null;// отказались перезаписать фаил
                        }
                    } else {

                        if( Files.isReadable(filePath) ) {
                            return filePath;
                        }else{
                            outputStream.println("Нет прав на чтение файла: " + filePath.toString());
                            continue;
                        }
                    }
                }else {// нет файла
                    if (createNewFile) {
                        return filePath;
                    } else { // нет файла
                        outputStream.println("Файл не найден!");
                        continue;
                    }
                }

            } catch (InvalidPathException ex) {
                throw new IllegalArgumentException("Path file is invalid: " + file,ex);
            }
        }
        return null;
    }

    private Path requestDir(String question) {
        Path dirPath = null;
        for (int i = 0; i < 3; i++) {
            outputStream.println(question);
            String dir = console.nextLine();
            try {
                dirPath = Path.of(dir);
                if (Files.isDirectory(dirPath)) {
                    if (Files.exists(dirPath)) {
                        return dirPath;
                    } else {
                         outputStream.println("Директория: "+dirPath +" будет создана.");
                         return dirPath;
                    }
                }else{
                    outputStream.println("Указана не директория!");
                    continue;
                }
            } catch (InvalidPathException ex) {
                throw new IllegalArgumentException("Path is invalid: " + dir,ex);
            }
        }
        return null;
    }


    private boolean codeDialog(boolean isCode) {
        Path fromFilePath = requestFile(questionOpen,false);//Path.of("fromFile.txt") ;//
        if (fromFilePath != null){
            Path toFilePath =  requestFile(questionSaveAs,true);// Path.of("fromFile2.txt") ;// requestFile(questionSaveAs,true);
            if (toFilePath != null) {
                for (int k = 0; k < 3; k++) {
                    outputStream.println(questionKey);
                    if (console.hasNextInt()) {
                        if (isCode){
                            return Encapsulant.encodeFile(fromFilePath, toFilePath,  console.nextInt());
                        }else{
                            return Encapsulant.encodeFile(fromFilePath, toFilePath,console.nextInt()*-1);
                        }
                    }
                }
            }
        }
        return false;
    }


    private boolean bruteforseDialog() {
        Path fromFilePath =  requestFile(questionSaveDir,false);
        if (fromFilePath != null){
            Path toDirPath =  requestDir(questionOpen);
            if (toDirPath != null) {
                 if (Files.exists(toDirPath)){
                     if (fileExistInDir(toDirPath)){
                        if(questionYesNo(questionFilesInDirExist)){
                            return Encapsulant.bruteForseFile(fromFilePath, toDirPath);
                        }else{
                            return false;
                        }
                     }
                 }
                 return Encapsulant.bruteForseFile(fromFilePath, toDirPath);
            }
        }
        return false;

    }

    private boolean fileExistInDir(Path toDirPath) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(toDirPath)) {
            for (Path path : files)
                return true;
        } catch (IOException e) {
            outputStream.println("Ошибка доступа к директории: " +toDirPath);
            throw new IllegalArgumentException("Ошибка доступа к директории: " +toDirPath,e);
        }
        return false;
    }


}
