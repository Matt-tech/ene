package ene.views;

import java.io.File;
import ene.interfaces.Model;
import ene.interfaces.Controller;
import ene.views.AbstractView;
import ene.controllers.LibraryController;
import ene.controllers.PlayerController;
import ene.interfaces.Localization;
import ene.models.LibraryModel;
import ene.models.TrackModel;
import java.util.Map;
import java.util.UUID;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
 * Content view class.
 */
public class ContentView extends AbstractView <JScrollPane, LibraryModel> implements Localization, ListSelectionListener {
    /**
     * Table instance.
     */
    protected JTable table;

    /**
     * Table content instance.
     */
    protected DefaultTableModel tableContent = new DefaultTableModel() {
        /**
         * Object serialization version number.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    /**
     * Library controller instance.
     */
    protected LibraryController libraryController;

    /**
     * Player controller instance.
     */
    protected PlayerController playerController;

    /**
     * Sets the table instance.
     * @param table Table instance.
     */
    protected void setTable(JTable table) {
        this.table = table;
    }

    /**
     * Returns the table instance.
     * @return Table instance.
     */
    protected JTable getTable() {
        return this.table;
    }

    /**
     * Sets the table content instance.
     * @param tableContent Table content instance.
     */
    protected void setTableContent(DefaultTableModel tableContent) {
        this.tableContent = tableContent;
    }

    /**
     * Returns the table content instance.
     * @return Table content instance.
     */
    protected DefaultTableModel getTableContent() {
        return this.tableContent;
    }

    /**
     * Sets the library controller instance.
     * @param libraryController Library controller instance.
     */
    protected void setLibraryController(Controller libraryController) {
        this.libraryController = (LibraryController)libraryController;
    }

    /**
     * Returns the library controller instance.
     * @return Library controller instance.
     */
    protected LibraryController getLibraryController() {
        return this.libraryController;
    }

    /**
     * Sets the player controller instance.
     * @param playerController Player controller instance.
     */
    protected void setPlayerController(Controller playerController) {
        this.playerController = (PlayerController)playerController;
    }

    /**
     * Returns the player controller instance.
     * @return Player controller instance.
     */
    protected PlayerController getPlayerController() {
        return this.playerController;
    }

    /**
     * Constructor.
     * @param model Library model instance.
     * @param libraryController Library controller instance.
     * @param playerController Player controller instance.
     */
    public ContentView(Model model, Controller libraryController, Controller playerController) {
        model.addView(this);
        this.setModel(model);
        this.setLibraryController(libraryController);
        this.setPlayerController(playerController);
        this.initialize();
        this.setLayoutPosition(BorderLayout.CENTER);
        this.getLibraryController().addDirectoryContent(new File("Music"));
    }

    /**
     * Initializing.
     */
    protected void initialize() {
        // Prepare table content.
        DefaultTableModel tableContent = this.getTableContent();
        tableContent.setColumnIdentifiers(new String[]{getString("TABLE_COLUMN_ARTIST"), getString("TABLE_COLUMN_TITLE"), getString("TABLE_COLUMN_GENRE"), "UUID"});
        // Prepare table.
        JTable table = new JTable(tableContent);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        this.hideColumn(table, 3); // Hides UUID.
        this.setTable(table);
        this.setCoreComponent(new JScrollPane(table));
    }

    /**
     * Hide table column.
     * @param table Table instance.
     * @param columnIndex Column index.
     */
    protected void hideColumn(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setPreferredWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
    }

    @Override
    public void update() {
        DefaultTableModel tableContent = this.getTableContent();
        Map<UUID, TrackModel> tracks = this.getModel().getAll();
        tableContent.setRowCount(0);
        for (Map.Entry<UUID, TrackModel> entry : tracks.entrySet()) {
            TrackModel track = entry.getValue();
            tableContent.addRow(new String[]{track.getArtist(), track.getTitle(), track.getGenre(), track.getUUID().toString()});
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            debugInfoAbout(event);
            UUID uuid = UUID.fromString((String)tableContent.getValueAt(this.getTable().getSelectedRow(), 3));
            this.getPlayerController().load(this.getModel().getByUUID(uuid));
        }
    }
}
