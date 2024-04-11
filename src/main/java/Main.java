import controllers.LoginController;
import helper.JDBC;
import helper.UserActivityLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

import static javafx.application.Application.launch;

/**
 * The main class responsible for launching the application.
 */
public class Main extends Application{

    /**
     * The main entry point for the JavaFX application.
     * @param primaryStage The primary stage of the application.
     * @throws Exception If an error occurs during application startup.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        //determine locale
        Locale locale = determineLocale();

        //determine Resource  bundle based on locale
        ResourceBundle bundle = ResourceBundle.getBundle("language", locale);

        //Load the Login page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"), bundle);
        Parent root = loader.load();

        //set controller for the Login page
        LoginController controller = loader.getController();
        controller.setStage(primaryStage);

        //create our scene
        Scene scene = new Scene(root);

        // Action...
        primaryStage.setTitle("STUdev Scheduling - Login");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * Determines the locale based on the default locale.
     * @return The determined locale.
     */
    private Locale determineLocale() {
        Locale defaultLocale = Locale.getDefault();

        //Check where the default locale is
        if (defaultLocale.getCountry().equals("FR")) {
            System.out.println("The locale is french.");
            return new Locale("fr", "FR");
        } else {
            System.out.println("The locale is english.");
            return new Locale("en", "US");
        }
    }

    /**
     * The main method, responsible for launching the application.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        JDBC.openConnection();
        launch(args);
        JDBC.closeConnection();
    }
}
