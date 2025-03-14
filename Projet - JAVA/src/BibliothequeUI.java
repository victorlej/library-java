import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class BibliothequeUI extends JFrame {
    private Bibliotheque bibliotheque;
    private JTable tableLivres;
    private DefaultTableModel modeleLivres;
    private JTable tableUtilisateurs;
    private DefaultTableModel modeleUtilisateurs;
    private JTable tableRetards;
    private DefaultTableModel modeleRetards;
    private Color primaryColor = new Color(63, 81, 181); // Bleu indigo

    public BibliothequeUI() {
        bibliotheque = Bibliotheque.getInstance();

        setTitle("Gestion de Bibliothèque");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                bibliotheque.fermer();
            }
        });

        JTabbedPane onglets = new JTabbedPane();
        onglets.setBackground(Color.WHITE);
        onglets.setForeground(primaryColor);

        JPanel panelLivres = creerPanelLivres();
        JPanel panelUtilisateurs = creerPanelUtilisateurs();
        JPanel panelEmprunts = creerPanelEmprunts();
        JPanel panelRetards = creerPanelRetards();

        ajouterMenuContextuelLivres();
        ajouterMenuContextuelUtilisateurs();

        onglets.addTab("Livres", panelLivres);
        onglets.addTab("Utilisateurs", panelUtilisateurs);
        onglets.addTab("Emprunts", panelEmprunts);
        onglets.addTab("Retards", panelRetards);

        getContentPane().add(onglets);
    }

    private JPanel creerPanelLivres() {
        JPanel panel = new JPanel(new BorderLayout());
        modeleLivres = new DefaultTableModel();
        modeleLivres.addColumn("ID");
        modeleLivres.addColumn("Titre");
        modeleLivres.addColumn("Auteur");
        modeleLivres.addColumn("Genre");
        modeleLivres.addColumn("Disponible");

        tableLivres = new JTable(modeleLivres);
        tableLivres.setRowHeight(25);
        // On supprime l'ancienne configuration directe des en-têtes :
        // tableLivres.getTableHeader().setBackground(primaryColor);
        // tableLivres.getTableHeader().setForeground(Color.WHITE);

        // Applique le style sur l'en-tête et les lignes
        styleTableHeader(tableLivres);
        styleTableRows(tableLivres);

        // Couleurs par défaut (au cas où la cellule ne serait pas rendue par le renderer)
        tableLivres.setForeground(Color.BLACK);
        tableLivres.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tableLivres);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelAjout = new JPanel(new GridLayout(4, 2, 5, 5));
        panelAjout.setBorder(BorderFactory.createTitledBorder("Ajouter un livre"));

        JTextField champTitre = new JTextField();
        champTitre.setForeground(Color.BLACK);
        champTitre.setBackground(Color.WHITE);

        JTextField champAuteur = new JTextField();
        champAuteur.setForeground(Color.BLACK);
        champAuteur.setBackground(Color.WHITE);

        String[] genres = {"Roman", "Science", "BD", "Poésie", "Histoire", "Autre"};
        JComboBox<String> comboGenre = new JComboBox<>(genres);
        comboGenre.setForeground(Color.BLACK);
        comboGenre.setBackground(Color.WHITE);

        JLabel labelTitre = new JLabel("Titre:");
        labelTitre.setForeground(Color.BLACK);
        JLabel labelAuteur = new JLabel("Auteur:");
        labelAuteur.setForeground(Color.BLACK);
        JLabel labelGenre = new JLabel("Genre:");
        labelGenre.setForeground(Color.BLACK);

        panelAjout.add(labelTitre);
        panelAjout.add(champTitre);
        panelAjout.add(labelAuteur);
        panelAjout.add(champAuteur);
        panelAjout.add(labelGenre);
        panelAjout.add(comboGenre);

        JButton btnAjouter = createStyledButton("Ajouter");
        JButton btnExporter = createStyledButton("Exporter en CSV");
        JButton btnSupprimer = createStyledButton("Supprimer");

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnExporter);
        panelBoutons.add(btnSupprimer);
        panelAjout.add(new JLabel(""));
        panelAjout.add(panelBoutons);

        JPanel panelBas = new JPanel(new BorderLayout());
        panelBas.add(panelAjout, BorderLayout.CENTER);
        panel.add(panelBas, BorderLayout.SOUTH);

        btnAjouter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titre = champTitre.getText().trim();
                String auteur = champAuteur.getText().trim();
                String genre = (String) comboGenre.getSelectedItem();
                if (titre.isEmpty() || auteur.isEmpty()) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Livre livre = LivreFactory.creerLivre(genre, titre, auteur);
                    bibliotheque.ajouterLivre(livre);
                    rafraichirTableLivres();
                    champTitre.setText("");
                    champAuteur.setText("");
                    comboGenre.setSelectedIndex(0);
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Livre ajouté avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'ajout du livre: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnExporter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    bibliotheque.exporterLivresCSV("livres.csv");
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Export réussi vers livres.csv", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'export: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnSupprimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tableLivres.getSelectedRow();
                if (row != -1) {
                    row = tableLivres.convertRowIndexToModel(row);
                    int livreId = (int) modeleLivres.getValueAt(row, 0);
                    if (JOptionPane.showConfirmDialog(BibliothequeUI.this, "Supprimer ce livre ?",
                            "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            for (Livre l : bibliotheque.getLivres()) {
                                if (l.getId() == livreId) {
                                    bibliotheque.supprimerLivre(l);
                                    rafraichirTableLivres();
                                    break;
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(BibliothequeUI.this, "Erreur: " + ex.getMessage());
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez sélectionner un livre à supprimer",
                            "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        rafraichirTableLivres();
        return panel;
    }

    private JPanel creerPanelUtilisateurs() {
        JPanel panel = new JPanel(new BorderLayout());
        modeleUtilisateurs = new DefaultTableModel();
        modeleUtilisateurs.addColumn("ID");
        modeleUtilisateurs.addColumn("Nom");
        modeleUtilisateurs.addColumn("Prénom");
        modeleUtilisateurs.addColumn("Email");
        modeleUtilisateurs.addColumn("Emprunts");

        tableUtilisateurs = new JTable(modeleUtilisateurs);
        tableUtilisateurs.setRowHeight(25);
        // On remplace l'ancienne config des en-têtes par nos méthodes
        // tableUtilisateurs.getTableHeader().setBackground(primaryColor);
        // tableUtilisateurs.getTableHeader().setForeground(Color.WHITE);
        styleTableHeader(tableUtilisateurs);
        styleTableRows(tableUtilisateurs);

        tableUtilisateurs.setForeground(Color.BLACK);
        tableUtilisateurs.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tableUtilisateurs);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelAjout = new JPanel(new GridLayout(4, 2, 5, 5));
        panelAjout.setBorder(BorderFactory.createTitledBorder("Inscrire un utilisateur"));

        JTextField champNom = new JTextField();
        champNom.setForeground(Color.BLACK);
        champNom.setBackground(Color.WHITE);

        JTextField champPrenom = new JTextField();
        champPrenom.setForeground(Color.BLACK);
        champPrenom.setBackground(Color.WHITE);

        JTextField champEmail = new JTextField();
        champEmail.setForeground(Color.BLACK);
        champEmail.setBackground(Color.WHITE);

        JLabel labelNom = new JLabel("Nom:");
        labelNom.setForeground(Color.BLACK);
        JLabel labelPrenom = new JLabel("Prénom:");
        labelPrenom.setForeground(Color.BLACK);
        JLabel labelEmail = new JLabel("Email:");
        labelEmail.setForeground(Color.BLACK);

        panelAjout.add(labelNom);
        panelAjout.add(champNom);
        panelAjout.add(labelPrenom);
        panelAjout.add(champPrenom);
        panelAjout.add(labelEmail);
        panelAjout.add(champEmail);

        JButton btnInscrire = createStyledButton("Inscrire");
        JPanel panelBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBouton.add(btnInscrire);
        panelAjout.add(new JLabel(""));
        panelAjout.add(panelBouton);

        JPanel panelBas = new JPanel(new BorderLayout());
        panelBas.add(panelAjout, BorderLayout.CENTER);
        panel.add(panelBas, BorderLayout.SOUTH);

        btnInscrire.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = champNom.getText().trim();
                String prenom = champPrenom.getText().trim();
                String email = champEmail.getText().trim();
                if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Utilisateur utilisateur = new Utilisateur(nom, prenom, email);
                    bibliotheque.inscrireUtilisateur(utilisateur);
                    rafraichirTableUtilisateurs();
                    champNom.setText("");
                    champPrenom.setText("");
                    champEmail.setText("");
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Utilisateur inscrit avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'inscription: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        rafraichirTableUtilisateurs();
        return panel;
    }

    private JPanel creerPanelEmprunts() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        JLabel lblUtilisateur = new JLabel("Utilisateur:");
        lblUtilisateur.setForeground(Color.BLACK);
        JComboBox<Utilisateur> comboUtilisateurs = new JComboBox<>();
        comboUtilisateurs.setForeground(Color.BLACK);
        comboUtilisateurs.setBackground(Color.WHITE);

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(lblUtilisateur, c);

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panelPrincipal.add(comboUtilisateurs, c);

        JLabel lblLivre = new JLabel("Livre:");
        lblLivre.setForeground(Color.BLACK);
        JComboBox<Livre> comboLivres = new JComboBox<>();
        comboLivres.setForeground(Color.BLACK);
        comboLivres.setBackground(Color.WHITE);

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

        JButton btnEmprunter = createStyledButton("Emprunter");
        JButton btnRendre = createStyledButton("Rendre");
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoutons.add(btnEmprunter);
        panelBoutons.add(btnRendre);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        panelPrincipal.add(panelBoutons, c);

        JTextArea infoZone = new JTextArea(10, 40);
        infoZone.setEditable(false);
        infoZone.setForeground(Color.BLACK);
        infoZone.setBackground(Color.WHITE);

        JScrollPane infoScroll = new JScrollPane(infoZone);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        panelPrincipal.add(infoScroll, c);

        panel.add(panelPrincipal, BorderLayout.CENTER);
        rafraichirCombos(comboUtilisateurs, comboLivres);

        btnEmprunter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utilisateur utilisateur = (Utilisateur) comboUtilisateurs.getSelectedItem();
                Livre livre = (Livre) comboLivres.getSelectedItem();
                if (utilisateur == null || livre == null) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Veuillez sélectionner un utilisateur et un livre", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    bibliotheque.emprunterLivre(utilisateur, livre);
                    rafraichirCombos(comboUtilisateurs, comboLivres);
                    rafraichirTableLivres();
                    rafraichirTableUtilisateurs();
                    infoZone.append("Livre \"" + livre.getTitre() + "\" emprunté par " +
                            utilisateur.getPrenom() + " " + utilisateur.getNom() + "\n");
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Emprunt effectué avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors de l'emprunt: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
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
                            "Veuillez sélectionner un utilisateur et un livre", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    bibliotheque.rendreLivre(utilisateur, livre);
                    rafraichirCombos(comboUtilisateurs, comboLivres);
                    rafraichirTableLivres();
                    rafraichirTableUtilisateurs();
                    infoZone.append("Livre \"" + livre.getTitre() + "\" rendu par " +
                            utilisateur.getPrenom() + " " + utilisateur.getNom() + "\n");
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Livre rendu avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BibliothequeUI.this,
                            "Erreur lors du retour: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private JPanel creerPanelRetards() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        modeleRetards = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeleRetards.addColumn("ID");
        modeleRetards.addColumn("Livre");
        modeleRetards.addColumn("Utilisateur");
        modeleRetards.addColumn("Date d'emprunt");
        modeleRetards.addColumn("Jours de retard");

        tableRetards = new JTable(modeleRetards);
        tableRetards.setRowHeight(25);
        // tableRetards.getTableHeader().setBackground(primaryColor);
        // tableRetards.getTableHeader().setForeground(Color.WHITE);
        styleTableHeader(tableRetards);
        styleTableRows(tableRetards);

        tableRetards.setForeground(Color.BLACK);
        tableRetards.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tableRetards);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnRafraichir = createStyledButton("Rafraîchir");
        btnRafraichir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rafraichirTableRetards();
            }
        });
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoutons.add(btnRafraichir);
        panel.add(panelBoutons, BorderLayout.SOUTH);

        rafraichirTableRetards();
        return panel;
    }

    // -------------------------------------------------------------------------
    // Méthodes pour styliser l'en-tête et les lignes des JTable
    // -------------------------------------------------------------------------
    private void styleTableHeader(JTable table) {
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel header = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, column);
                header.setBackground(primaryColor);
                header.setForeground(Color.WHITE);
                header.setHorizontalAlignment(JLabel.CENTER);
                header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(50, 50, 50)));
                header.setFont(header.getFont().deriveFont(Font.BOLD));
                header.setOpaque(true);
                return header;
            }
        });
    }

    private void styleTableRows(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    // Couleur de fond lorsqu'une ligne est sélectionnée
                    c.setBackground(new Color(220, 220, 255));
                    c.setForeground(Color.BLACK);
                } else {
                    // Couleur de fond en alternance (zébrage)
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }
    // -------------------------------------------------------------------------

    // Boutons stylisés
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(primaryColor.darker());
                } else if (getModel().isRollover()) {
                    g.setColor(primaryColor.brighter());
                } else {
                    g.setColor(primaryColor);
                }
                g.fillRect(0, 0, getWidth(), getHeight());

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };

        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // Ajout d'une bordure légère pour mieux définir les contours du bouton
        button.setBorder(BorderFactory.createLineBorder(primaryColor.darker(), 1));

        return button;
    }

    private void ajouterMenuContextuelLivres() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Supprimer");
        deleteItem.addActionListener(e -> {
            int row = tableLivres.getSelectedRow();
            if (row != -1) {
                row = tableLivres.convertRowIndexToModel(row);
                int livreId = (int) modeleLivres.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Supprimer ce livre ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        for (Livre l : bibliotheque.getLivres()) {
                            if (l.getId() == livreId) {
                                bibliotheque.supprimerLivre(l);
                                rafraichirTableLivres();
                                break;
                            }
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                    }
                }
            }
        });
        popupMenu.add(deleteItem);
        tableLivres.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tableLivres.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tableLivres.setRowSelectionInterval(row, row);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void ajouterMenuContextuelUtilisateurs() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Supprimer");
        deleteItem.addActionListener(e -> {
            int row = tableUtilisateurs.getSelectedRow();
            if (row != -1) {
                row = tableUtilisateurs.convertRowIndexToModel(row);
                int userId = (int) modeleUtilisateurs.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Supprimer cet utilisateur ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        for (Utilisateur u : bibliotheque.getUtilisateurs()) {
                            if (u.getId() == userId) {
                                bibliotheque.supprimerUtilisateur(u);
                                rafraichirTableUtilisateurs();
                                break;
                            }
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                    }
                }
            }
        });
        popupMenu.add(deleteItem);
        tableUtilisateurs.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tableUtilisateurs.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tableUtilisateurs.setRowSelectionInterval(row, row);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

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

    private void rafraichirTableRetards() {
        modeleRetards.setRowCount(0);
        try {
            List<Emprunt> retards = bibliotheque.getEmpruntsEnRetard();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Emprunt emprunt : retards) {
                Livre livre = emprunt.getLivre();
                Utilisateur utilisateur = emprunt.getUtilisateur();
                long diffTime = System.currentTimeMillis() - emprunt.getDateEmprunt().getTime();
                long diffDays = diffTime / (24 * 60 * 60 * 1000) - 14;
                modeleRetards.addRow(new Object[]{
                        emprunt.getId(),
                        livre.getTitre() + " (" + livre.getAuteur() + ")",
                        utilisateur.getPrenom() + " " + utilisateur.getNom(),
                        sdf.format(emprunt.getDateEmprunt()),
                        diffDays
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }

    private void rafraichirCombos(JComboBox<Utilisateur> comboUtilisateurs, JComboBox<Livre> comboLivres) {
        comboUtilisateurs.removeAllItems();
        for (Utilisateur utilisateur : bibliotheque.getUtilisateurs()) {
            comboUtilisateurs.addItem(utilisateur);
        }
        comboLivres.removeAllItems();
        for (Livre livre : bibliotheque.getLivres()) {
            comboLivres.addItem(livre);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Paramètres globaux pour les couleurs
            UIManager.put("Label.foreground", Color.BLACK);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextArea.foreground", Color.BLACK);
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", Color.BLACK);
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("Table.foreground", Color.BLACK);
            UIManager.put("Table.background", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            BibliothequeUI ui = new BibliothequeUI();
            ui.setVisible(true);
        });
    }
}
