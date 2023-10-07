import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Projet {
    private int numero;
    private String matiere;
    private String sujet;
    private Date dateRemisePrevue;
    private List<Binome> binomes = new ArrayList<>();

    public Projet(int numero, String matiere, String sujet, Date dateRemisePrevue) {
        this.numero = numero;
        this.matiere = matiere;
        this.sujet = sujet;
        this.dateRemisePrevue = dateRemisePrevue;
    }

    public void ajouterBinome(Binome binome) {
        binomes.add(binome);
    }

    // Getters et setters

}
