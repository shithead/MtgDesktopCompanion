package org.magic.gui.renderer;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.magic.api.beans.JsonMessage;
import org.magic.gui.components.renderer.JsonMessagePanel;

public class JsonMessageRenderer implements ListCellRenderer<JsonMessage> {

	@Override
	public Component getListCellRendererComponent(JList<? extends JsonMessage> list, JsonMessage value, int index,boolean isSelected, boolean cellHasFocus) {
		var panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JPanel(),BorderLayout.NORTH);
		panel.add(new JsonMessagePanel(value),BorderLayout.CENTER);
		return panel;
	}}
