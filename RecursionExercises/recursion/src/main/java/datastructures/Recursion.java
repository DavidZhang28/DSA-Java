package datastructures;

/**
 * @author David Zhang
 * Programming Assignment 2 - Recursion exercises
 * COMS W3134
 *
 * Note: All methods must be written recursively. No credit will be given for
 * methods written without recursion, even if they produce the correct output.
 */
public class Recursion 
{

    /**
     * Returns the value of x * y, computed via recursive addition.
     * x is added y times. Both x and y are non-negative.
     * @param x  non-negative integer multiplicand 1
     * @param y  non-negative integer multiplicand 2
     * @return   x * y
     */
    public static int recursiveMultiplication(int x, int y) 
    {
        if (y == 0 || x == 0)
            return 0;
        return Math.abs(x) + recursiveMultiplication(Math.abs(x), Math.abs(y-1)) * (Math.abs(x) / x) * (Math.abs(y) / y);
    }

/******************************************************************************/
    /**
     * Reverses a string via recursion.
     * @param s  the non-null string to reverse
     * @return   a new string with the characters in reverse order
     */
    private static String reverseHelper(String s, int i)
    {
        if (i < 0)
            return "";
        return s.charAt(i) + reverseHelper(s, i-1);
    }
    
    public static String reverse(String s) 
    {
        return reverseHelper(s, s.length() - 1);
    }

/******************************************************************************/
    private static int maxHelper(int[] array, int index, int max) 
    {
        if (index >= array.length)
            return max;
        int val = Math.max(max, array[index]);
        return maxHelper(array, index+1, val);
    }

    /**
     * Returns the maximum value in the array.
     * Uses a helper method to do the recursion.
     * @param array  the array of integers to traverse
     * @return       the maximum value in the array
     */
    public static int max(int[] array) 
    {
        return maxHelper(array, 0, Integer.MIN_VALUE);
    }

/******************************************************************************/

    /**
     * Returns whether or not a string is a palindrome, a string that is
     * the same both forward and backward.
     * @param s  the string to process
     * @return   a boolean indicating if the string is a palindrome
     */
    public static boolean isPalindromeHelper(String s, int i)
    {
        int high = s.length() - 1 - i;
        if (high <= i)
            return true;
        if (!(s.charAt(i) == s.charAt(high)))
            return false;
        return isPalindromeHelper(s, i+1);
    }

    public static boolean isPalindrome(String s) 
    {
        return isPalindromeHelper(s, 0);
    }

/******************************************************************************/
    private static boolean memberHelper(int key, int[] array, int index) 
    {
        if (index >= array.length)
            return false;
        if (array[index] == key)
            return true;
        return memberHelper(key, array, index + 1);
    }

    /**
     * Returns whether or not the integer key is in the array of integers.
     * Uses a helper method to do the recursion.
     * @param key    the value to seek
     * @param array  the array to traverse
     * @return       a boolean indicating if the key is found in the array
     */
    public static boolean isMember(int key, int[] array) 
    {
        return memberHelper(key, array, 0);
    }

/******************************************************************************/
    /**
     * Returns a new string where identical chars that are adjacent
     * in the original string are separated from each other by a tilde '~'.
     * @param s  the string to process
     * @return   a new string where identical adjacent characters are separated
     *           by a tilde
     */
    private static String separateIdenticalHelper(String s, int i, StringBuilder b)
    {
        if (s.length() == 0)
            return s;
        if (i == s.length() - 1)
        {
            b.append(s.charAt(i));
            return b.toString();
        }
        if (s.charAt(i) == s.charAt(i+1))
        {
            b.append(s.charAt(i));
            b.append('~');
            return separateIdenticalHelper(s, i+1, b);
        }
        b.append(s.charAt(i));
        return separateIdenticalHelper(s, i+1, b);

    }

    public static String separateIdentical(String s) 
    {
        return separateIdenticalHelper(s, 0, new StringBuilder());
    }
}
