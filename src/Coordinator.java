// COMP1549 - Advanced Programming

// GROUP 67
// Group members:
// - Omith Chowdhury - 001236697
// - Daim Ahmed - 001223454
// - Mohammed Amiin Mohammed - 001223569
// - Tuong-Luan X Bach - 001232844
// - Zafer Ahmed - 001225733

// CODE FOR THE COORDINATOR

package src;

// This class manages the coordinator role within the chat application.
public class Coordinator {
    // A static variable to hold the reference to the current coordinator's ClientHandler.
    // Static because there's only one coordinator across all instances of this class.
    private static ClientHandler currentCoordinator;

    // A static and synchronized method to set the current coordinator.
    // It is synchronized to ensure thread safety, as multiple threads may attempt to update the coordinator concurrently.
    public static synchronized void setCoordinator(ClientHandler clientHandler) {
        currentCoordinator = clientHandler; // Update the current coordinator with the provided ClientHandler.
    }

    // A static and synchronized method to retrieve the current coordinator.
    // Returns the ClientHandler associated with the current coordinator.
    // Synchronization ensures thread-safe access to the currentCoordinator variable.
    public static synchronized ClientHandler getCurrentCoordinator() {
        return currentCoordinator; // Return the reference to the current coordinator's ClientHandler.
    }
}
