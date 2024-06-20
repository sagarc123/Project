package com.example;

import java.io.*;
import java.sql.*;
import java.sql.Date; // Import Date from java.sql package (to avoid conflict with java.util.Date)
import java.util.*;

/**
 * Game Store Management System
 * 
 * This program allows management of games based on genres. It can add new games,
 * update game details, display games by genre, search games, add new customers,
 * place orders, and exit the program.
 * 
 * Data is stored in different tables such as genres, games, customers, and orders.
 */
public class GameStore {
    private static final Scanner scanner = new Scanner(System.in); // Scanner object for user input
    private static Connection con = null; // Connection object for database connection

    public static void main(String[] args) {
        try {
            // Establish database connection
            String DB_URL = "jdbc:mysql://localhost:3306/games_db";
            String USER = "root";
            String PASS = "root";

            con = DriverManager.getConnection(DB_URL, USER, PASS); // Connect to the database
            con.setAutoCommit(true); // Set auto-commit to true

            // Instantiate the game store management object
            GameStore manager = new GameStore();

            // Check if tables exist, create if not
            manager.createDatabaseTables();

            // Run the main menu
            manager.mainMenu();

            // Close the database connection
            con.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details if connection or query fails
        }
    }

    /**
     * Method to create necessary database tables if they do not exist
     */
    private void createDatabaseTables() {
        try {
            Statement statement = con.createStatement(); // Create statement object

            // Create tables if they don't exist
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS genres (" +
                    "genre_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "genre_name VARCHAR(100) NOT NULL UNIQUE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS games (" +
                    "game_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "game_name VARCHAR(100) NOT NULL," +
                    "genre_id INT," +
                    "price DECIMAL(10,2)," +
                    "age_limit INT," +
                    "storage VARCHAR(50)," +
                    "FOREIGN KEY (genre_id) REFERENCES genres(genre_id)" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "customer_name VARCHAR(100) NOT NULL," +
                    "email VARCHAR(100) NOT NULL UNIQUE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS orders (" +
                    "order_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "customer_id INT," +
                    "game_id INT," +
                    "order_date DATE," +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
                    "FOREIGN KEY (game_id) REFERENCES games(game_id)" +
                    ")");

            // Populate genres table with initial data if it's empty
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM genres");
            resultSet.next();
            int count = resultSet.getInt(1);

            if (count == 0) {
                String[] genres = {"Action-adventure", "Battle royale game", "First-person shooter", "Horror", "RPG", "Sports", "Strategy", "Survival game", "Survival horror"};

                // Insert initial genres into the table
                for (String genre : genres) {
                    statement.executeUpdate("INSERT INTO genres (genre_name) VALUES ('" + genre + "')");
                }
            }

            statement.close(); // Close the statement
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details if table creation or population fails
        }
    }

    /**
     * Method to display the main menu and handle user input
     */
    private void mainMenu() {
        while (true) {
            // Display main menu options
            System.out.println("\n=== Game Store Management System ===");
            System.out.println("1. Add New Game");
            System.out.println("2. Update Game Details");
            System.out.println("3. Display Games by Genre");
            System.out.println("4. Add New Customer");
            System.out.println("5. Place Order");
            System.out.println("6. View Ordered Games");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine(); // Read user choice

            switch (choice) {
                case "1":
                    addNewGame(); // Call method to add a new game
                    break;
                case "2":
                    updateGameDetails(); // Call method to update game details
                    break;
                case "3":
                    displayGamesByGenre(); // Call method to display games by genre
                    break;
                case "4":
                    addNewCustomer(); // Call method to add a new customer
                    break;
                case "5":
                    placeOrder(); // Call method to place an order
                    break;
                case "6":
                    viewOrderedGames(); // Call method to view ordered games
                    break;
                case "7":
                    System.out.println("Exiting..."); // Exit the program
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a valid option."); // Invalid choice message
            }
        }
    }

    /**
     * Method to add a new game to the database
     */
    private void addNewGame() {
        try {
            System.out.println("\nAdding a New Game:");

            System.out.print("Enter Game Name: ");
            String gameName = scanner.nextLine(); // Read game name

            // Display available genres
            displayGenres();

            System.out.print("Enter Genre ID: ");
            int genreId = Integer.parseInt(scanner.nextLine()); // Read genre ID

            System.out.print("Enter Price: ");
            double price = Double.parseDouble(scanner.nextLine()); // Read price

            System.out.print("Enter Age Limit: ");
            int ageLimit = Integer.parseInt(scanner.nextLine()); // Read age limit

            System.out.print("Enter Storage Medium: ");
            String storage = scanner.nextLine(); // Read storage medium

            // Insert into games table
            String sql = "INSERT INTO games (game_name, genre_id, price, age_limit, storage) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setString(1, gameName); // Set game name parameter
                preparedStatement.setInt(2, genreId); // Set genre ID parameter
                preparedStatement.setDouble(3, price); // Set price parameter
                preparedStatement.setInt(4, ageLimit); // Set age limit parameter
                preparedStatement.setString(5, storage); // Set storage parameter

                preparedStatement.executeUpdate(); // Execute update
            }

            System.out.println("Game added successfully!"); // Success message
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace(); // Print SQL or number format exception details
        }
    }

