public class EtatTermine implements EtatEmprunt {
    @Override
    public void traiter(Emprunt emprunt) {
        // Rien à faire, l'emprunt est terminé
    }

    @Override
    public String getNom() {
        return "TERMINE";
    }
}
