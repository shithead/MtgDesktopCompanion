package org.magic.gui;

import static org.magic.tools.MTG.getEnabledPlugin;
import static org.magic.tools.MTG.getPlugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXTable;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardAlert;
import org.magic.api.beans.MagicDeck;
import org.magic.api.beans.MagicPrice;
import org.magic.api.interfaces.MTGCardsExport;
import org.magic.api.interfaces.MTGCardsExport.MODS;
import org.magic.api.interfaces.MTGDao;
import org.magic.api.interfaces.MTGPricesProvider;
import org.magic.api.interfaces.MTGServer;
import org.magic.gui.abstracts.AbstractBuzyIndicatorComponent;
import org.magic.gui.abstracts.MTGUIComponent;
import org.magic.gui.components.MagicCardDetailPanel;
import org.magic.gui.components.PricesTablePanel;
import org.magic.gui.components.ServerStatePanel;
import org.magic.gui.components.charts.HistoryPricesPanel;
import org.magic.gui.components.dialog.CardSearchImportDialog;
import org.magic.gui.components.renderer.MagicPricePanel;
import org.magic.gui.editor.MagicEditionsComboBoxCellEditor;
import org.magic.gui.models.CardAlertTableModel;
import org.magic.gui.renderer.AlertedCardsRenderer;
import org.magic.gui.renderer.CardShakeRenderer;
import org.magic.gui.renderer.MagicEditionsComboBoxCellRenderer;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.threads.ThreadManager;
import org.magic.services.workers.AbstractObservableWorker;
import org.magic.sorters.MagicPricesComparator;
import org.magic.tools.IDGenerator;
import org.magic.tools.UITools;

public class AlarmGUI extends MTGUIComponent {
	
	private static final long serialVersionUID = 1L;
	private JXTable table;
	private CardAlertTableModel model;
	private MagicCardDetailPanel magicCardDetailPanel;
	private DefaultListModel<MagicPrice> resultListModel;
	private JList<MagicPrice> list;
	private JButton btnRefresh;
	private JButton btnDelete;
	private HistoryPricesPanel variationPanel;
	private JButton btnImport;
	private AbstractBuzyIndicatorComponent lblLoading;
	private File f;
	private PricesTablePanel pricesTablePanel;
	private JButton btnSuggestPrice;
	private JSplitPane splitPanel;
	
	
	public AlarmGUI() {
		initGUI();
		initActions();
	}
	

	@Override
	public ImageIcon getIcon() {
		return MTGConstants.ICON_ALERT;
	}
	
	@Override
	public String getTitle() {
		return MTGControler.getInstance().getLangService().getCapitalize("ALERT_MODULE");
	}
	
	

