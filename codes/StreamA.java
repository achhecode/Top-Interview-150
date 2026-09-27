package codes;

import java.util.Arrays;
import java.util.List;

public class StreamA {
    public static void main(String[] args) {
        // 
    }

    public static void test(){
        List<Album> albums = Arrays.asList(
            new Album("1", "A", 1999),
            new Album("2", "B", 2001),
            new Album("3", "A", 2003)
        );

        List<String> result = albums.stream()
        .filter(album -> album.getYear() == 1999)
        .filter(album -> album.getGenre() == "A")
        .limit(5)
        .map(Album::getName)
        .sorted()
        .toList();

        result.forEach(System.out::println);
    }
}




