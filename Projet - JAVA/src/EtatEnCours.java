// Implémentations des états
public class EtatEnCours implements EtatEmprunt {
    @Override
    public void traiter(Emprunt emprunt) {
        // Vérification si l'emprunt est en retard
        java.util.Date dateActuelle = new java.util.Date();
        java.util.Date dateEmprunt = emprunt.getDateEmprunt();

        // Considérer en retard après 14 jours
        long differenceEnMillis = dateActuelle.getTime() - dateEmprunt.getTime();
        long differenceEnJours = differenceEnMillis / (1000 * 60 * 60 * 24);

        if (differenceEnJours > 14) {
            emprunt.setEtat(new EtatEnRetard());
        }
    }

    @Override
    public String getNom() {
        return "EN_COURS";
    }
}
