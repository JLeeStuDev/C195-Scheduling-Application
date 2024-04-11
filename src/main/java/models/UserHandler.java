package models;

import com.mysql.cj.protocol.Resultset;
import helper.JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles user-related operations such as setting and retrieving the logged-in user,
 * clearing the logged-in user, and retrieving the user ID by username.
 */
public class UserHandler {

    private static String loggedInUser;

    /**
     * Sets the logged-in user.
     * @param username The username of the logged-in user.
     */
    public void setLoggedInUser(String username) {
        loggedInUser = username;
    }

    /**
     * Gets the username of the logged-in user.
     * @return The username of the logged-in user.
     */
    public static String getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Retrieves the user ID associated with a given username.
     * @param loggedInUser The username of the user.
     * @return The user ID associated with the username, or -1 if not found.
     */
    public static int getUserIDByUsername(String loggedInUser) {

        int userID = -1;

        JDBC.openConnection();
        String query = "SELECT User_ID FROM users WHERE User_Name = ?";

        try {
            Connection connection = JDBC.connection;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, loggedInUser);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                userID = resultSet.getInt("User_ID");
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            JDBC.closeConnection();
        }

        return userID;
    }
}