	public void initGUI() {
		splitPanel = new JSplitPane();

		table = new JXTable();
		model = new CardAlertTableModel();
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		magicCardDetailPanel = new MagicCardDetailPanel();
		variationPanel = new HistoryPricesPanel(true);
		JPanel panelRight = new JPanel();
		resultListModel = new DefaultListModel<>();
		list = new JList<>(resultListModel);
		JPanel panel = new JPanel();
		btnRefresh = UITools.createBindableJButton(null, MTGConstants.ICON_REFRESH, KeyEvent.VK_R, "refresh Alarm");
		btnImport = UITools.createBindableJButton(null, MTGConstants.ICON_IMPORT, KeyEvent.VK_I, "import Alarm");
		btnDelete = UITools.createBindableJButton(null, MTGConstants.ICON_DELETE, KeyEvent.VK_D, "delete Alarm");
		btnSuggestPrice = UITools.createBindableJButton(null, MTGConstants.ICON_EURO, KeyEvent.VK_S, "suggestPrices Alarm");
		lblLoading = AbstractBuzyIndicatorComponent.createProgressComponent();
		JPanel serversPanel = new JPanel();
		ServerStatePanel oversightPanel = new ServerStatePanel(false,getPlugin("Alert Trend Server", MTGServer.class));
		ServerStatePanel serverPricePanel = new ServerStatePanel(false,getPlugin("Alert Price Checker", MTGServer.class));
		UITools.initTableFilter(table);

		
///////CONFIG		
		setLayout(new BorderLayout());
		splitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		table.setModel(model);
		table.getColumnModel().getColumn(4).setCellRenderer(new AlertedCardsRenderer());
		magicCardDetailPanel.enableThumbnail(true);
		list.setCellRenderer((JList<? extends MagicPrice> obj, MagicPrice value, int index, boolean isSelected,boolean cellHasFocus) -> new MagicPricePanel(value));
		table.getColumnModel().getColumn(5).setCellRenderer(new CardShakeRenderer());
		table.getColumnModel().getColumn(6).setCellRenderer(new CardShakeRenderer());
		table.getColumnModel().getColumn(7).setCellRenderer(new CardShakeRenderer());
		table.getColumnModel().getColumn(1).setCellRenderer(new MagicEditionsComboBoxCellRenderer(false));
		table.getColumnModel().getColumn(1).setCellEditor(new MagicEditionsComboBoxCellEditor());
		table.setRowHeight(MTGConstants.TABLE_ROW_HEIGHT);

		btnSuggestPrice.setToolTipText(MTGControler.getInstance().getLangService().getCapitalize("SUGGEST_PRICE"));
		
		panelRight.setLayout(new BorderLayout());
	
///////ADDS	
		splitPanel.setLeftComponent(new JScrollPane(table));
		add(splitPanel, BorderLayout.CENTER);
		splitPanel.setRightComponent(tabbedPane);
		
		serversPanel.setLayout(new GridLayout(2, 1, 0, 0));
		serversPanel.add(oversightPanel);
		serversPanel.add(serverPricePanel);
		panelRight.add(serversPanel,BorderLayout.SOUTH);
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("DETAILS"), MTGConstants.ICON_TAB_DETAILS, magicCardDetailPanel, null);
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("PRICE_VARIATIONS"), MTGConstants.ICON_TAB_VARIATIONS, variationPanel, null);
		pricesTablePanel = new PricesTablePanel();
		tabbedPane.addTab(MTGControler.getInstance().getLangService().getCapitalize("PRICES"), MTGConstants.ICON_TAB_PRICES, pricesTablePanel, null);
		add(panelRight, BorderLayout.EAST);
		panelRight.add(new JScrollPane(list),BorderLayout.CENTER);
		add(panel, BorderLayout.NORTH);
		panel.add(btnDelete);
		panel.add(btnImport);
		panel.add(btnRefresh);
		panel.add(btnSuggestPrice);
		panel.add(lblLoading);
		
	}


	@Override
	public void onFirstShowing() {
		splitPanel.setDividerLocation(.5);
		model.fireTableDataChanged();
		
	}


	private void initActions() {
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				resultListModel.removeAllElements();
				
				int viewRow = table.getSelectedRow();
				if (viewRow > -1) {
					int modelRow = table.convertRowIndexToModel(viewRow);
					MagicCardAlert selected = (MagicCardAlert) table.getModel().getValueAt(modelRow, 0);
					updateInfo(selected);
					table.setRowSelectionInterval(viewRow, viewRow);
					for (MagicPrice mp : selected.getOffers())
						resultListModel.addElement(mp);
				}
			}
		});
		
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {

					if (e.getClickCount() == 2 && (list.getSelectedValue() != null)) {
						Desktop.getDesktop().browse(new URI(list.getSelectedValue().getUrl()));
					}
				} catch (Exception e1) {
					MTGControler.getInstance().notify(e1);
				}

			}
		});
		
		btnRefresh.addActionListener(e -> model.fireTableDataChanged());
		
		
		btnSuggestPrice.addActionListener(ae->{
			
			if(table.getSelectedRows().length<=0)
				return;
			
			
			lblLoading.start(table.getSelectedRows().length);
			SwingWorker<Void, MagicCardAlert> sw = new SwingWorker<>()
					{
						@Override
						protected void done() {
							lblLoading.end();
							model.fireTableDataChanged();
						}

						@Override
						protected void process(List<MagicCardAlert> chunks) {
							lblLoading.progressSmooth(chunks.size());
						}

						@Override
						protected Void doInBackground(){
							List<MagicCardAlert> alerts = extract(table.getSelectedRows());
							for (MagicCardAlert alert : alerts)
							{	
								List<MagicPrice> prices=new ArrayList<>();
								MTGControler.getInstance().listEnabled(MTGPricesProvider.class).forEach(p->{
									try {
										prices.addAll(p.getPrice(alert.getCard().getCurrentSet(), alert.getCard()));
									} catch (IOException e1) {
										logger.error("error adding price for" + alert.getCard() + " with " + p,e1);
									}
									
								});
								
								Collections.sort(prices,new MagicPricesComparator());
								if(!prices.isEmpty())
								{
									alert.setPrice(prices.get(0).getValue());
									try {
										getEnabledPlugin(MTGDao.class).updateAlert(alert);
									} catch (SQLException e) {
										logger.error("error updating " + alert,e);
									}
								}
								publish(alert);
							}
							return null;
						}
				
					};
			
					ThreadManager.getInstance().runInEdt(sw,"suggest prices");
		});
		
		
		btnDelete.addActionListener(event -> {
			int res = JOptionPane.showConfirmDialog(null,MTGControler.getInstance().getLangService().getCapitalize("CONFIRM_DELETE",table.getSelectedRows().length + " item(s)"),
					MTGControler.getInstance().getLangService().getCapitalize("DELETE") + " ?",JOptionPane.YES_NO_OPTION);
			
			if (res == JOptionPane.YES_OPTION) 
			{
				int[] selected = table.getSelectedRows();
				lblLoading.start(selected.length);
				
				
				SwingWorker<List<MagicCardAlert>, MagicCardAlert> sw = new SwingWorker<>()
				{

					@Override
					protected List<MagicCardAlert> doInBackground() throws Exception {
						List<MagicCardAlert> alerts = extract(selected);
						for (MagicCardAlert alert : alerts)
						{
							getEnabledPlugin(MTGDao.class).deleteAlert(alert);
							publish(alert);
						}
						return alerts;
					}

					@Override
					protected void done() {
						try {
							get();
						}
						catch(Exception e)
						{
							MTGControler.getInstance().notify(e);
						}
						
						model.fireTableDataChanged();
						lblLoading.end();
					}

					@Override
					protected void process(List<MagicCardAlert> chunks) {
						lblLoading.progress(chunks.size());
					}
				};
				
				ThreadManager.getInstance().runInEdt(sw, "delete alerts");

			}
		});
		
		
		btnImport.addActionListener(ae -> {
			JPopupMenu menu = new JPopupMenu();

			JMenuItem mnuImportSearch = new JMenuItem(MTGControler.getInstance().getLangService().getCapitalize("IMPORT_FROM", MTGControler.getInstance().getLangService().get("SEARCH_MODULE")));
			mnuImportSearch.setIcon(MTGConstants.ICON_SEARCH);

			mnuImportSearch.addActionListener(importAE -> {
				CardSearchImportDialog cdSearch = new CardSearchImportDialog();
				cdSearch.setVisible(true);
				if (cdSearch.getSelection() != null) {
					for (MagicCard mc : cdSearch.getSelection())
						addCard(mc);
				}
			});
			menu.add(mnuImportSearch);

			for (final MTGCardsExport exp : MTGControler.getInstance().listEnabled(MTGCardsExport.class)) {
				if (exp.getMods() == MODS.BOTH || exp.getMods() == MODS.IMPORT) {

					JMenuItem it = new JMenuItem();
					it.setIcon(exp.getIcon());
					it.setText(exp.getName());
					it.addActionListener(itEvent -> {
						JFileChooser jf = new JFileChooser(".");
						jf.setFileFilter(new FileFilter() {
							@Override
							public String getDescription() {
								return exp.getName();
							}

							@Override
							public boolean accept(File f) {
								return (f.isDirectory() || f.getName().endsWith(exp.getFileExtension()));
							}
						});
						int res = -1;
						f = new File("");

						if (!exp.needDialogForDeck(MODS.IMPORT)) {
							res = jf.showOpenDialog(null);
							f = jf.getSelectedFile();
						} else {
							
							try {
								exp.importDeckFromFile(null).getMain().keySet().forEach(this::addCard);
							} catch (IOException e1) {
								logger.error(e1);
							}

						}

						if (res == JFileChooser.APPROVE_OPTION)
						{	
							
							AbstractObservableWorker<MagicDeck, MagicCard, MTGCardsExport> sw = new AbstractObservableWorker<>(lblLoading,exp) {

								@Override
								protected MagicDeck doInBackground() throws Exception {
									return plug.importDeckFromFile(f);
								}
								
								@Override
								protected void done() {
									super.done();
									
									if(getResult()!=null)
										for (MagicCard mc : getResult().getMain().keySet())
											addCard(mc);
								}
							};
							ThreadManager.getInstance().runInEdt(sw,"import alarms");
						}
					});

					menu.add(it);
				}
			}

			Component b = (Component) ae.getSource();
			Point point = b.getLocationOnScreen();
			menu.show(b, 0, 0);
			menu.setLocation(point.x, point.y + b.getHeight());

		});
		
		
	}
	

	private void updateInfo(MagicCardAlert selected) {
		magicCardDetailPanel.setMagicCard(selected.getCard());
		variationPanel.init(selected.getCard(), null, selected.getCard().getName());
		pricesTablePanel.init(selected.getCard(), selected.getCard().getCurrentSet(),selected.isFoil());
	}

	private void addCard(MagicCard mc) {
		MagicCardAlert alert = new MagicCardAlert();
		alert.setCard(mc);
		alert.setPrice(1.0);
		alert.setId(IDGenerator.generate(mc));
		try {
			getEnabledPlugin(MTGDao.class).saveAlert(alert);
		} catch (SQLException e) {
			logger.error(e);
		}
		model.fireTableDataChanged();

	}


	private List<MagicCardAlert> extract(int[] ids) {
		List<MagicCardAlert> select = new ArrayList<>();

		for (int l : ids) {
			select.add(((MagicCardAlert) table.getValueAt(l, 0)));
		}
		return select;

	}

}
