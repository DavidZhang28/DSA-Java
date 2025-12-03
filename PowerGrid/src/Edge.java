import java.util.*;
import java.io.*;


public class Edge implements Comparable<Edge>
{
    public int from, to, weight;
    public String name;

    public Edge(int from, int to, int weight, String name)
    {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.name = name;
    }

    public int compareTo(Edge other)
    {
        if (this.weight == other.weight)
        {
            if (this.from == other.from)
            {
                if (this.to == other.to)
                {
                    return this.name.compareTo(other.name);
                }
                return this.to - other.to;
            }
            return this.from - other.from;
        }
        return this.weight - other.weight;
    }
}
