import java.util.Date;

public class Main {
    public static void main(String[] args) {
        System.out.println("Test PC ");
        Formation miageIf = new Formation(1, "MIAGE-IF", "Initiale");
        Etudiant etudiant1 = new Etudiant(101, "John", "Doe", miageIf);
        Etudiant etudiant2 = new Etudiant(102, "Jane", "Smith", miageIf);

        Projet projet1 = new Projet(1, "Entrepôt de données", "Sujet 1", new Date());
        Binome binome1 = new Binome(1, 1, etudiant1, etudiant2, 8.5f, 9.0f, new Date());
        projet1.ajouterBinome(binome1);
    }
}
