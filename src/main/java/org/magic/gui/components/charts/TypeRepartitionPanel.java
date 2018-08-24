package org.magic.gui.components.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicDeck;
import org.magic.services.MTGDeckManager;

public class TypeRepartitionPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<MagicCard> cards;
	private transient MTGDeckManager manager;

	public TypeRepartitionPanel() {
		manager = new MTGDeckManager();
		setLayout(new BorderLayout(0, 0));
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent componentEvent) {
				init(cards);
			}
		});
	}

	public void init(MagicDeck deck) {
		if(deck!=null)
			init(deck.getAsList());
	}
	public void init(List<MagicCard> cards) {
		this.cards = cards;
		
		if(isVisible())
			refresh();
	}


	private void refresh() {
		this.removeAll();
		
		if(cards==null)
			return;
		
		JFreeChart chart = ChartFactory.createPieChart3D("Type repartition", // chart title
				getTypeRepartitionDataSet(), // data
				false, // include legend
				true, true);

		ChartPanel pane = new ChartPanel(chart);
		this.add(pane, BorderLayout.CENTER);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setSectionPaint("B", Color.BLACK);
		plot.setSectionPaint("W", Color.WHITE);
		plot.setSectionPaint("U", Color.BLUE);
		plot.setSectionPaint("G", Color.GREEN);
		plot.setSectionPaint("R", Color.RED);
		plot.setSectionPaint("multi", Color.YELLOW);
		plot.setSectionPaint("uncolor", Color.GRAY);

		PieSectionLabelGenerator generator = new StandardPieSectionLabelGenerator("{0} = {1}", new DecimalFormat("0"),
				new DecimalFormat("0.00%"));
		plot.setLabelGenerator(generator);
		
		chart.fireChartChanged();
		pane.revalidate();
	}


	private PieDataset getTypeRepartitionDataSet() {
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (Entry<String, Integer> entry : manager.analyseTypes(cards).entrySet()) {
			dataset.setValue(entry.getKey(), entry.getValue());
		}

		return dataset;
	}

}
