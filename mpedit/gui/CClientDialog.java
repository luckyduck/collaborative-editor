/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CClientDialog.java,v 1.1.1.1 2004/10/04 18:45:35 vulture Exp $
 */

package mpedit.gui;
import javax.swing.*;
import java.awt.*;
import mpedit.exception.*;

public class CClientDialog extends CDialogTemplate
{
	public String m_ip;
	public int m_port;
	public String m_nickname;
	public String m_password;

	private JTextField m_field_ip, m_field_port, m_field_nickname;
	private JPasswordField m_field_password;

	public CClientDialog(Frame parent)
	{
		super(parent, "Connect to...");
		this.m_ip = null;
		this.m_port = 0;
		this.m_nickname = null;
		this.m_password = null;

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

		// create pannels and add labels/text field in them
		mainPanel.add(new JLabel("IP/Hostname:"));
		m_field_ip = new JTextField(10);

		mainPanel.add(m_field_ip);
		mainPanel.add(new JLabel("Port:"));
		m_field_port = new JTextField(4);
		m_field_port.setText("2305");
		mainPanel.add(m_field_port);

		JPanel panel_nick = new JPanel();

		mainPanel.add(new JLabel("Username:"));
		m_field_nickname = new JTextField(15);
		mainPanel.add(m_field_nickname);

		mainPanel.add(new JLabel("Password:"));
		m_field_password = new JPasswordField(15);
		mainPanel.add(m_field_password);

		getContentPane().add(mainPanel, BorderLayout.CENTER);

		//set the size of the Dialog
		this.setSize(400, 150);
	}

	protected void onOK()
	{
		try
		{
			// get and check Port
			this.m_ip = m_field_ip.getText();
			this.m_port = Integer.parseInt(m_field_port.getText());
			this.m_password = new String(m_field_password.getPassword());
			if (this.m_port > 65535)
			{
				NumberFormatException ex =
					new NumberFormatException(
						"Port too large: " + this.m_port);
				throw ex;
			}
			//same for username, empty username blows
			this.m_nickname = m_field_nickname.getText();
			if (m_nickname.length() < 1)
			{
				BadUsernameException ex =
					new BadUsernameException("empty username");

				throw ex;
			}

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
