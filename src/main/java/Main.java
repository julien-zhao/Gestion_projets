import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Date;

public class Main extends Application {
    public static void main(String[] args) {
        //ZHAO WENBO
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestion des Projets Étudiants");

        // Créez des éléments d'interface utilisateur (labels, boutons, champs de texte, etc.)
        Label labelEtudiants = new Label("Liste des Étudiants:");
        ListView<String> etudiantsListView = new ListView<>();

        Label labelFormations = new Label("Liste des Formations:");
        ListView<String> formationsListView = new ListView<>();

        Label labelProjets = new Label("Liste des Projets:");
        ListView<String> projetsListView = new ListView<>();

        Button ajouterEtudiantButton = new Button("Ajouter Étudiant");
        Button ajouterFormationButton = new Button("Ajouter Formation");
        Button ajouterProjetButton = new Button("Ajouter Projet");

        // Créez des dispositions pour organiser les éléments d'interface utilisateur
        VBox leftVBox = new VBox(labelEtudiants, etudiantsListView, labelFormations, formationsListView);
        VBox rightVBox = new VBox(labelProjets, projetsListView);

        HBox buttonsHBox = new HBox(ajouterEtudiantButton, ajouterFormationButton, ajouterProjetButton);

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(leftVBox);
        borderPane.setCenter(rightVBox);
        borderPane.setBottom(buttonsHBox);

        // Créez une scène avec la disposition
        Scene scene = new Scene(borderPane, 800, 600);

        // Configurez la scène et affichez-la
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
