import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Translator
{
    private final List<Entry> dictionary = new ArrayList<>();

    public Translator(Path dictionaryPath) throws FileReadException, InvalidFileFormatException
    {
        List<String> lines;
        try
        {
            lines = Files.readAllLines(dictionaryPath, StandardCharsets.UTF_8);
        } catch (IOException | SecurityException e)
        {
            throw new FileReadException("Не удалось прочитать словарь: " + dictionaryPath, e);
        }

        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if (i == 0 && line.startsWith("\uFEFF"))
            {
                line = line.substring(1);
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank())
            {
                throw new InvalidFileFormatException("Неверный формат словаря в строке " + (i + 1)
                                                   + ". Ожидается: слово или выражение | перевод");
            }
            dictionary.add(new Entry(parts[0].strip(), parts[1].strip()));
        }
        dictionary.sort(Comparator.comparingInt((Entry entry) -> entry.source.length()).reversed());
    }

    public String translate(String text)
    {
        StringBuilder result = new StringBuilder();
        int position = 0;
        while (position < text.length())
        {
            boolean found = false;
            for (Entry entry : dictionary)
            {
                Matcher matcher = entry.pattern.matcher(text);
                matcher.region(position, text.length());
                matcher.useTransparentBounds(true);
                if (matcher.lookingAt())
                {
                    result.append(entry.translation);
                    position = matcher.end();
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                int codePoint = text.codePointAt(position);
                result.appendCodePoint(codePoint);
                position += Character.charCount(codePoint);
            }
        }
        return result.toString();
    }

    private static class Entry
    {
        private final String source;
        private final String translation;
        private final Pattern pattern;

        private Entry(String source, String translation)
        {
            this.source = source;
            this.translation = translation;
            String expression = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS)
                    .splitAsStream(source)
                    .map(Pattern::quote)
                    .collect(Collectors.joining("\\s+"));
            String wordCharacters = "[\\p{L}\\p{M}\\p{N}_]";
            pattern = Pattern.compile("(?<!" + wordCharacters + ")" + expression + "(?!" + wordCharacters + ")",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
        }
    }
}
