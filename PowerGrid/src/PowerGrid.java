import java.io.*;
import java.util.*;
import java.util.stream.*;


//Are we required to handle long-size weights?
//Are we required to handle the case where streets have no names? If so, does it matter how many spaces there are?
//Can we use trim to trim the names of everything if the answer to the questions above is yes?

public class PowerGrid 
{
    static List<Edge> PrimsAlgorithm(int nodes, List<List<Edge>> edges)
    {
        int[] used = new int[nodes + 1];
        Edge[] minDist = new Edge[nodes + 1];
        List<Edge> ans = new ArrayList<>();

        //loop through the algoirhtm nodes times. Then, check the used array.
        //If there are any unused nodes (used[i] == 0). then the graph is disconnected.

        //set up minDist with distances from node 1
        for (int i = 1; i < nodes + 1; i++)
        {
            minDist[i] = new Edge(1, i, Integer.MAX_VALUE, "");
        }

        //loop thorugh nodes times:
        //finds the minimum edge weight that has edges
        int curNode = 1;
        while (edges.get(curNode).isEmpty())
        {
            curNode++;
            if (curNode > nodes)
                return null;
        }

        for (int i = 0; i < nodes; i++)
        {
            used[curNode] = 1;
            //NOT USED TO SORT
            //used to find the shortest edge, which is added to our MST
            PriorityQueue<Edge> p = new PriorityQueue<>();
            for (Edge e : edges.get(curNode))
            {
                if (used[e.to] == 0 && e.weight < minDist[e.to].weight)
                {
                    minDist[e.to].weight = e.weight;
                    minDist[e.to].from = e.from;
                    minDist[e.to].name = e.name;
                }
            }
            for (int j = 1; j < nodes + 1; j++)
            {
                if (used[j] == 0 && minDist[j].weight != Integer.MAX_VALUE)
                    p.add(minDist[j]);
            }
            Edge shortestEdge = p.poll();
            if (shortestEdge != null)
            {
                ans.add(shortestEdge);
                curNode = shortestEdge.to;
            }
        }

        //check for a disconnected graph
        boolean works = true;
        for (int i = 1; i < nodes + 1; i++)
        {
            if (used[i] == 0)
            {
                works = false;
                break;
            }
        }
        if (works)
            return ans;
        return null;
    }

    public static void main(String[] args)
    {
        //Declare nodes, edges
        int nodes = 0;
        List<List<Edge>> edges = new ArrayList<>();

        if (args.length != 1)
        {
            System.err.println("Usage: java PowerGrid <input file>");
            System.exit(1);
        }

        String filename = args[0];
        BufferedReader reader = null;

        //try catch block to open the file
        try
        {
            reader = new BufferedReader(new FileReader(filename));
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: Cannot open file \'" + filename + "\'.");
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Error: An I/O error occured reading \'" + filename + "\'.");
            System.exit(1);
        }

        //try catch block to read the number of nodes from the first line
        try
        {
            String line = reader.readLine();
            try
            {
                nodes = Integer.parseInt(line.trim());
                if (nodes <= 0 || nodes > 1000)
                {
                    System.err.println("Error: Invalid number of vertices \'" + line + "\' on line 1.");
                    System.exit(1);
                }
            }
            catch (NumberFormatException e)
            {
                System.err.println("Error: Invalid number of vertices \'" + line + "\' on line 1.");
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            System.err.println("Error: An I/O error occured reading \'" + filename + "\'.");
            System.exit(1);
        }

        //creates each list in the edges list
        for (int i = 0; i < nodes + 1; i++)
            edges.add(new ArrayList<Edge>());

        //checks to make sure that the edge doesn't already exist
        int[][] adjacencyMatrix = new int[nodes + 1][nodes + 1];

        //try catch block to read edges from the file
        int lineNumber = 1;
        try
        {
            String line;
            int from = 0; int to = 0; int weight = 1;
            String name = "";
            while ((line = reader.readLine()) != null)
            {
                lineNumber++;
                String[] parts = line.split(",");
                if (parts.length != 4)
                {
                    System.err.println("Error: Invalid edge data \'" + line + "\' on line " + lineNumber + ".");
                    System.exit(1);
                }
                //try catch block for each of the edge parts respectively
                try
                {
                    from = Integer.parseInt(parts[0].trim());
                    if (from <= 0 || from > nodes)
                    {
                        System.err.println("Error: Starting vertex \'" + parts[0] + "\' on line " + lineNumber + " is not among valid values 1-" + nodes + ".");
                        System.exit(1);
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Error: Starting vertex \'" + parts[0] + "\' on line " + lineNumber + " is not among valid values 1-" + nodes + ".");
                    System.exit(1);
                }

                try
                {
                    to = Integer.parseInt(parts[1].trim());
                    if (to <= 0 || to > nodes)
                    {
                        System.err.println("Error: Ending vertex \'" + parts[1] + "\' on line " + lineNumber + " is not among valid values 1-" + nodes + ".");
                        System.exit(1);
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Error: Ending vertex \'" + parts[1] + "\' on line " + lineNumber + " is not among valid values 1-" + nodes + ".");
                    System.exit(1);
                }

                try
                {
                    weight = Integer.parseInt(parts[2].trim());
                    if (weight <= 0)
                    {
                        System.err.println("Error: Invalid edge weight \'" + parts[2] + "\' on line " + lineNumber + ".");
                        System.exit(1);
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Error: Invalid edge weight \'" + parts[2] + "\' on line " + lineNumber + ".");
                    System.exit(1);
                }

                name = parts[3].trim();

                //checks for duplicate edges
                if (adjacencyMatrix[from][to] != 0)
                {
                    System.err.println("Error: Duplicate edge \'" + line + "\' found on line " + lineNumber + ".");
                    System.exit(1);
                }

                //adds edge to the list and updates adjacency matrix
                //Edge edge = new Edge(from, to, weight, name);
                edges.get(from).add(new Edge(from, to, weight, name));
                edges.get(to).add(new Edge(to, from, weight, name));
                adjacencyMatrix[from][to] = 1;
                adjacencyMatrix[to][from] = 1;
            }
            reader.close();
            //call Prim's Algorithm
            List<Edge> mst = PrimsAlgorithm(nodes, edges);
            if (mst == null)
                System.out.println("No solution.");
            else
            {
                int totalWeight = 0;
                //turn all edges in mst into tuples, so the sort will be by street name.
                List<Tuple> tuples = new ArrayList<>();
                for (Edge e : mst)
                {
                    tuples.add(new Tuple(e.from, e.to, e.weight, e.name));
                    totalWeight += e.weight;
                }
                Collections.sort(tuples);
                System.out.println("Total wire length (meters): " + totalWeight);
                for (Tuple t : tuples)
                {
                    System.out.println(t.name + " [" + t.weight + "]");
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("Error: An I/O error occured reading \'" + filename + "\'.");
            System.exit(1);
        }
    }
}
