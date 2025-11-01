package madlibs;

import java.io.*;
import java.net.*;
import java.util.*;

public class MadLibGenerator 
{
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    public static void main(String[] args)
    {
        String prompt = "Write a short madlib story with blanks for nouns, verbs, adjectives, and adverbs. Format the story so that the blanks are enclosed by brackets [ and ] and are in lowercase letters. For example, you might have a sentence like this: The [noun] jumped over the [adjective] log and [adverb] [verb] away. Please refrain from using first noun, last noun, etc. in the prompt. Make sure to only use the four options provided. Please refrain from outputting anything but the madlib story itself - thank you!";
        try
        {
            String response = ChatGPTInterface.generateChatGPTResponse(prompt);
            String copy = new String(response);
            MyList<String> promptList = new MyLinkedList<String>();
            MyList<String> responseList = new MyLinkedList<String>();
            while (copy.indexOf('[') != -1 && copy.indexOf(']') != -1)
            {
                int start = copy.indexOf('[');
                int end =  copy.indexOf(']');
                promptList.add(copy.substring(start + 1, end));
                copy = copy.substring(end + 1);
            }
            Iterator<String> it = promptList.iterator();
            System.out.println("*** ChatGPT Mad Lib Generator ***");
            Scanner scanner = new Scanner(System.in);
            while (it.hasNext())
            {
                System.out.print("Enter your response for '" + it.next() + "': ");
                System.out.flush();
                responseList.add(scanner.nextLine());
            }
            scanner.close();
            Iterator<String> inp = responseList.iterator();
            StringBuilder ans = new StringBuilder();
            while (inp.hasNext())
            {
                int start = response.indexOf('[');
                int end = response.indexOf(']');
                ans.append(response.substring(0, start));
                ans.append(GREEN).append(inp.next()).append(RESET);
                response = response.substring(end + 1);
            }
            ans.append(response);
            System.out.println(ans.toString());
        }
        catch (IOException e)
        {
            System.err.println("Error: not able to connect to ChatGPT.");
            System.exit(1);
        }
    }
}

class ChatGPTInterface 
{
    public static String generateChatGPTResponse(String prompt) throws IOException 
    {
        String urlString = "https://us.api.openai.com/v1/chat/completions",
        apiKey = "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",
        model = "gpt-4.1";
        try 
        {
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // The request body
            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Response from ChatGPT
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = br.readLine()) != null) 
            {
                response.append(line);
            }
            br.close();
            // Extract the message from the JSON response.
            return extractMessageFromJSONResponse(response.toString());
        } 
        catch (IOException e) 
        {
            throw new IOException("Cannot connect to ChatGPT at " + urlString + ".");
        }
    }

    private static String extractMessageFromJSONResponse(String response) 
    {
        int start = response.indexOf("content") + 11;
        return response.substring(start, response.indexOf("\"", start));
    }
}
