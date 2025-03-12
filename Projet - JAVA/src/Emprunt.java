// Classe Emprunt qui utilise les états
public class Emprunt {
    private int id;
    private Livre livre;
    private Utilisateur utilisateur;
    private java.util.Date dateEmprunt;
    private java.util.Date dateRetour;
    private EtatEmprunt etat;

    public Emprunt(Livre livre, Utilisateur utilisateur) {
        this.livre = livre;
        this.utilisateur = utilisateur;
        this.dateEmprunt = new java.util.Date();
        this.etat = new EtatEnCours();
    }

    public void setEtat(EtatEmprunt etat) {
        this.etat = etat;
    }

    public void traiter() {
        etat.traiter(this);
    }

    public void setDateRetour(java.util.Date dateRetour) {
        this.dateRetour = dateRetour;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Livre getLivre() {
        return livre;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public java.util.Date getDateEmprunt() {
        return dateEmprunt;
    }

    public java.util.Date getDateRetour() {
        return dateRetour;
    }

    public String getEtatNom() {
        return etat.getNom();
    }
}