    /**
     * Method to update game details in the database
     */
    private void updateGameDetails() {
        try {
            System.out.println("\nUpdating Game Details:");

            // Display all games with genres
            displayGamesWithGenres();

            System.out.print("Enter Game ID: ");
            int gameId = Integer.parseInt(scanner.nextLine()); // Read game ID

            System.out.print("Enter Field to Update (game_name, price, age_limit, storage): ");
            String field = scanner.nextLine(); // Read field to update

            System.out.print("Enter New Value: ");
            String newValue = scanner.nextLine(); // Read new value

            // Update game details
            String sql = "UPDATE games SET " + field + " = ? WHERE game_id = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setString(1, newValue); // Set new value parameter
                preparedStatement.setInt(2, gameId); // Set game ID parameter

                int affectedRows = preparedStatement.executeUpdate(); // Execute update
                if (affectedRows > 0) {
                    System.out.println("Game details updated successfully!"); // Success message
                } else {
                    System.out.println("Game not found with ID: " + gameId); // Game not found message
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace(); // Print SQL or number format exception details
        }
    }

    /**
     * Method to display games by a specific genre
     */
    private void displayGamesByGenre() {
        try {
            // Display available genres
            displayGenres();

            System.out.print("\nEnter Genre ID: ");
            int genreId = Integer.parseInt(scanner.nextLine()); // Read genre ID

            String sql = "SELECT * FROM games WHERE genre_id = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setInt(1, genreId); // Set genre ID parameter

                ResultSet resultSet = preparedStatement.executeQuery(); // Execute query
                displayGameResultSet(resultSet); // Display games from result set
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace(); // Print SQL or number format exception details
        }
    }

    /**
     * Method to add a new customer
     */
    private void addNewCustomer() {
        try {
            System.out.println("\nAdding a New Customer:");

            System.out.print("Enter Customer Name: ");
            String customerName = scanner.nextLine(); // Read customer name

            System.out.print("Enter Email: ");
            String email = scanner.nextLine(); // Read email

            // Insert into customers table
            String sql = "INSERT INTO customers (customer_name, email) " +
                    "VALUES (?, ?)";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, customerName); // Set customer name parameter
                preparedStatement.setString(2, email); // Set email parameter

                preparedStatement.executeUpdate(); // Execute update

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int customerId = generatedKeys.getInt(1);
                    displayCustomerDetails(customerId); // Display customer details
                }
            }

