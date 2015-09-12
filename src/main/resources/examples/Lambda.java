import java.util.*;
import java.util.stream.*;

public class Lambda
{
    public static void main(String[] args)
    {
        List<String> items = new ArrayList<>();

        items.add("apple");
        items.add("banana");
        items.add("blueberry");
        items.add("cherry");
        items.add("pineapple");
        items.add("pear");
        items.add("strawberry");

        List<Object> filtered = items.stream().filter( item -> item.startsWith("b") ).collect(Collectors.toList());

        for (Object str : filtered)
        {
            System.out.println(str);
        }
    }
}