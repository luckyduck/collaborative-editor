/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CMainFrame.java,v 1.5 2004/10/07 19:00:52 lucky Exp $
 */
package mpedit.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import mpedit.net.CClientManager;
import mpedit.net.CConnectionManager;
import mpedit.net.CServerManager;

public class CMainFrame extends JFrame
{
	// the main textarea for editing
	private JTextArea m_text = new JTextArea();

	// our "document handler"
	private Document m_document = m_text.getDocument();

	// a document listener
	private CDocListener m_documentListener;

	// the JList which contains a list of all connected users
	private CUserlist m_userlist = new CUserlist();

	// the connection manager, we initialise it as needed:
	// if we start a server, then we instantiate a servermanager
	// if we act as a client, we instantiate a clientmanager
	private CConnectionManager m_manager;

	// the menubar
	private CMenu m_menubar = new CMenu(this);

	// references to the menuItems
	private JMenuItem[] m_fileItems = m_menubar.getM_fileItems();
	private JMenuItem[] m_editItems = m_menubar.getM_editItems();
	private JMenuItem[] m_connItems = m_menubar.getM_connItems();

	// the undo manager
	private UndoManager m_undoManager = m_menubar.getM_undoManager();

	/**
	 * our constructor which calls init() to fire up teh evil gui
	 */
	public CMainFrame()
	{
		super();
		try
		{
			this.init();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * this method initialises the main window
	 */
	private void init() throws Exception
	{
		// the main title
		setTitle("MPedit");

		int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.7);
		int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.6);

		// set the size and the start location
		setSize(width, height);

		// set the starting point
		setLocation((int) (width / 4), (int) (height / 4));

		// tell swing to exit on close
		addWindowListener(new MyWindowAdapter());

		// init. the menu and add it to the contentpane
		setJMenuBar(m_menubar);

		// set the content pane
		this.getContentPane().add(initMainPanel());

		// finally, show the world the created window =)
		setVisible(true);
	}

	/**
	 * This method creates a JPanel and add's the TextArea and a userlist to it.
	 *
	 * @return tmpPanel The created Panel with the TextArea and the list of users
	 */
	private JPanel initMainPanel()
	{
		// GridBagConstraints, we save constraints for 
		// the parts of our mainframe
		GridBagConstraints gbc = new GridBagConstraints();
		
		// a tmp. panel which holds the main content such as the typing area
		// and a list with all connected users
		GridBagLayout gbl = new GridBagLayout();
		JPanel tmpPanel = new JPanel(gbl);

		// it's maybe usefull to scroll inside the text
		// (who wants to write so much ? :P)
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane scrollText = new JScrollPane(m_text);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 3;
		gbc.weighty = 1;
		gbl.setConstraints(scrollText, gbc);
		tmpPanel.add(scrollText);
		

		// we have to add the JList! here we go =)
		JScrollPane scrollClientList = new JScrollPane(m_userlist);
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbl.setConstraints(scrollClientList, gbc);
		tmpPanel.add(scrollClientList);

		
		// let's tell the document something about our infamous undomanager
		m_document.addUndoableEditListener(m_undoManager);

		return tmpPanel;
	}

	/**
	 * insert text after a given position
	 *
	 * @param text The text which should be inserted
	 * @param offset The offset where we want the text to be inserted
	 */
	public boolean insertText(int offset, String text)
	{
		boolean trueOrFalse = false;

		System.err.println("CMainFrame::insertText()");
		try
		{
			m_document.removeDocumentListener(m_documentListener);
			m_document.insertString(offset, text, null);
			if (offset < m_text.getCaretPosition())
				m_text.setCaretPosition(m_text.getCaretPosition() + text.length());
				trueOrFalse = true;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		} finally
		{
			m_document.addDocumentListener(m_documentListener);
		}

		return trueOrFalse;
	}

