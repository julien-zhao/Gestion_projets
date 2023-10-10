public class Etudiant {

    //Sttribut
    private int numero;
    private String nom;
    private String prenom;
    private Formation formation;

    //Constructeur
    public Etudiant(int numero, String nom, String prenom, Formation formation) {
        this.numero = numero;
        this.nom = nom;
        this.prenom = prenom;
        this.formation = formation;
    }

    // Getters et setters
    public int getNumero() {
        return numero;
    }
    public void setNumero(int numero) {
        this.numero = numero;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public Formation getFormation() {
        return formation;
    }
    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    //methode
}
