public class SquareRoot 
{   
    public static final double EPSILON = 1e-7;

    public static double sqrt(double num, double epsilon)
    {
        if (num == Double.NaN || num < 0)
            return Double.NaN;
        else if (num == Double.POSITIVE_INFINITY || num == 0)
            return num;
        else
        {
            /*
            Double currentGuess = num;
            Double previousGuess = Double.NEGATIVE_INFINITY;
            while (Math.abs(currentGuess - previousGuess) >= epsilon)
            {
                previousGuess = currentGuess;
                currentGuess = (previousGuess + num / previousGuess) * 0.5;
            }
            return currentGuess;
            */
            Double currentGuess = num;
            Double previousGuess;
            do
            {
                previousGuess = currentGuess;
                currentGuess = (previousGuess + num / previousGuess) * 0.5;
            } while (Math.abs(currentGuess - previousGuess) >= epsilon);
            return currentGuess;
        }
    }
    public static void main(String[] args) 
    {
        if (args.length < 1 || args.length > 2)
        {
            System.err.println("Usage: java SquareRoot <value> [epsilon]");
            System.exit(1);
        }
        Double num = 0.0;
        Double epsilon = EPSILON;
        try
        {
            num = Double.parseDouble(args[0]);
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error: Value argument must be a double.");
            System.exit(1);
        }
        if (args.length == 2)
        {
            try
            {
                epsilon = Double.parseDouble(args[1]);
            }
            catch (NumberFormatException e)
            {
                System.err.println("Error: Epsilon argument must be a positive double.");
                System.exit(1);
            }
        }
        if (epsilon <= 0)
        {
            System.err.println("Error: Epsilon argument must be a positive double.");
            System.exit(1);
        }
        Double answer = sqrt(num, epsilon);
        System.out.printf("%.8f\n", answer);
        System.exit(0);
    }
}
