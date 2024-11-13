import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

class Message implements Comparable<Message> {
    private String content;
    private int priority;
    private String username;

    public Message(String content, int priority, String username) {
        this.content = content;
        this.priority = priority;
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public int getPriority() {
        return priority;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int compareTo(Message other) {
        // Manually compare priorities
        if (this.priority < other.priority) {
            return -1;  // this is higher priority
        } else if (this.priority > other.priority) {
            return 1;  // other is higher priority
        } else {
            return 0;  // they are equal in priority
        }
    }
}

// Custom priority queue class
class MyPriorityQueue {
    private ArrayList<Message> queue;

    public MyPriorityQueue() {
        queue = new ArrayList();
    }

    public void add(Message msg) {
        queue.add(msg);  // Add the message to the end of the list
        int currentIndex = queue.size() - 1;

        // Bubble up the new message to its correct position based on priority
        while (currentIndex > 0 && queue.get(currentIndex).compareTo(queue.get((currentIndex - 1) / 2)) < 0) {
            // Swap the current message with its parent in the heap
            Message temp = queue.get(currentIndex);
            queue.set(currentIndex, queue.get((currentIndex - 1) / 2));
            queue.set((currentIndex - 1) / 2, temp);
            currentIndex = (currentIndex - 1) / 2;
        }
    }

    public Message poll() {
        if (queue.isEmpty()) {
            return null;
        }

        // Take the root element (highest priority)
        Message result = queue.get(0);

        // Replace the root with the last element in the list
        Message lastElement = queue.remove(queue.size() - 1);

        if (queue.isEmpty()) {
            return result;
        }

        queue.set(0, lastElement);

        // Bubble down the new root to its correct position based on priority
        int currentIndex = 0;
        while (true) {
            int leftChildIndex = 2 * currentIndex + 1;
            int rightChildIndex = 2 * currentIndex + 2;
            int smallest = currentIndex;

            if (leftChildIndex < queue.size() && queue.get(leftChildIndex).compareTo(queue.get(smallest)) < 0) {
                smallest = leftChildIndex;
            }
            if (rightChildIndex < queue.size() && queue.get(rightChildIndex).compareTo(queue.get(smallest)) < 0) {
                smallest = rightChildIndex;
            }

            if (smallest != currentIndex) {
                // Swap the current element with the smallest child
                Message temp = queue.get(currentIndex);
                queue.set(currentIndex, queue.get(smallest));
                queue.set(smallest, temp);
                currentIndex = smallest;
            } else {
                break;
            }
        }

        return result;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

public class GossipServer2 {
    private static MyPriorityQueue messageQueue = new MyPriorityQueue();  // Use MyPriorityQueue instead of PriorityQueue

    public static void main(String[] args) throws Exception {
        ServerSocket servsoc = new ServerSocket(1234);  // Start server on port 1234
        System.out.println("Server is ready..");

        // Create a separate thread to accept user input in the server's console (Using anonymous class)
        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.print("Display: ");  // Prompt the user with "Display"
                    String command = scanner.nextLine().trim();  // Read the user input
                    if (command.equals("display")) {
                        displayQueue();  // Display the messages in priority order
                    } else {
                        System.out.println("Unknown command. Try 'display' to show messages.");
                    }
                }
            }
        });
        inputThread.start();

        // Accept multiple clients in a loop
        while (true) {
            Socket sock = servsoc.accept();  // Accept incoming client connections
            new ClientHandler(sock).start();  // Spawn a new thread for each client
        }
    }

    // Method to display messages in priority order
    private static void displayQueue() {
        System.out.println("Messages in Priority Order:");
        while (!messageQueue.isEmpty()) {
            Message msg = messageQueue.poll();  // Poll messages from the queue (highest priority first)
            System.out.println("Priority: " + msg.getPriority() + ", User: " + msg.getUsername() + ", Content: " + msg.getContent());
        }
    }

    // Handle each client connection
    private static class ClientHandler extends Thread {
        private Socket sock;

        public ClientHandler(Socket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            try {
                BufferedReader rRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));  // Create reader to read data from client

                String receivedMessage;
                while ((receivedMessage = rRead.readLine()) != null) {
                    if (receivedMessage.trim().isEmpty()) continue;

                    String[] parts = receivedMessage.split("\\|", 3);  // Expect "priority|username|message"
                    if (parts.length == 3) {
                        try {
                            int priority = Integer.parseInt(parts[0].trim());  // Parse priority
                            String username = parts[1].trim();  // Get the username
                            String content = parts[2].trim();  // Get the message content

                            // Create message object and add it to the priority queue
                            Message message = new Message(content, priority, username);
                            messageQueue.add(message);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid priority format. Skipping message: " + receivedMessage);
                        }
                    } else {
                        System.out.println("Invalid message format. Skipping: " + receivedMessage);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading messages from client: " + e.getMessage());
            }
        }
    }
}
