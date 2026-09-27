package codes;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        test();
    }

    public static void test(){
        String name = "A R Danish kya haal h";
        Map<Character, Integer> map = new HashMap<>();

        for(char c: name.toCharArray()){
            if(c==' ') continue;
            if(map.containsKey(c)){
                map.put(c, map.get(c)+1);
            }else{
                map.put(c, 1);
            }
        }

        Iterator<Map.Entry<Character, Integer>> it = map.entrySet().iterator();

        while(it.hasNext()){
            // System.out.println(it.next().getKey()+" : "+it.next().getValue());
            Map.Entry<Character, Integer> entry = it.next();
            // System.out.println(entry.getKey()+" : "+entry.getValue());
        }

        for(Map.Entry<Character, Integer> entry: map.entrySet()){
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }

        System.out.println("Printing 3");

        for(Character c: map.keySet()){
            System.out.println(c+" : "+map.get(c));
        }


        map.forEach((key, value) -> System.out.println(key +" : "+value));

    }
}
