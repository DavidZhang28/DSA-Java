import java.util.*;

public class Tuple implements Comparable<Tuple>
{
    public int from, to, weight; 
    String name;

    public Tuple(int from, int to, int weight, String name)
    {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.name = name;
    }

    public int compareTo(Tuple other)
    {
        if (this.name.equals(other.name))
            {
            if (this.weight == other.weight)
            {
                if (this.from == other.from)
                {
                    return this.to - other.to;
                }
                return this.from - other.from;
            }
            return this.weight - other.weight;
        }
        return this.name.compareTo(other.name);
    }
}
