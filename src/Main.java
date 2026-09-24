import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        String fileName = args.length > 0 ? args[0] : "dictionary.txt";

        try
        {
            Translator translator = new Translator(Path.of(fileName));
            try (Scanner scanner = new Scanner(System.in))
            {
                System.out.println("Словарь загружен.");
                System.out.println("Введите текст для перевода или /exit для выхода.");
                while (true)
                {
                    System.out.print("> ");
                    if (!scanner.hasNextLine())
                    {
                        break;
                    }
                    String text = scanner.nextLine();
                    if (text.equalsIgnoreCase("/exit"))
                    {
                        break;
                    }
                    System.out.println(translator.translate(text));
                }
            }
        } catch (InvalidFileFormatException e)
        {
            System.err.println("Ошибка формата: " + e.getMessage());
        } catch (FileReadException e)
        {
            System.err.println("Ошибка чтения: " + e.getMessage());
        } catch (InvalidPathException e)
        {
            System.err.println("Некорректный путь к словарю: " + fileName);
        }
    }
}
