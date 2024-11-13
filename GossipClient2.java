import java.io.*;
import java.net.*;

public class GossipClient2 {
    public static void main(String[] args) throws Exception {
        Socket sock = new Socket("127.0.0.1", 1234);  // Connect to the server on port 1234
        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));  // Create reader to take user input
        PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);  // Create writer to send data to server

        String sm;

        System.out.println("Enter message and priority in the format: priority|message");

        while (true) {
            // Prompt for username before every message
            System.out.print("Enter your username: ");
            String username = keyRead.readLine();  // Ask for the username
            
            System.out.print("Enter message and priority (priority|message): ");
            sm = keyRead.readLine();  // Input format: "priority|message"

            // Validate the input format
            if (sm != null && sm.contains("|")) {
                String[] parts = sm.split("\\|", 2);
                if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                    // Format: "priority|username|message"
                    String formattedMessage = parts[0] + "|" + username + "|" + parts[1];
                    pw.println(formattedMessage);           // Send message to the server
                    pw.flush();               // Ensure the message is sent immediately
                    System.out.println("Message sent: " + formattedMessage);
                } else {
                    System.out.println("Invalid input. Please provide both priority and message.");
                }
            } else {
                System.out.println("Invalid format. Please use 'priority|message'.");
            }
        }
    }
}
