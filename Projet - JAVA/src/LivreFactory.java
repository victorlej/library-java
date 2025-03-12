public class LivreFactory {

    public static Livre creerLivre(String type, String titre, String auteur) {
        Livre livre = new Livre();
        livre.setTitre(titre);
        livre.setAuteur(auteur);

        switch(type.toLowerCase()) {
            case "roman":
                livre.setGenre("Roman");
                break;
            case "science":
                livre.setGenre("Science");
                break;
            case "bd":
                livre.setGenre("BD");
                break;
            case "poesie":
                livre.setGenre("Poésie");
                break;
            case "histoire":
                livre.setGenre("Histoire");
                break;
            default:
                livre.setGenre("Autre");
                break;
        }

        return livre;
    }
}