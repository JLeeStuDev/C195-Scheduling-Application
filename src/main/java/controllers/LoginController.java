package controllers;

import helper.JDBC;
import helper.UserActivityLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Appointment;
import models.UserHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The LoginController class controls the login UI and authentication login.
 */
public class LoginController {

    //FXML Fields
    @FXML
    private TextField tfLoginUsername;

    @FXML
    private PasswordField pwfLoginPassword;

    @FXML
    private Label lblLoginLocation;

    @FXML
    private Label lblLoginLanguage;

    @FXML
    private Label lblLoginError;

    @FXML
    private Label lblLoginUsername;

    @FXML
    private Label lblLoginPassword;

    @FXML
    private Button btnLoginButton;

    //Initializers
    private ResourceBundle resourceBundle;

    private Stage stage;

    /**
     * Initializes the login UI components and sets the resource bundle.
     */
    public void initialize() {

        Locale locale = Locale.getDefault();
        resourceBundle = ResourceBundle.getBundle("language", locale);



        //Define Strings based on Resource Bundle
        String lblUsername = resourceBundle.getString("login.lblUsername");
        String lblPassword = resourceBundle.getString("login.lblPassword");
        String btnLogin = resourceBundle.getString("login.btnLogin");
        String lblLanguage = resourceBundle.getString("login.lblLanguage");
        String lblLocation = resourceBundle.getString("login.lblLocation");

        //set text based on resource Bundle
        lblLoginLanguage.setText(lblLanguage);
        lblLoginUsername.setText(lblUsername);
        lblLoginPassword.setText(lblPassword);
        btnLoginButton.setText(btnLogin);
        lblLoginLocation.setText(lblLocation + ZoneId.systemDefault());

        //Deactivate the error label
        lblLoginError.setVisible(false);

        setStage(stage);
    }

    /**
     * Sets the stage.
     *
     * @param stage The stage to set.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Handles the login process.
     */
    @FXML
    private void Login() {

        lblLoginError.setVisible(false);

        String username = tfLoginUsername.getText();
        String password = pwfLoginPassword.getText();

        // Open DB connection
        JDBC.openConnection();

        //SQL query
        String query = "SELECT * FROM users WHERE User_Name = ? AND Password  = ?";

        //try the connection and query
        try (Connection connection = JDBC.connection;
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //For Testing Purposes
                //lblLoginError.setVisible(true);
                //lblLoginError.setText("Login Successful for " + username);

                UserHandler userHandler = new UserHandler();

                userHandler.setLoggedInUser(username);

                loginSuccess(username);

                System.out.println("Login Attempt for " + username + " was successful.");

                checkForUpcomingAppointments(UserHandler.getUserIDByUsername(username));

                //Close the Login Screen
                stage.close();

                //Open the next stage - Main Menu
                openMainMenu();
            }
            else {
                loginFailed(username);

                String failure = resourceBundle.getString("Login.error.invalidCredentials");

                lblLoginError.setVisible(true);
                lblLoginError.setText(failure);
                System.out.println("Login Attempt for " + username + " was unsuccessful.");
            }
        }

        catch (SQLException e) {
            e.printStackTrace();

            loginFailed(username);

            lblLoginError.setVisible(true);
            lblLoginError.setText(resourceBundle.getString("Login.error.databaseError"));
        }

        finally {
            JDBC.closeConnection();
        }
    }

    /**
     * Opens the main menu.
     */
    private void openMainMenu() {

        try {
            //load the main menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainMenu.fxml"));
            Parent root = loader.load();

            //Create the main menu stage
            Stage mainMenuStage = new Stage();
            mainMenuStage.setTitle("STUdev Scheduling - Main Menu");
            mainMenuStage.setScene(new Scene(root));
            mainMenuStage.setMaximized(true);
            mainMenuStage.show();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for upcoming appointments and displays alerts if any.
     *
     * @param userID    The ID of the user.
     */
    private void checkForUpcomingAppointments(int userID) {

        List<Appointment> upcomingAppointments = Appointment.getUpcomingAppointmentsForUser(userID);

        LocalDateTime currentLocalTime = LocalDateTime.now();

        boolean isAppointmentWithin15Minutes = false;

        for (Appointment appointment : upcomingAppointments) {
            LocalDateTime appointmentDateTime = appointment.getStart();

            if (isWithin15Minutes(currentLocalTime, appointmentDateTime)) {
                displayAlert("Upcoming Appointment Alert", resourceBundle.getString("Login.Alert.UpcomingAppointment") +
                        resourceBundle.getString("Login.Alert.Appointment") + appointment.getAppointmentID() + "\n" +
                        resourceBundle.getString("Login.Alert.Date") + appointment.getStart().toLocalDate() + "\n" +
                        resourceBundle.getString("Login.Alert.Time") + appointment.getStart().toLocalTime());
                isAppointmentWithin15Minutes = true;
                break;
            }
        }
        if (!isAppointmentWithin15Minutes) {
            displayAlert("No Upcoming Appointments", resourceBundle.getString("Login.Alert.NoUpcomingAppointment"));
        }
    }

    /**
     * Logs the successful login activity.
     *
     * @param username  The username of the user.
     */
    public void loginSuccess(String username) {
        UserActivityLogger.logUserActivity(username, true);
    }

    /**
     * Logs the failed login activity.
     *
     * @param username The username of the user.
     */
    public void loginFailed(String username) {
        UserActivityLogger.logUserActivity(username, false);
    }

    /**
     * Checks if an appointment is within 15 minutes from the current time.
     *
     * @param currentTime       The current time.
     * @param appointmentTime   The time of the appointment.
     * @return                  True if the appointment is within 15 minutes, otherwise false.
     */
    private boolean isWithin15Minutes(LocalDateTime currentTime, LocalDateTime appointmentTime) {
        long diffInMinutes = java.time.Duration.between(currentTime, appointmentTime).toMinutes();
        return diffInMinutes >= 0 && diffInMinutes <= 15;
    }

    /**
     * Displays an alert with the given title and message.
     *
     * @param title     The title of the alert.
     * @param message   The message to be displayed in the alert.
     */
    private void displayAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}