package madlibs;

import java.util.Iterator;

/**
 * Client program to test MyList interface and MyLinkedList class.
 * @author Brian S. Borowski
 * @version 1.0 September 27, 2022
 */
public class MyLinkedListTest {
    public static void main(String[] args) {
        MyList<String> strings = new MyLinkedList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            strings.add(String.valueOf(c));
        }
        Iterator<String> iter = strings.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
        for (int i = 0; i < strings.size(); i++) {
            strings.set(i, String.valueOf((char)('A' + 25 - i)));
        }
        strings.add("ZZZzzzz");
        iter = strings.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
        for (int i = 0; i < strings.size(); i++) {
            System.out.println(i + " " + strings.get(i));
        }
        strings.clear();
        System.out.println("List size after clearing: " + strings.size());
        System.out.println("Is list empty? " + strings.isEmpty());
    }
}
