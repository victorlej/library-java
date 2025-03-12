import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class Bibliotheque {
    private static Bibliotheque instance;
    private List<Livre> livres;
    private List<Utilisateur> utilisateurs;
    private Connection connection;

    // Constructeur privé (pattern Singleton)
    private Bibliotheque() {
        this.livres = new ArrayList<>();
        this.utilisateurs = new ArrayList<>();
        try {
            this.initDatabase();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
        }
    }

    // Méthode statique pour obtenir l'instance unique
    public static synchronized Bibliotheque getInstance() {
        if (instance == null) {
            instance = new Bibliotheque();
        }
        return instance;
    }

    // Initialisation de la base de données
    private void initDatabase() throws IOException, SQLException {
        // Chargement des propriétés de connexion
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Fichier config.properties introuvable !");
            }
            props.load(input);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        // Établir la connexion
        connection = DriverManager.getConnection(url, user, password);

        // Créer les tables si elles n'existent pas
        createTables();

        // Charger les données
        chargerLivres();
        chargerUtilisateurs();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Table des livres
            stmt.execute("CREATE TABLE IF NOT EXISTS livres (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "titre VARCHAR(255) NOT NULL, " +
                    "auteur VARCHAR(255) NOT NULL, " +
                    "genre VARCHAR(100) NOT NULL, " +
                    "disponible BOOLEAN DEFAULT TRUE)");

            // Table des utilisateurs
            stmt.execute("CREATE TABLE IF NOT EXISTS utilisateurs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(100) NOT NULL, " +
                    "prenom VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(255) UNIQUE NOT NULL, " +
                    "nb_emprunts INT DEFAULT 0)");

            // Table des emprunts (relation entre livres et utilisateurs)
            stmt.execute("CREATE TABLE IF NOT EXISTS emprunts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "livre_id INT NOT NULL, " +
                    "utilisateur_id INT NOT NULL, " +
                    "date_emprunt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "date_retour TIMESTAMP, " +
                    "statut VARCHAR(50) DEFAULT 'EN_COURS', " +
                    "FOREIGN KEY (livre_id) REFERENCES livres(id), " +
                    "FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id))");
        }
    }

    // Méthodes de gestion des livres
    public void ajouterLivre(Livre livre) throws SQLException {
        String sql = "INSERT INTO livres (titre, auteur, genre, disponible) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, livre.getTitre());
            pstmt.setString(2, livre.getAuteur());
            pstmt.setString(3, livre.getGenre());
            pstmt.setBoolean(4, livre.isDisponible());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La création du livre a échoué, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    livre.setId(generatedKeys.getInt(1));
                    this.livres.add(livre);
                } else {
                    throw new SQLException("La création du livre a échoué, aucun ID obtenu.");
                }
            }
        }
    }

    private void chargerLivres() throws SQLException {
        this.livres.clear();
        String sql = "SELECT id, titre, auteur, genre, disponible FROM livres";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Livre livre = new Livre(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("auteur"),
                        rs.getString("genre"),
                        rs.getBoolean("disponible")
                );
                this.livres.add(livre);
            }
        }
    }

    // Méthodes de gestion des utilisateurs
    public void inscrireUtilisateur(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, nb_emprunts) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getPrenom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setInt(4, utilisateur.getNbEmprunts());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("L'inscription de l'utilisateur a échoué, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    utilisateur.setId(generatedKeys.getInt(1));
                    this.utilisateurs.add(utilisateur);
                } else {
                    throw new SQLException("L'inscription de l'utilisateur a échoué, aucun ID obtenu.");
                }
            }
        }
    }

    private void chargerUtilisateurs() throws SQLException {
        this.utilisateurs.clear();
        String sql = "SELECT id, nom, prenom, email, nb_emprunts FROM utilisateurs";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getInt("nb_emprunts")
                );
                this.utilisateurs.add(utilisateur);
            }
        }
    }

    // Gestion des emprunts
    public void emprunterLivre(Utilisateur utilisateur, Livre livre) throws Exception {
        if (!livre.isDisponible()) {
            throw new Exception("Le livre n'est pas disponible.");
        }

        // Transaction pour assurer la cohérence
        connection.setAutoCommit(false);
        try {
            // Mettre à jour le livre
            String sqlLivre = "UPDATE livres SET disponible = false WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlLivre)) {
                pstmt.setInt(1, livre.getId());
                pstmt.executeUpdate();
            }

            // Mettre à jour l'utilisateur
            String sqlUser = "UPDATE utilisateurs SET nb_emprunts = nb_emprunts + 1 WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUser)) {
                pstmt.setInt(1, utilisateur.getId());
                pstmt.executeUpdate();
            }

            // Ajouter l'emprunt
            String sqlEmprunt = "INSERT INTO emprunts (livre_id, utilisateur_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlEmprunt)) {
                pstmt.setInt(1, livre.getId());
                pstmt.setInt(2, utilisateur.getId());
                pstmt.executeUpdate();
            }

            // Appliquer les changements en mémoire
            utilisateur.emprunterLivre(livre);

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void rendreLivre(Utilisateur utilisateur, Livre livre) throws Exception {
        if (livre.isDisponible()) {
            throw new Exception("Ce livre n'était pas emprunté.");
        }

        // Transaction pour assurer la cohérence
        connection.setAutoCommit(false);
        try {
            // Mettre à jour le livre
            String sqlLivre = "UPDATE livres SET disponible = true WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlLivre)) {
                pstmt.setInt(1, livre.getId());
                pstmt.executeUpdate();
            }

            // Mettre à jour l'utilisateur
            String sqlUser = "UPDATE utilisateurs SET nb_emprunts = nb_emprunts - 1 WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUser)) {
                pstmt.setInt(1, utilisateur.getId());
                pstmt.executeUpdate();
            }

            // Mettre à jour l'emprunt
            String sqlEmprunt = "UPDATE emprunts SET date_retour = CURRENT_TIMESTAMP, statut = 'TERMINE' " +
                    "WHERE livre_id = ? AND utilisateur_id = ? AND statut = 'EN_COURS'";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlEmprunt)) {
                pstmt.setInt(1, livre.getId());
                pstmt.setInt(2, utilisateur.getId());
                pstmt.executeUpdate();
            }

            // Appliquer les changements en mémoire
            utilisateur.rendreLivre(livre);

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // Export des livres en CSV
    public void exporterLivresCSV(String fichier) throws IOException {
        try (FileWriter writer = new FileWriter(fichier)) {
            // En-tête
            writer.append("ID,Titre,Auteur,Genre,Disponible\n");

            // Données
            for (Livre livre : livres) {
                writer.append(String.valueOf(livre.getId())).append(",");
                writer.append(livre.getTitre().replace(",", ";")).append(",");
                writer.append(livre.getAuteur().replace(",", ";")).append(",");
                writer.append(livre.getGenre()).append(",");
                writer.append(String.valueOf(livre.isDisponible())).append("\n");
            }
        }
    }

    // Getters pour les listes
    public List<Livre> getLivres() {
        return livres;
    }

    public List<Utilisateur> getUtilisateurs() {
        return utilisateurs;
    }

    // Méthode pour fermer proprement les ressources
    public void fermer() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
}