	/**
	 * removeText between to given positions
	 *
	 * @param offset The position where we start to remove chars
	 * @param length how many chars should be removed ?
	 */
	public boolean removeText(int offset, int length)
	{
		boolean trueOrFalse = false;

		System.err.println("CMainFrame::removeText");
		try
		{
			m_document.removeDocumentListener(m_documentListener);
			m_document.remove(offset, length);
			if (offset < m_text.getCaretPosition())
				m_text.setCaretPosition(m_text.getCaretPosition() - length);
			trueOrFalse = true;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		} finally
		{
			m_document.addDocumentListener(m_documentListener);
		}
		return trueOrFalse;
	}

	/**
	 * Returns a reference top the visible userlist
	 * @return m_userlist A reference to the userlist
	 */
	public CUserlist getUserlist()
	{
		return m_userlist;
	}

	/**
	 * This method is called when a user wants to start
	 * a new server
	 */
	public void onServer()
	{
		CServerDialog sd = new CServerDialog(this);

		if (sd.doModal())
		{
			m_manager = new CServerManager(this, sd.m_port, sd.m_password, sd.m_nickname);
			m_documentListener = new CDocListener(m_manager, m_text);
			m_document.addDocumentListener(m_documentListener);

			// disable server and client menuitems
			m_connItems[0].setEnabled(false);
			m_connItems[1].setEnabled(false);

			// enable the disconnect and sync feature
			m_connItems[2].setEnabled(true);
			m_connItems[3].setEnabled(true);
		}
	}

	/**
	 * we call this method when a user wants to connect to an
	 * existing server
	 *
	 */
	public void onClient()
	{
		CClientDialog cd = new CClientDialog(this);

		if (cd.doModal())
		{
			m_manager = new CClientManager(this, cd.m_ip, cd.m_port, cd.m_password, cd.m_nickname);
			m_documentListener = new CDocListener(m_manager, m_text);
			m_document.addDocumentListener(m_documentListener);

			// disable open and new from the file menu, clients dont have the right todo this
			m_fileItems[0].setEnabled(false);
			m_fileItems[1].setEnabled(false);

			// disable server and client menuitems
			m_connItems[0].setEnabled(false);
			m_connItems[1].setEnabled(false);

			// enable the disconnect and sync feature
			m_connItems[2].setEnabled(true);
			m_connItems[3].setEnabled(true);
		} else
		{
			System.err.println("something failed");
		}
	}

	/**
	 * this method kills the connectionmanager, used for several things
	 */
	public void killConnManager()
	{
		m_manager.destroy();
		m_manager = null;

		m_document.removeDocumentListener(m_documentListener);
		m_documentListener = null;

		m_userlist.removeAllClients();

		// re-enable new and open on disconnect
		m_fileItems[0].setEnabled(true);
		m_fileItems[1].setEnabled(true);

		// disable server and client menuitems
		m_connItems[0].setEnabled(true);
		m_connItems[1].setEnabled(true);

		// disable the disconnect and sync feature
		m_connItems[2].setEnabled(false);
		m_connItems[3].setEnabled(false);
	}

	/**
	 * this method removes all text from our document, especially needed on sync
	 */
	public void removeAllText()
	{
		synchronized (m_document)
		{
			m_document.removeDocumentListener(m_documentListener);
			m_text.setText("");
			m_text.setCaretPosition(0);
			m_document.addDocumentListener(m_documentListener);
		}
	}
	
	/**
	 * a nifty looking inline windowclosingadapter which
	 * closes our window and also handles disconnects
	 * to avoid "teh evil *exception-hail"
	 */
	class MyWindowAdapter extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			if (m_manager != null)
				killConnManager();
			System.exit(0);
		}
	};
	
	// TODO: implement a merge function, options: beginning..., end..., cursor...  

	/**
	 * Returns a reference to the JTextArea which we use for typing
	 *
	 * @return A reference to our typing area
	 */
	public JTextArea getM_text()
	{
		return m_text;
	}

	/**
	 * @return Returns a reference to our connection manager
	 */
	public CConnectionManager getM_manager()
	{
		return m_manager;
	}

	/**
	 * @return Returns a reference to the document
	 */
	public Document getM_document()
	{
		return m_document;
	}

	/**
	 * @return Returns a reference to the undo manager
	 */
	public UndoManager getM_undoManager()
	{
		return m_undoManager;
	}

}
