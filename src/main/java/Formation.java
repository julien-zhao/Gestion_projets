public class Formation {
    private int numero;
    private String nom; //MIAGE-IF, MIAGE-ID ou MIAGE-SITN
    private String promotion; //Initial, En Alternance ou Formation Continue

    public Formation(int numero, String nom, String promotion) {
        this.numero = numero;
        this.nom = nom;
        this.promotion = promotion;
    }

    // Getters et setters
}