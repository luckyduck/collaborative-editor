/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CDialogTemplate.java,v 1.1.1.1 2004/10/04 18:45:35 vulture Exp $
 */
package mpedit.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class CDialogTemplate extends JDialog implements ActionListener
{
	protected boolean m_bool = false;

	public CDialogTemplate(Frame parent, String title)
	{
		super(parent, title, true);
	}

	protected void init()
	{
		getContentPane().setLayout(new BorderLayout());
		this.setResizable(false);

		JPanel panel_buttons = this.drawButtonPanel();

		getContentPane().add(panel_buttons, BorderLayout.SOUTH);

		//and finally the WindowAdapter
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setLocation(
			(int) (Toolkit.getDefaultToolkit().getScreenSize().width / 4),
			(int) (Toolkit.getDefaultToolkit().getScreenSize().height / 4));
	}

	public JPanel drawButtonPanel()
	{
		JPanel panel_buttons = new JPanel();
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		panel_buttons.add(ok);
		panel_buttons.add(cancel);

		//add action listener, you may have guessed it :)
		//this class has ActionListerner implemented, so we just use it
		ok.addActionListener(this);
		cancel.addActionListener(this);

		return panel_buttons;
	}

	public void actionPerformed(ActionEvent e)
	{
		//which of our 2 buttons has been clicked
		if (e.getActionCommand().equals("OK"))
			this.onOK();
		else
			this.onCancel();
	}

	public boolean doModal()
	{
		show();
		return m_bool;
	}

	protected void onOK()
	{
		m_bool = true;
		dispose();
	}

	protected void onCancel()
	{
		m_bool = false;
		dispose();
	}
}
