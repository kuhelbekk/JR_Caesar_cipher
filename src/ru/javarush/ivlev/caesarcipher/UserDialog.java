package ru.javarush.ivlev.caesarcipher;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;

/**
 * Ивлев Алексей
 * Клас диалога для определения нужд пользователя
 */

public class UserDialog {
    private static final Set<String> YES = Set.of("y", "yes");
    private static final Set<String> NO = Set.of("n", "no");

    Scanner console;
    PrintStream outputStream;
    private static final String questionOpenFile = "Укажите путь к исходному файлу.";


    public UserDialog(InputStream in, PrintStream out) {
        this.console = new Scanner(in);
        this.outputStream = out;
    }


    public void startDialog() {
        if (questionYesNo("Нужно зашифровать файл? ")) {
            if (codeDialog(true)) {
                outputStream.println("Шифровка выполнена удачно.");
            }
            return;
        } else {
            if (questionYesNo("Нужно расшифровать файл?")) {
                if (questionYesNo("Знаете ключ для расшифровки?")) {
                    if (codeDialog(false)) {
                        outputStream.println("Расшифровка выполнена удачно.");
                    } else {

                    }
                } else {
                    // bruteforse
                    if (questionYesNo("Попробуем определить ключ статическим анализом?")) {
                        staticAnalizeDialog();
                    } else {
                        if (questionYesNo("Сохранить все возможные варианты расшифровки?")) {
                            bruteForseDialog();
                        }
                        return;
                    }
                }

            } else {
                outputStream.println("Извините, я умею только шифровать и расшифровывать.");

            }
        }

    }


    /**
     * @param question - Вопрос с предполагаемым ответом да или нет
     *                 ответ Y или N предполагается на английском
     * @return возвращает бинарный выбор пользователя
     */

    private boolean questionYesNo(String question) {
        for (int i = 0; i < 3; i++) {
            outputStream.println(question + "y/n (yes or no)");
            String ans = console.nextLine();
            if (YES.contains(ans) || "yes".equalsIgnoreCase(ans)) {
                return true;
            } else if ("n".equalsIgnoreCase(ans) || "no".equalsIgnoreCase(ans)) {
                return false;
            }
            outputStream.println("Я Вас не понимаю.");
        }
        return false;
    }


    /**
     * Диалог запроса пути файла
     *
     * @param question      - вопрос о назначении файла
     * @param createNewFile - нужно ли учитывать что файл будет создан
     * @return возвращает путь к файлу или null  если пользователь не определился
     */

    private Path requestFile(String question, boolean createNewFile) {
        Path filePath;
        for (int i = 0; i < 3; i++) {
            outputStream.println(question);
            String file = console.nextLine();
            if (file.length() > 0) {
                try {
                    filePath = Path.of(file);
                    if (checkFile(filePath, createNewFile)) {
                        return filePath;
                    }
                } catch (SecurityException ex) {
                    outputStream.println("Не хватает прав доступа, обратитесь к администратору." + ex.getMessage());
                }
            }
        }
        return null;
    }


    private boolean checkFile(Path filePath, boolean createNewFile){
        if (Files.exists(filePath)) {
            if (Files.isRegularFile(filePath)) {
                if (createNewFile) {
                    if (Files.isWritable(filePath)) {
                        if (questionYesNo("Файл существует. Перезаписать?")) {
                            return true;
                        }
                        return false;
                    }
                    outputStream.println("Нет прав на запись файла: " + filePath.toString());
                    return false;
                } else {
                    if (Files.isReadable(filePath)) {
                        return true;
                    }
                    outputStream.println("Нет прав на чтение файла: " + filePath.toString());
                    return false;
                }
            } else {
                outputStream.println("Вы указали не файл.");
            }
        } else {// нет файла
            if (createNewFile) {
                return true;
            }
            outputStream.println("Файл не найден!");
        }
        return false;
    }

