import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

public class Main {
    private static final Character[] ILLEGAL_WINDOWS_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
    private static final String INTEGER_FILE_NAME = "integers.txt";
    private static final String FLOAT_FILE_NAME = "floats.txt";
    private static final String STRING_FILE_NAME = "strings.txt";

    public static void main(String[] args) {
        List<String> listOfStrings = new ArrayList<String>();
        List<Long> listOfIntegers = new ArrayList<Long>();
        List<Float> listOfFloats = new ArrayList<Float>();
        List<File> InputFiles = new ArrayList<File>();
        String outputPrefix = "";
        String outputDirectory = "";
        boolean shortStatistic = false;
        boolean fullStatistic = false;
        boolean appendMode = false;

        try {
            // чтение параметров
            String arg = "";
            for (int i = 0; i < args.length; i++) {
                arg = args[i];
                if (arg.matches("-.")) {
                    char key = Character.toLowerCase(arg.charAt(1));
                    if (key == 'o') {
                        if ((i + 1) >= args.length) {
                            throw new Exception("Не указан путь для опции \"o\".");
                        }
                        String directory = args[i + 1];
                        try {
                            Files.createDirectories(Paths.get(directory));
                            if (!directory.endsWith("/")) {
                                directory += "/";
                            }
                            outputDirectory = directory;
                            i++;
                        } catch (Exception e) {
                            throw new Exception("Не верно указан путь для опции \"o\": \"" + e.getMessage() + "\"");
                        }
                    } else if (key == 'p') {
                        if ((i + 1) >= args.length) {
                            throw new Exception("Не указан префикс для опции \"p\".");
                        }
                        if (!validateStringFilename(args[i + 1])) {
                            throw new Exception("Недопустимое наименование префикса для опции \"p\".");
                        }
                        outputPrefix = args[i + 1];
                        i++;
                    } else if (key == 'a') {
                        appendMode = true;
                    } else if (key == 's') {
                        shortStatistic = true;
                    } else if (key == 'f') {
                        fullStatistic = true;
                    } else {
                        throw new Exception("Неизвестная опция: \"" + key + "\"");
                    }
                } else {
                    File file = new File(arg);
                    if (file.exists() && !file.isDirectory()) {
                        InputFiles.add(file);
                    } else {
                        throw new Exception("Не найден файл: \"" + arg + "\"");
                    }
                }
            }

            // Чтение файлов
            List<BufferedReader> readers = new ArrayList<BufferedReader>();
            for (File file : InputFiles) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file.getName()), "UTF-8"));
                readers.add(reader);
            }
            String line;
            Long integerLine;
            Float floatLine;
            Iterator<BufferedReader> rIterator = readers.iterator();
            while (rIterator.hasNext()) {
                BufferedReader reader = rIterator.next();
                if ((line = reader.readLine()) != null) {
                    if ((integerLine = parseIntOrNull(line)) != null) {
                        listOfIntegers.add(integerLine);
                    } else if ((floatLine = parseFloatOrNull(line)) != null) {
                        listOfFloats.add(floatLine);
                    } else {
                        listOfStrings.add(line);
                    }
                } else {
                    reader.close();
                    rIterator.remove();
                    readers.remove(reader);
                }

                if (!rIterator.hasNext()) {
                    rIterator = readers.iterator();
                }
            }
            int intCount = listOfIntegers.size();
            int floatCount = listOfFloats.size();
            int stringCount = listOfStrings.size();

            // Запись в файлы
            if (intCount > 0) {
                writeToFile(listOfIntegers, outputDirectory + outputPrefix + INTEGER_FILE_NAME, appendMode);
            }
            if (floatCount > 0) {
                writeToFile(listOfFloats, outputDirectory + outputPrefix + FLOAT_FILE_NAME, appendMode);
            }
            if (stringCount > 0) {
                writeToFile(listOfStrings, outputDirectory + outputPrefix + STRING_FILE_NAME, appendMode);
            }

            // Вывод сообщения
            System.out.println("Сортировка успешно завершена!\n");
            if (fullStatistic) {
                System.out.println("Статистика:" +
                        "\nКоличество целых чисел:\t\t" + intCount +
                        (intCount > 0 ? "\nМинимальное значение:\t\t" + intListGetMin(listOfIntegers) +
                                "\nМаксимальное значение:\t\t" + intListGetMax(listOfIntegers) +
                                "\nСумма всех значений:\t\t" + intListGetSum(listOfIntegers) +
                                "\nСреднее значение:\t\t" + intListGetAverage(listOfIntegers)
                                : "")
                        +

                        "\n\nКоличество вещественных чисел:\t" + floatCount +
                        (floatCount > 0 ? "\nМинимальное значение:\t\t" + floatListGetMin(listOfFloats) +
                                "\nМаксимальное значение:\t\t" + floatListGetMax(listOfFloats) +
                                "\nСумма всех значений:\t\t" + floatListGetSum(listOfFloats) +
                                "\nСреднее значение:\t\t" + floatListGetAverage(listOfFloats)
                                : "")
                        +

                        "\n\nКоличество строк:\t\t" + stringCount +
                        (stringCount > 0 ? "\nРазмер самой короткой строки:\t" + stringListGetShortest(listOfStrings) +
                                "\nРазмер самой длинной строки:\t" + floatListGetLongest(listOfStrings)
                                : ""));
            } else if (shortStatistic) {
                System.out.println("Статистика:" +
                        "\nКоличество целых чисел:\t\t" + intCount +
                        "\nКоличество вещественных чисел:\t" + floatCount +
                        "\nКоличество строк:\t\t" + stringCount);
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    private static long intListGetMin(List<Long> list) {
        return Collections.min(list);
    }

    private static long intListGetMax(List<Long> list) {
        return Collections.max(list);
    }

    private static double intListGetSum(List<Long> list) {
        return list.stream().mapToDouble(Long::longValue).sum();
    }

    private static double intListGetAverage(List<Long> list) {
        return list.stream().mapToDouble(a -> a).average().orElse(Double.NaN);
    }

    private static float floatListGetMin(List<Float> list) {
        return Collections.min(list);
    }

    private static float floatListGetMax(List<Float> list) {
        return Collections.max(list);
    }

    private static float floatListGetSum(List<Float> list) {
        float sum = 0;
        for (Float val : list) {
            sum += val;
        }
        return sum;
    }

    private static float floatListGetAverage(List<Float> list) {
        return floatListGetSum(list) / list.size();
    }

    private static int stringListGetShortest(List<String> list) {
        return list.stream().mapToInt(String::length).min().orElse(0);
    }

    private static int floatListGetLongest(List<String> list) {
        return list.stream().mapToInt(String::length).max().orElse(0);
    }

    private static boolean validateStringFilename(String filename) {
        if (filename == null || filename.isEmpty() || filename.length() > 255) {
            return false;
        }
        return Arrays.stream(ILLEGAL_WINDOWS_CHARACTERS).noneMatch(ch -> filename.contains(ch.toString()));
    }

    private static void writeToFile(List<?> lines, String filePath, Boolean appendMode) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, appendMode));
        for (Object line : lines) {
            writer.write(line.toString() + "\n");
        }
        writer.close();
    }

    private static Long parseIntOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Float parseFloatOrNull(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}