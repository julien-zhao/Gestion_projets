import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Projet {

    //Attribut
    private int numero;
    private String matiere;
    private String sujet;
    private Date dateRemisePrevue;
    private List<Binome> binomes = new ArrayList<>();

    //Constructeur
    public Projet(int numero, String matiere, String sujet, Date dateRemisePrevue) {
        this.numero = numero;
        this.matiere = matiere;
        this.sujet = sujet;
        this.dateRemisePrevue = dateRemisePrevue;
    }


    // Getters et setters
    public int getNumero() {
        return numero;
    }
    public void setNumero(int numero) {
        this.numero = numero;
    }
    public String getMatiere() {
        return matiere;
    }
    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }
    public String getSujet() {
        return sujet;
    }
    public void setSujet(String sujet) {
        this.sujet = sujet;
    }
    public Date getDateRemisePrevue() {
        return dateRemisePrevue;
    }
    public void setDateRemisePrevue(Date dateRemisePrevue) {
        this.dateRemisePrevue = dateRemisePrevue;
    }
    public List<Binome> getBinomes() {
        return binomes;
    }
    public void setBinomes(List<Binome> binomes) {
        this.binomes = binomes;
    }

    //methode
    public void ajouterBinome(Binome binome) {
        binomes.add(binome);
    }



}
