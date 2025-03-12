import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;

public class BibliothequeUI extends JFrame {
    private Bibliotheque bibliotheque;
    private JTable tableLivres;
    private DefaultTableModel modeleLivres;
    private JTable tableUtilisateurs;
    private DefaultTableModel modeleUtilisateurs;

    public BibliothequeUI() {
        // Initialisation
        bibliotheque = Bibliotheque.getInstance();

        // Configuration de la fenêtre
        setTitle("Gestion de Bibliothèque");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ajout d'un listener de fermeture pour libérer les ressources
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                bibliotheque.fermer();
            }
        });

        // Création des composants
        JTabbedPane onglets = new JTabbedPane();

        // Onglet Livres
        JPanel panelLivres = creerPanelLivres();
        onglets.addTab("Livres", panelLivres);

        // Onglet Utilisateurs
        JPanel panelUtilisateurs = creerPanelUtilisateurs();
        onglets.addTab("Utilisateurs", panelUtilisateurs);

        // Onglet Emprunts
        JPanel panelEmprunts = creerPanelEmprunts();
        onglets.addTab("Emprunts", panelEmprunts);

        // Ajout des onglets à la fenêtre
        getContentPane().add(onglets);
    }

    private JPanel creerPanelLivres() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tableau pour afficher les livres
        modeleLivres = new DefaultTableModel();
        modeleLivres.addColumn("ID");
        modeleLivres.addColumn("Titre");
        modeleLivres.addColumn("Auteur");
        modeleLivres.addColumn("Genre");
        modeleLivres.addColumn("Disponible");

        tableLivres = new JTable(modeleLivres);
        JScrollPane scrollPane = new JScrollPane(tableLivres);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panneau pour ajouter un livre
        JPanel panelAjout = new JPanel(new GridLayout(4, 2, 5, 5));
        panelAjout.setBorder(BorderFactory.createTitledBorder("Ajouter un livre"));

        JTextField champTitre = new JTextField();
        JTextField champAuteur = new JTextField();
        String[] genres = {"Roman", "Science", "BD", "Poésie", "Histoire", "Autre"};
        JComboBox<String> comboGenre = new JComboBox<>(genres);

        panelAjout.add(new JLabel("Titre:"));
        panelAjout.add(champTitre);
        panelAjout.add(new JLabel("Auteur:"));
        panelAjout.add(champAuteur);
        panelAjout.add(new JLabel("Genre:"));
        panelAjout.add(comboGenre);

        JButton btnAjouter = new JButton("Ajouter");
        JButton btnExporter = new JButton("Exporter en CSV");

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnExporter);

        panelAjout.add(new JLabel(""));
        panelAjout.add(panelBoutons);

        // Panel du bas pour les contrôles
        JPanel panelBas = new JPanel(new BorderLayout());
        panelBas.add(panelAjout, BorderLayout.CENTER);
        panel.add(panelBas, BorderLayout.SOUTH);

        // Actions des boutons
        btnAjouter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titre = champTitre.getText().trim();
                String auteur = champAuteur.getText().trim();
                String genre = (String) comboGenre.getSelectedItem();

                if (titre.isEmpty() || auteur.isEmpty()) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez remplir tous les champs",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    // Utilisation du pattern Factory
                    Livre livre = LivreFactory.creerLivre(genre, titre, auteur);
                    bibliotheque.ajouterLivre(livre);
                    rafraichirTableLivres();

                    // Réinitialiser les champs
                    champTitre.setText("");
                    champAuteur.setText("");
                    comboGenre.setSelectedIndex(0);

                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Livre ajouté avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'ajout du livre: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnExporter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    bibliotheque.exporterLivresCSV("livres.csv");
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Export réussi vers livres.csv",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'export: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Initialiser le tableau
        rafraichirTableLivres();

        return panel;
    }

    private JPanel creerPanelUtilisateurs() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tableau pour afficher les utilisateurs
        modeleUtilisateurs = new DefaultTableModel();
        modeleUtilisateurs.addColumn("ID");
        modeleUtilisateurs.addColumn("Nom");
        modeleUtilisateurs.addColumn("Prénom");
        modeleUtilisateurs.addColumn("Email");
        modeleUtilisateurs.addColumn("Emprunts");

        tableUtilisateurs = new JTable(modeleUtilisateurs);
        JScrollPane scrollPane = new JScrollPane(tableUtilisateurs);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panneau pour inscrire un utilisateur
        JPanel panelAjout = new JPanel(new GridLayout(4, 2, 5, 5));
        panelAjout.setBorder(BorderFactory.createTitledBorder("Inscrire un utilisateur"));

        JTextField champNom = new JTextField();
        JTextField champPrenom = new JTextField();
        JTextField champEmail = new JTextField();

        panelAjout.add(new JLabel("Nom:"));
        panelAjout.add(champNom);
        panelAjout.add(new JLabel("Prénom:"));
        panelAjout.add(champPrenom);
        panelAjout.add(new JLabel("Email:"));
        panelAjout.add(champEmail);

        JButton btnInscrire = new JButton("Inscrire");

        JPanel panelBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBouton.add(btnInscrire);

        panelAjout.add(new JLabel(""));
        panelAjout.add(panelBouton);

        // Panel du bas pour les contrôles
        JPanel panelBas = new JPanel(new BorderLayout());
        panelBas.add(panelAjout, BorderLayout.CENTER);
        panel.add(panelBas, BorderLayout.SOUTH);

        // Action du bouton
        btnInscrire.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = champNom.getText().trim();
                String prenom = champPrenom.getText().trim();
                String email = champEmail.getText().trim();

                if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez remplir tous les champs",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Utilisateur utilisateur = new Utilisateur(nom, prenom, email);
                    bibliotheque.inscrireUtilisateur(utilisateur);
                    rafraichirTableUtilisateurs();

                    // Réinitialiser les champs
                    champNom.setText("");
                    champPrenom.setText("");
                    champEmail.setText("");

                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Utilisateur inscrit avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'inscription: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Initialiser le tableau
        rafraichirTableUtilisateurs();

        return panel;
    }

    private JPanel creerPanelEmprunts() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel principal avec GridBagLayout pour plus de flexibilité
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        // Combo pour sélectionner un utilisateur
        JLabel lblUtilisateur = new JLabel("Utilisateur:");
        JComboBox<Utilisateur> comboUtilisateurs = new JComboBox<>();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(lblUtilisateur, c);

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelPrincipal.add(comboUtilisateurs, c);

        // Combo pour sélectionner un livre
        JLabel lblLivre = new JLabel("Livre:");
        JComboBox<Livre> comboLivres = new JComboBox<>();

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        panelPrincipal.add(lblLivre, c);

        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelPrincipal.add(comboLivres, c);

        // Boutons pour emprunter et rendre
        JButton btnEmprunter = new JButton("Emprunter");
        JButton btnRendre = new JButton("Rendre");

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoutons.add(btnEmprunter);
        panelBoutons.add(btnRendre);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        panelPrincipal.add(panelBoutons, c);

        // Affichage des infos d'emprunt (placeholder)
        JTextArea infoZone = new JTextArea(10, 40);
        infoZone.setEditable(false);
        JScrollPane infoScroll = new JScrollPane(infoZone);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        panelPrincipal.add(infoScroll, c);

        panel.add(panelPrincipal, BorderLayout.CENTER);

        // Remplir les combos
        rafraichirCombos(comboUtilisateurs, comboLivres);

        // Actions des boutons
        btnEmprunter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utilisateur utilisateur = (Utilisateur) comboUtilisateurs.getSelectedItem();
                Livre livre = (Livre) comboLivres.getSelectedItem();

                if (utilisateur == null || livre == null) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez sélectionner un utilisateur et un livre",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    bibliotheque.emprunterLivre(utilisateur, livre);
                    rafraichirCombos(comboUtilisateurs, comboLivres);
                    rafraichirTableLivres();
                    rafraichirTableUtilisateurs();

                    // Mettre à jour les infos
                    infoZone.append("Livre \"" + livre.getTitre() + "\" emprunté par " +
                            utilisateur.getPrenom() + " " + utilisateur.getNom() + "\n");

                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Emprunt effectué avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'emprunt: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRendre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utilisateur utilisateur = (Utilisateur) comboUtilisateurs.getSelectedItem();
                Livre livre = (Livre) comboLivres.getSelectedItem();

                if (utilisateur == null || livre == null) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez sélectionner un utilisateur et un livre",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    bibliotheque.rendreLivre(utilisateur, livre);
                    rafraichirCombos(comboUtilisateurs, comboLivres);
                    rafraichirTableLivres();
                    rafraichirTableUtilisateurs();

                    // Mettre à jour les infos
                    infoZone.append("Livre \"" + livre.getTitre() + "\" rendu par " +
                            utilisateur.getPrenom() + " " + utilisateur.getNom() + "\n");

                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Livre rendu avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors du retour: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    // Méthodes utilitaires pour rafraîchir les données
    private void rafraichirTableLivres() {
        modeleLivres.setRowCount(0);
        List<Livre> livres = bibliotheque.getLivres();

        for (Livre livre : livres) {
            modeleLivres.addRow(new Object[]{
                    livre.getId(),
                    livre.getTitre(),
                    livre.getAuteur(),
                    livre.getGenre(),
                    livre.isDisponible() ? "Oui" : "Non"
            });
        }
    }

    private void rafraichirTableUtilisateurs() {
        modeleUtilisateurs.setRowCount(0);
        List<Utilisateur> utilisateurs = bibliotheque.getUtilisateurs();

        for (Utilisateur utilisateur : utilisateurs) {
            modeleUtilisateurs.addRow(new Object[]{
                    utilisateur.getId(),
                    utilisateur.getNom(),
                    utilisateur.getPrenom(),
                    utilisateur.getEmail(),
                    utilisateur.getNbEmprunts()
            });
        }
    }

    private void rafraichirCombos(JComboBox<Utilisateur> comboUtilisateurs, JComboBox<Livre> comboLivres) {
        // Rafraîchir la liste des utilisateurs
        comboUtilisateurs.removeAllItems();
        for (Utilisateur utilisateur : bibliotheque.getUtilisateurs()) {
            comboUtilisateurs.addItem(utilisateur);
        }

        // Rafraîchir la liste des livres
        comboLivres.removeAllItems();
        for (Livre livre : bibliotheque.getLivres()) {
            comboLivres.addItem(livre);
        }
    }

    // Méthode principale pour lancer l'application
    public static void main(String[] args) {
        // Utiliser le look and feel du système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lancer l'interface
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BibliothequeUI ui = new BibliothequeUI();
                ui.setVisible(true);
            }
        });
    }
}