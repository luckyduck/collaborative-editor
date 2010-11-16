/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CUserlist.java,v 1.1.1.1 2004/10/04 18:45:37 vulture Exp $
 */
package mpedit.gui;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class CUserlist extends JList
{
	// the visible clientlist on the right
	private static DefaultListModel m_listModel = new DefaultListModel();

	public CUserlist()
	{
		super(m_listModel);
	}

	/**
	 * This method adds a client to the visual userlist
	 * @param nickname The nickname which should be added
	 */
	public void addClient(String nickname)
	{
		m_listModel.addElement(nickname);
	}

	/**
	 * This method removes a given username from the userlist
	 * @param nickname The nickname which should be removed
	 */
	public void removeClient(String nickname)
	{
		m_listModel.removeElement(nickname);
	}

	/**
	 * This method removes all usernames from our nifty
	 * userlist. this seems to be usefull, i'm impressed! :)
	 */
	public void removeAllClients()
	{
		m_listModel.removeAllElements();
	}
}
