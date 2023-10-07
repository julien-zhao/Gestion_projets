import java.util.Date;

public class Binome {
    private int numeroProjet;
    private int numeroBinome;
    private Etudiant etudiant1;
    private Etudiant etudiant2;
    private float noteRapport;
    private float noteSoutenance;
    private Date dateRemiseEffective;

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

    public float calculerNoteFinale() {
        // Calcul de la note finale en fonction des notes de rapport et de soutenance,
        // et du nombre de points en moins par jour de retard
        // Vous devrez implémenter cette logique selon vos besoins.
        return 0.0f; // Remplacer par le calcul réel
    }
}
