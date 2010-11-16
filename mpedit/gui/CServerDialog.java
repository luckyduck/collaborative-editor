/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CServerDialog.java,v 1.2 2004/10/04 18:50:06 vulture Exp $
 */
package mpedit.gui;

import mpedit.exception.BadUsernameException;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CServerDialog extends CDialogTemplate
{
	public int m_port;
	public String m_password;
	public String m_nickname;

	private JTextField m_field_port, m_field_nickname, m_field_password;

	public CServerDialog(Frame parent)
	{
		super(parent, "Create Server...");

		try
		{
			init();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void init()
	{
		super.init();

		// mainPanel
		JPanel mainPanel = new JPanel(new GridLayout(4, 4));

		// a textfield for the listenport
		mainPanel.add(new JLabel("Port:"));
		m_field_port = new JTextField(4);
		m_field_port.setText("2305");
		mainPanel.add(m_field_port);

		// a textfield for the username
		mainPanel.add(new JLabel("Username:"));
		m_field_nickname = new JTextField(15);
		mainPanel.add(m_field_nickname);

		// a textfield for the password
		mainPanel.add(new JLabel("Password:"));
		m_field_password = new JTextField(15);
		mainPanel.add(m_field_password);

		//add panels to content pane
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		//set dialog size
		this.setSize(400, 150);
	}

	protected void onOK()
	{
		try
		{
			// get and check Port
			this.m_port = Integer.parseInt(m_field_port.getText());
			this.m_password = m_field_password.getText();
			if (this.m_port > 65535)
			{
				NumberFormatException ex = new NumberFormatException("Port too large: " + this.m_port);
				throw ex;
			}
			//same for username, empty username blows
			this.m_nickname = m_field_nickname.getText();
			if (m_nickname.length() < 1)
			{
				BadUsernameException ex = new BadUsernameException("empty username");
				throw ex;
			}

			//dialog was completed successfully
			this.m_bool = true;
			dispose();
		} catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(null, "Port out of Range(1-65535)");
		} catch (BadUsernameException e)
		{
			JOptionPane.showMessageDialog(null, "Empty Username");
		}
	}
}