            System.out.println("Customer added successfully!"); // Success message
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details
        }
    }

    /**
     * Method to place an order
     */
    private void placeOrder() {
        try {
            System.out.println("\nPlacing an Order:");

            System.out.print("Enter Customer Name: ");
            String customerName = scanner.nextLine(); // Read customer name

            int customerId = getCustomerIdByName(customerName); // Get customer ID by name
            if (customerId == -1) {
                System.out.println("Customer not found. Please add the customer first."); // Customer not found message
                return;
            }

            // Display games by genre
            displayGamesWithGenres();

            System.out.print("Enter Game ID: ");
            int gameId = Integer.parseInt(scanner.nextLine()); // Read game ID

            // Insert into orders table
            String sql = "INSERT INTO orders (customer_id, game_id, order_date) " +
                    "VALUES (?, ?, NOW())";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setInt(1, customerId); // Set customer ID parameter
                preparedStatement.setInt(2, gameId); // Set game ID parameter

                preparedStatement.executeUpdate(); // Execute update
            }

            System.out.println("Order placed successfully!"); // Success message
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace(); // Print SQL or number format exception details
        }
    }

    /**
     * Method to view ordered games along with customer data
     */
    private void viewOrderedGames() {
        try {
            String sql = "SELECT o.order_id, c.customer_name, c.email, g.game_name, o.order_date " +
                         "FROM orders o " +
                         "JOIN customers c ON o.customer_id = c.customer_id " +
                         "JOIN games g ON o.game_id = g.game_id " +
                         "ORDER BY o.order_id ASC";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery(); // Execute query
                System.out.printf("\n%-10s %-20s %-30s %-30s %-15s\n", "Order ID", "Customer Name", "Email", "Game Name", "Order Date");
                System.out.println("--------------------------------------------------------------------------------------------");
                while (resultSet.next()) {
                    int orderId = resultSet.getInt("order_id"); // Get order ID from result set
                    String customerName = resultSet.getString("customer_name"); // Get customer name from result set
                    String email = resultSet.getString("email"); // Get email from result set
                    String gameName = resultSet.getString("game_name"); // Get game name from result set
                    Date orderDate = resultSet.getDate("order_date"); // Get order date from result set
                    System.out.printf("%-10d %-20s %-30s %-30s %-15s\n", orderId, customerName, email, gameName, orderDate); // Print formatted order details
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details
        }
    }

    // Helper methods to display data and fetch IDs

    /**
     * Method to display all genres
     */
    private void displayGenres() {
        try {
            String sql = "SELECT * FROM genres";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery(); // Execute query
                System.out.println("\nAvailable Genres:");
                while (resultSet.next()) {
                    int genreId = resultSet.getInt("genre_id"); // Get genre ID from result set
                    String genreName = resultSet.getString("genre_name"); // Get genre name from result set
                    System.out.printf("%d: %s\n", genreId, genreName); // Print formatted genre ID and name
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details
        }
    }

    /**
     * Method to display all games with genres
     */
    private void displayGamesWithGenres() {
        try {
            String sql = "SELECT g.game_id, g.game_name, g.price, g.age_limit, g.storage, gn.genre_name " +
                         "FROM games g " +
                         "JOIN genres gn ON g.genre_id = gn.genre_id";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery(); // Execute query
                System.out.printf("\n%-10s %-30s %-15s %-10s %-15s %-20s\n", "Game ID", "Game Name", "Price", "Age Limit", "Storage", "Genre");
                System.out.println("--------------------------------------------------------------------------------------------");
                while (resultSet.next()) {
                    int gameId = resultSet.getInt("game_id"); // Get game ID from result set
                    String gameName = resultSet.getString("game_name"); // Get game name from result set
                    double price = resultSet.getDouble("price"); // Get price from result set
                    int ageLimit = resultSet.getInt("age_limit"); // Get age limit from result set
                    String storage = resultSet.getString("storage"); // Get storage from result set
                    String genreName = resultSet.getString("genre_name"); // Get genre name from result set
                    System.out.printf("%-10d %-30s %-15.2f %-10d %-15s %-20s\n", gameId, gameName, price, ageLimit, storage, genreName); // Print formatted game details
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details
        }
    }

    /**
     * Method to display games from a result set
     * @param resultSet The result set containing game data
     * @throws SQLException if an SQL exception occurs
     */
    private void displayGameResultSet(ResultSet resultSet) throws SQLException {
        System.out.printf("\n%-10s %-30s %-15s %-10s %-15s\n", "Game ID", "Game Name", "Price", "Age Limit", "Storage");
        System.out.println("--------------------------------------------------------------------------");
        while (resultSet.next()) {
            int gameId = resultSet.getInt("game_id"); // Get game ID from result set
            String gameName = resultSet.getString("game_name"); // Get game name from result set
            double price = resultSet.getDouble("price"); // Get price from result set
            int ageLimit = resultSet.getInt("age_limit"); // Get age limit from result set
            String storage = resultSet.getString("storage"); // Get storage from result set
            System.out.printf("%-10d %-30s %-15.2f %-10d %-15s\n", gameId, gameName, price, ageLimit, storage); // Print formatted game details
        }
    }

    /**
     * Method to get the customer ID by customer name
     * @param customerName The name of the customer
     * @return The customer ID, or -1 if the customer is not found
     */
    private int getCustomerIdByName(String customerName) {
        try {
            String sql = "SELECT customer_id FROM customers WHERE customer_name = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setString(1, customerName); // Set customer name parameter
                ResultSet resultSet = preparedStatement.executeQuery(); // Execute query
                if (resultSet.next()) {
                    return resultSet.getInt("customer_id"); // Return customer ID from result set
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details
        }
        return -1; // Return -1 if customer not found
    }

    /**
     * Method to display customer details
     * @param customerId The ID of the customer
     */
    private void displayCustomerDetails(int customerId) {
        try {
            String sql = "SELECT * FROM customers WHERE customer_id = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setInt(1, customerId); // Set customer ID parameter
                ResultSet resultSet = preparedStatement.executeQuery(); // Execute query
                if (resultSet.next()) {
                    String customerName = resultSet.getString("customer_name"); // Get customer name from result set
                    String email = resultSet.getString("email"); // Get email from result set
                    System.out.printf("\nCustomer ID: %d\nCustomer Name: %s\nEmail: %s\n", customerId, customerName, email); // Print formatted customer details
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print SQL exception details
        }
    }
}
