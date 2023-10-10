import java.util.Date;

public class Binome {

    //Attribut
    private int numeroProjet;
    private int numeroBinome;
    private Etudiant etudiant1;
    private Etudiant etudiant2;
    private float noteRapport;
    private float noteSoutenance;
    private Date dateRemiseEffective;

    //Constructeur
    public Binome(int numeroProjet, int numeroBinome, Etudiant etudiant1, Etudiant etudiant2, float noteRapport, float noteSoutenance, Date dateRemiseEffective) {
        this.numeroProjet = numeroProjet;
        this.numeroBinome = numeroBinome;
        this.etudiant1 = etudiant1;
        this.etudiant2 = etudiant2;
        this.noteRapport = noteRapport;
        this.noteSoutenance = noteSoutenance;
        this.dateRemiseEffective = dateRemiseEffective;
    }

    // Getters et setters
    public int getNumeroProjet() {
        return numeroProjet;
    }
    public void setNumeroProjet(int numeroProjet) {
        this.numeroProjet = numeroProjet;
    }
    public int getNumeroBinome() {
        return numeroBinome;
    }
    public void setNumeroBinome(int numeroBinome) {
        this.numeroBinome = numeroBinome;
    }
    public Etudiant getEtudiant1() {
        return etudiant1;
    }
    public void setEtudiant1(Etudiant etudiant1) {
        this.etudiant1 = etudiant1;
    }
    public Etudiant getEtudiant2() {
        return etudiant2;
    }
    public void setEtudiant2(Etudiant etudiant2) {
        this.etudiant2 = etudiant2;
    }
    public float getNoteRapport() {
        return noteRapport;
    }
    public void setNoteRapport(float noteRapport) {
        this.noteRapport = noteRapport;
    }
    public float getNoteSoutenance() {
        return noteSoutenance;
    }
    public void setNoteSoutenance(float noteSoutenance) {
        this.noteSoutenance = noteSoutenance;
    }
    public Date getDateRemiseEffective() {
        return dateRemiseEffective;
    }
    public void setDateRemiseEffective(Date dateRemiseEffective) {
        this.dateRemiseEffective = dateRemiseEffective;
    }

    //Methode
    public float calculerNoteFinale() {
        // Calcul de la note finale en fonction des notes de rapport et de soutenance,
        // et du nombre de points en moins par jour de retard
        // Vous devrez implémenter cette logique selon vos besoins.
        return 0.0f; // Remplacer par le calcul réel
    }
}