    /**
     * Диалог запроса директории
     *
     * @param question - вопрос о назначении файла
     * @return возвращает путь  или null  если пользователь не определился
     */
    private Path requestDir(String question) {
        Path dirPath = null;
        for (int i = 0; i < 3; i++) {
            outputStream.println(question);
            String dir = console.nextLine();
            try {
                dirPath = Path.of(dir);
                if (Files.exists(dirPath)) {
                    if (Files.isDirectory(dirPath)) {
                        return dirPath;
                    } else {
                        outputStream.println("Не удается определить директорию. Проверте указанный путь.");
                    }
                } else {
                    if (questionYesNo("Директория: " + dirPath + " не найдена,  Попробовать создать?")) {
                        dirPath = Files.createDirectory(dirPath);
                        outputStream.println("Директория успешно создана");
                        return dirPath;
                    }
                }
            } catch (SecurityException | UnsupportedOperationException e) {
                outputStream.println("Не хватает прав, обратитеть к администратору." + e.getMessage());
            } catch (IOException e) {
                outputStream.println("Ошибка при обращении к директории" + e.getMessage());
            }
        }
        return null;
    }



    private boolean codeDialog(boolean isCode) {
        Path fromFilePath = requestFile(questionOpenFile, false);
        if (fromFilePath != null) {
            Path toFilePath = requestFile("Укажите путь для сохранения обработанного файла.", true);
            if (toFilePath != null) {
                for (int k = 0; k < 3; k++) {
                    outputStream.println("Укажите цифровой ключ");
                    if (console.hasNextInt()) {
                        int key = console.nextInt();
                        if (key <= 0) {
                            outputStream.println("Ключ должен быть положительным числом");
                            continue;
                        }
                        if (isCode) {
                            return Encapsulant.encodeFile(fromFilePath, toFilePath, key);
                        } else {
                            return Encapsulant.encodeFile(fromFilePath, toFilePath, key * -1);
                        }
                    } else {
                        console.next();
                    }
                }
            }
        }
        return false;
    }

    private void staticAnalizeDialog() {
        Path fromFilePath = requestFile(questionOpenFile, false);
        if (fromFilePath != null) {
            int key = Encapsulant.encodeAnalysis(fromFilePath);
            if (key > 0) {
                outputStream.println("Удалось определить ключ, key =" + key);
                Path toFilePath = requestFile("Укажите путь для сохранения расшифрованного файла.", true);
                if (toFilePath != null) {
                    if (Encapsulant.encodeFile(fromFilePath, toFilePath, -key)) {
                        outputStream.println("Файл сохранен.");
                    }
                }
            } else if (key == 0) {
                outputStream.println("Скорее всего файл не зашифрован");
            } else {
                outputStream.println("Расшифровать файл не удалось");
            }
        }
    }

    private boolean bruteForseDialog() {
        Path fromFilePath = requestFile(questionOpenFile, false);
        if (fromFilePath != null) {
            Path toDirPath = requestDir("Укажите директорию для сохранения результатов.");
            if (toDirPath != null) {
                if (Files.exists(toDirPath)) {
                    if (fileExistInDir(toDirPath)) {
                        if (questionYesNo("Файлы в директории  могут быть перезаписаны. Продолжить?")) {
                            return startBrutForce(fromFilePath, toDirPath);
                        } else {
                            return false;
                        }
                    } else {
                        return startBrutForce(fromFilePath, toDirPath);
                    }
                }
            }
        }
        return false;
    }

    private boolean startBrutForce(Path fromFilePath, Path toDirPath) {

        int keyOfBrutForce = Encapsulant.bruteForceFile(fromFilePath, toDirPath);
        if (keyOfBrutForce >= 0) {
            outputStream.println("Наиболее вероятный ключ = " + keyOfBrutForce);
            return true;
        }
        outputStream.println("Ключ статическим анализом определить не удалось");
        return false;
    }


    private boolean fileExistInDir(Path toDirPath) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(toDirPath)) {
            for (Path path : files)
                return true;
        } catch (IOException e) {
            outputStream.println("Ошибка доступа к директории: " + toDirPath);
            throw new IllegalArgumentException("Ошибка доступа к директории: " + toDirPath, e);
        }
        return false;
    }


}
