package me.aurous.ui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import me.aurous.apis.impl.vkapi.VKAuth;
import me.aurous.player.Settings;
import me.aurous.searchengines.impl.VKEngine;
import me.aurous.searchengines.impl.YouTubeEngine;
import me.aurous.ui.UISession;
import me.aurous.ui.models.ForcedListSelectionModel;
import me.aurous.utils.Constants;
import me.aurous.utils.ModelUtils;
import me.aurous.utils.Utils;
import me.aurous.utils.media.MediaUtils;
import me.aurous.utils.playlist.PlayListUtils;

import com.alee.extended.image.WebImage;
import com.alee.laf.text.WebTextField;

public class SearchWidget implements ActionListener {

	public static JTable getSearchTable() {
		return searchTable;
	}

	public static DefaultTableModel getTableModel() {
		return tableModel;
	}

	/**
	 * Launch the application.
	 */
	public static void openSearch() {
		final String[] args = {};
		if ((UISession.getSearchWidget() != null)
				&& UISession.getSearchWidget().isOpen()) {
			UISession.getSearchWidget().getWidget().toFront();
			UISession.getSearchWidget().getWidget().repaint();
			return;
		}
		final File f = new File(Constants.DATA_PATH + "settings/vkauth.dat");
		if (!f.exists() && !f.isDirectory()) {
			final int dialogButton = JOptionPane.YES_NO_OPTION;
			final int dialogResult = JOptionPane
					.showConfirmDialog(
							null,
							"<html>Aurous search feature is powered by <strong>vk.com</strong><br> to use this feature you must connect your account<br> if you do not have one you will be prompted to register. Continue?</html>",
							"No Key Detected", dialogButton);

			if (dialogResult == JOptionPane.YES_OPTION) {

				VKAuth.main(args);

			} else {
				return;
			}
		}
		EventQueue.invokeLater(() -> {
			try {
				final SearchWidget window = new SearchWidget();
				UISession.setSearchWidget(window);
				UISession.getSearchWidget().getWidget().setVisible(true);

			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
	}

	private JFrame searchWidget;
	private boolean isValidAuth;

	private JComboBox<String> comboBox;

	private WebTextField searchBar;

	private static JTable searchTable;

	private static DefaultTableModel tableModel;

	protected JScrollPane scroller;
	JPopupMenu popup;
	private final String[] options = { "by title", "by artist" };

	/**
	 * Create the application.
	 */
	public SearchWidget() {
		initialize();

	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Component c = (Component) e.getSource();
		final JPopupMenu popup = (JPopupMenu) c.getParent();
		final JTable table = (JTable) popup.getInvoker();

		switch (e.getActionCommand()) {
		case "Play":
			MediaUtils.switchMedia(table);
			break;
		case "Add":
			if ((Utils.isNull(Settings.getLastPlayList()))
					|| Settings.getLastPlayList().isEmpty()) {
				JOptionPane.showMessageDialog(new JFrame(),
						"You do not have any playlist loaded!", "Uh oh",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			final int row = table.getSelectedRow();

			if (table.getValueAt(row, 3).toString().contains("youtube")) {

				PlayListUtils.addUrlToPlayList(table.getValueAt(row, 3)
						.toString());
			} else {
				final String date = Utils.getDate();
				final String playListAddition = String.format(
						"%s, %s, %s, %s, %s, %s, %s, %s",
						table.getValueAt(row, 0), table.getValueAt(row, 1),
						table.getValueAt(row, 2), date, Settings.getUserName(),
						"", "https://aurous.me/bad.png",
						table.getValueAt(row, 4));

				PlayListUtils.addUrlToPlayList(playListAddition);
			}
		case "Copy URL":
			MediaUtils.copyMediaURL(table);
			break;
		}

	}

	public JComboBox<String> getComboBox() {
		return this.comboBox;
	}

	public WebTextField getSearchBar() {
		return this.searchBar;
	}

	public JFrame getWidget() {
		return this.searchWidget;
	}

	/**
	 * Initialize the contents of the searchWidget.
	 *
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		this.searchWidget = new JFrame();
		this.searchWidget.setResizable(false);
		this.searchWidget.setType(Type.UTILITY);

		this.searchWidget.getContentPane().setBackground(new Color(35, 35, 35));
		this.searchWidget
				.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.searchWidget.setTitle(String.format("Search - Powered by %s",
				Settings.getSearchEngine()));
		this.searchWidget.setSize(451, 382);
		this.searchWidget.getContentPane().setLayout(null);

		this.searchBar = new WebTextField(0);
		this.searchBar.setInputPrompt("Lana Del Ray...");

		this.searchBar.setBackground(new Color(35, 35, 35));
		this.searchBar.setForeground(Color.GRAY);
		this.searchBar.setFont(new Font("Calibri", Font.PLAIN, 16));
		this.searchBar.setLocation(0, 0);
		this.searchBar.setMargin(0, 0, 0, 2);
		final WebImage webImage = new WebImage(Utils.loadIcon("search.png"));
		this.searchBar.setTrailingComponent(webImage);
		this.searchBar.setSize(302, 25);
		this.searchWidget.getContentPane().add(this.searchBar);

		this.comboBox = new JComboBox<String>();

		this.comboBox.getEditor().getEditorComponent()
				.setBackground(Color.YELLOW);
		((JTextField) this.comboBox.getEditor().getEditorComponent())
		.setBackground(Color.YELLOW);
		this.comboBox.setBackground(Color.YELLOW);
		this.comboBox.setBounds(303, 1, 130, 25);
		for (final String option : this.options) {
			this.comboBox.addItem(option);
		}
		this.searchWidget.getContentPane().add(this.comboBox);

		searchTable = new JTable();
		searchTable.setName("search");
		searchTable.setBackground(new Color(35, 35, 35));
		searchTable.setForeground(Color.GRAY);
		searchTable.setOpaque(true);

		final JTableHeader header = searchTable.getTableHeader();
		header.setOpaque(false);
		header.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		header.setForeground(Color.GRAY);
		header.setAutoscrolls(true);

		header.setFont(new Font("Calibri", Font.PLAIN, 14));

		header.setBorder(BorderFactory.createEmptyBorder());

		searchTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				final int c = e.getKeyCode();
				e.getSource();
				if (c == KeyEvent.VK_DELETE) {

				} else if (c == KeyEvent.VK_ADD) {

				} else if (c == KeyEvent.VK_LEFT) {

				} else if (c == KeyEvent.VK_RIGHT) {

				} else if (c == KeyEvent.VK_ENTER) {

				}
			}
		});
		searchTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					final JTable target = (JTable) e.getSource();

					target.getSelectedRow();
					MediaUtils.switchMedia(target);
				}

			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					final JTable source = (JTable) e.getSource();
					final int row = source.rowAtPoint(e.getPoint());
					final int column = source.columnAtPoint(e.getPoint());

					if (!source.isRowSelected(row)) {
						source.changeSelection(row, column, false, false);
					}

					SearchWidget.this.popup.show(e.getComponent(), e.getX(),
							e.getY());
				}
			}

		});

		searchTable.setSelectionModel(new ForcedListSelectionModel());

		this.scroller = new javax.swing.JScrollPane(searchTable);
		this.scroller.setSize(445, 320);
		this.scroller.setLocation(0, 33);
		ModelUtils.loadSearchResults(Constants.DATA_PATH
				+ "settings/search.blank");

		final TableColumn hiddenLink = searchTable.getColumnModel()
				.getColumn(3);
		hiddenLink.setMinWidth(2);
		hiddenLink.setPreferredWidth(2);
		hiddenLink.setMaxWidth(2);
		hiddenLink.setCellRenderer(new ModelUtils.InteractiveRenderer(3));
		this.popup = new JPopupMenu();

		final JMenuItem addItem = new JMenuItem("Add");
		addItem.addActionListener(this);
		this.popup.add(addItem);
		final JMenuItem copyItem = new JMenuItem("Copy URL");
		copyItem.addActionListener(this);
		this.popup.add(copyItem);
		final JMenuItem playItem = new JMenuItem("Play");
		playItem.addActionListener(this);
		this.popup.add(playItem);
		searchTable.setAutoCreateRowSorter(true);
		this.scroller.setBorder(BorderFactory.createEmptyBorder());
		this.searchWidget.getContentPane().add(this.scroller);
		searchTable.setFillsViewportHeight(true);
		searchTable.setSelectionBackground(searchTable.getBackground());
		searchTable.setSelectionForeground(new Color(213, 163, 0));
		searchTable.setGridColor(new Color(44, 44, 44));
		searchTable.setShowVerticalLines(false);
		searchTable.setBorder(new EtchedBorder());
		searchTable.setFont(new Font("Calibri", Font.PLAIN, 14));
		this.searchWidget.setLocationRelativeTo(UISession.getPresenter()
				.getAurousFrame());
		setSearchEngine();

	}

	private void setSearchEngine() {
		if (Settings.getSearchEngine().equals("VK")) {
			final VKEngine vkEngine = new VKEngine(100);
			this.searchBar.addActionListener(e -> vkEngine.search());
		} else if (Settings.getSearchEngine().equals("YouTube")) {
			final YouTubeEngine searchEngine = new YouTubeEngine();
			this.searchBar.addActionListener(e -> searchEngine.search());
		}
	}

	public boolean isOpen() {
		return this.searchWidget.isVisible();
	}

	public boolean isValidAuth() {
		return this.isValidAuth;
	}

	public void setComboBox(final JComboBox<String> comboBox) {
		this.comboBox = comboBox;
	}

	public void setValidAuth(final boolean isValidAuth) {
		this.isValidAuth = isValidAuth;
	}

}
