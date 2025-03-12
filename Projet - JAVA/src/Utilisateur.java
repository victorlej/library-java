public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private int nbEmprunts;

    // Constructeur par défaut
    public Utilisateur() {
        this.nbEmprunts = 0;
    }

    // Constructeur sans id (pour création)
    public Utilisateur(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.nbEmprunts = 0;
    }

    // Constructeur complet (pour chargement depuis BD)
    public Utilisateur(int id, String nom, String prenom, String email, int nbEmprunts) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.nbEmprunts = nbEmprunts;
    }

    // Méthodes métier
    public void emprunterLivre(Livre livre) throws Exception {
        if (!livre.isDisponible()) {
            throw new Exception("Le livre n'est pas disponible");
        }
        livre.setDisponible(false);
        this.nbEmprunts++;
    }

    public void rendreLivre(Livre livre) throws Exception {
        if (livre.isDisponible()) {
            throw new Exception("Ce livre n'était pas emprunté");
        }
        livre.setDisponible(true);
        this.nbEmprunts--;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNbEmprunts() {
        return nbEmprunts;
    }

    public void setNbEmprunts(int nbEmprunts) {
        this.nbEmprunts = nbEmprunts;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", nbEmprunts=" + nbEmprunts +
                '}';
    }
}