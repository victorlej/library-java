public class EtatEnRetard implements EtatEmprunt {
    @Override
    public void traiter(Emprunt emprunt) {
        // Des actions spécifiques aux emprunts en retard pourraient être effectuées ici
    }

    @Override
    public String getNom() {
        return "EN_RETARD";
    }
}
