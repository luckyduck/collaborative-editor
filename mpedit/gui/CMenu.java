/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CMenu.java,v 1.3 2004/10/05 13:06:53 lucky Exp $
 */
package mpedit.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

public class CMenu extends JMenuBar
{
	// contains a reference to CMainFrame
	private CMainFrame m_mainframe;

	// a reference to m_text in CMainFrame
	private JTextArea m_text;

	// the menuitems
	private JMenuItem[] m_fileItems;
	private JMenuItem[] m_editItems;
	private JMenuItem[] m_connItems;

	// our little undo and redo helpers. i love them,
	// they're soooo cute! :P
	// we need a undo manager
	private UndoManager m_undoManager = new UndoManager();

	// this variable can hold the filename of the file where we save our document
	// if it contains something, we dont call a savedialog again
	private String m_saveFile = null;

	/**
	 * our constructor
	 * @param mainframe a reference to the mainframe
	 */
	public CMenu(CMainFrame mainframe)
	{
		m_mainframe = mainframe;
		m_text = mainframe.getM_text();

		// create a menubar
		JMenuBar tmpMenuBar;
		try
		{
			tmpMenuBar = initMenu();
			this.add(tmpMenuBar);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 * We create and return the menubar inside of this method
	 *
	 * @return tmpMenuBar Returns the created menubar with the attached items
	 * @throws Exception
	 */
	private JMenuBar initMenu() throws Exception
	{
		// create a tmp menubar which we'll return
		JMenuBar tmpMenuBar = new JMenuBar();

		// menu and items for the 'File' menu
		String fileTitle = "File";
		String[] fileItems =
			{ "New", "Open...", "Save", "Save as...", "Print...", "Exit" };
		char[] fileShortcuts = { 'N', 'O', 'S', ' ', 'P', 'Q' };
		int[] fileSeperators = { 2, 5 };
		JMenu menuFile =
			createMenu(
				fileTitle,
				fileItems,
				fileShortcuts,
				initFileListener(),
				fileSeperators,
				'f');
		tmpMenuBar.add(menuFile);

		// menu and items for the 'Edit' menu
		String editTitle = "Edit";
		String[] editItems = { "Undo", "Redo", "Copy", "Cut", "Paste", "Find..." };
		char[] editShortcuts = { 'Z', 'Y', 'C', 'X', 'V', 'F' };
		int[] editSeperators = { 2, 5 };
		JMenu menuEdit =
			createMenu(
				editTitle,
				editItems,
				editShortcuts,
				initEditListener(),
				editSeperators,
				'e');
		tmpMenuBar.add(menuEdit);

		// menu and items for the 'Connection' menu
		String connectionTitle = "Connection";
		String[] connectionItems =
			{ "Connect to...", "Server...", "Sync", "Disconnect" };
		char[] connectionShortcuts = { ' ', ' ', ' ', 'D' };
		int[] connectionSeperators = { 2, 4 };
		JMenu menuConn =
			createMenu(
				connectionTitle,
				connectionItems,
				connectionShortcuts,
				initConnListener(),
				connectionSeperators,
				'c');
		tmpMenuBar.add(menuConn);

		// disable items in the connection menu
		m_connItems[2].setEnabled(false); // sync is disabled
		m_connItems[3].setEnabled(false); // disconnect is also disabled

		// return the created menu
		return tmpMenuBar;
	}

	/**
	 * initialises the fileListener
	 *
	 *@return fileListener Returns the ready-to-use fileListener
	 **/
	private ActionListener initFileListener()
	{
		final CFileActions fileActions = new CFileActions();
		final JFileChooser chooser = new JFileChooser();

		// the actionlistener for the File menu
		ActionListener fileListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String clicked = event.getActionCommand();

				if (clicked.equalsIgnoreCase("New"))
				{
					if (JOptionPane
						.showConfirmDialog(
							null,
							"This destroys your current document! Continue?",
							"choose one",
							JOptionPane.YES_NO_OPTION)
						== JOptionPane.YES_OPTION)
					{
						m_saveFile = null;
						m_mainframe
							.getM_document()
							.removeUndoableEditListener(
							m_mainframe.getM_undoManager());
						m_text.setText("");
						m_mainframe
							.getM_document()
							.addUndoableEditListener(
							m_mainframe.getM_undoManager());
					}
				} else if (clicked.equalsIgnoreCase("Open..."))
				{
					if (JOptionPane
						.showConfirmDialog(
							null,
							"This destroys your current document! Continue?",
							"choose one",
							JOptionPane.YES_NO_OPTION)
						== JOptionPane.YES_OPTION)
					{
						int returnVal =
							chooser.showOpenDialog(m_mainframe);
						if (returnVal == JFileChooser.APPROVE_OPTION)
						{
							m_mainframe
								.getM_document()
								.removeUndoableEditListener(
								m_mainframe.getM_undoManager());

							m_text.setText(
								fileActions
									.openDocument(
										chooser
											.getSelectedFile()
											.getPath())
									.toString());
							m_mainframe
								.getM_document()
								.addUndoableEditListener(
								m_mainframe.getM_undoManager());
						}
					}

				} else if (clicked.equalsIgnoreCase("Save"))
				{
					if (m_saveFile == null)
					{
						int returnVal =
							chooser.showSaveDialog(m_mainframe);
						if (returnVal == JFileChooser.APPROVE_OPTION)
						{
							m_saveFile =
								chooser
									.getSelectedFile()
									.getPath();

							if (!fileActions
								.saveDocument(
									m_saveFile,
									m_text))
							{
								JOptionPane.showMessageDialog(
									m_mainframe,
									"Couldn't save to "
										+ m_saveFile);
								m_saveFile = null;
							}
						}
					} else
					{
						fileActions.saveDocument(m_saveFile, m_text);
					}
				} else if (clicked.equalsIgnoreCase("Save as..."))
				{
					int returnVal = chooser.showSaveDialog(m_mainframe);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						String fileName =
							chooser.getSelectedFile().getPath();
						if (!fileActions
							.saveDocument(fileName, m_text))
							JOptionPane.showMessageDialog(
								m_mainframe,
								"Couldn't save to "
									+ m_saveFile);
					}
				} else if (clicked.equalsIgnoreCase("Print..."))
				{
					// TODO: implement File->Print...
				} else if (clicked.equals("Exit"))
				{
					if (m_mainframe.getM_manager() != null)
						m_mainframe.killConnManager();
					System.exit(0);
				}
			}
		};
		return fileListener;
	}

	/**
	 * initialises the editListener
	 *
	 *@return editListener Returns the ready-to-use editListener
	 **/
	private ActionListener initEditListener()
	{
		// the actionlistener for the Edit menu
		ActionListener editListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String clicked = event.getActionCommand();

				if (clicked.equalsIgnoreCase("Undo"))
				{
					if (m_undoManager.canUndo())
						m_undoManager.undo();
				} else if (clicked.equalsIgnoreCase("Redo"))
				{
					if (m_undoManager.canRedo())
						m_undoManager.redo();
				} else if (clicked.equalsIgnoreCase("Copy"))
				{
					Clipboard cb =
						Toolkit
							.getDefaultToolkit()
							.getSystemClipboard();
					String s = m_text.getSelectedText();
					StringSelection contents = new StringSelection(s);
					cb.setContents(contents, null);
				} else if (clicked.equalsIgnoreCase("Cut"))
				{
					Clipboard cb =
						Toolkit
							.getDefaultToolkit()
							.getSystemClipboard();
					String s = m_text.getSelectedText();
					StringSelection contents = new StringSelection(s);
					cb.setContents(contents, null);
					m_mainframe.removeText(
						m_text.getSelectionStart(),
						(m_text.getSelectionEnd()
							- m_text.getSelectionStart()));
				} else if (clicked.equalsIgnoreCase("Paste"))
				{
					Clipboard cb =
						Toolkit
							.getDefaultToolkit()
							.getSystemClipboard();
					Transferable content = cb.getContents(this);
					try
					{
						String s =
							(String) content.getTransferData(
								DataFlavor.stringFlavor);
						m_mainframe.insertText(
							m_text.getCaretPosition(),
							s);
					} catch (Throwable e)
					{
						System.err.println(e);
					}
				} else if (clicked.equalsIgnoreCase("Find..."))
				{
					// TODO: implement Edit->Find...
				}
			}
		};
		return editListener;
	}

	/**
	 * initialises the connListener
	 *
	 *@return connListener Returns the ready-to-use connListener
	 **/
	private ActionListener initConnListener()
	{
		// The ActionListener for the 'Connection' menu
		ActionListener connListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String clicked = event.getActionCommand();

				if (clicked.equalsIgnoreCase("Connect to..."))
				{
					m_mainframe.onClient();
				} else if (clicked.equalsIgnoreCase("Server..."))
				{
					m_mainframe.onServer();
				} else if (clicked.equalsIgnoreCase("Sync"))
				{
					m_mainframe.getM_manager().syncData();
				} else if (clicked.equalsIgnoreCase("Disconnect"))
				{
					m_mainframe.killConnManager();
				}
			}
		};
		return connListener;
	}

	/**
	 * Creates menus out of given arguments
	 * @param title The menutitle
	 * @param menuItems The items of the menu
	 * @param menuShortcuts The keyboard shortcuts for the respective items
	 * @param listener The ActionListener which belongs to this menu
	 * @param seperators Positions where we should insert Seperators
	 * @return tmpMenu The created menu
	 */
	private JMenu createMenu(
		String title,
		String[] menuItems,
		char[] menuShortcuts,
		ActionListener listener,
		int[] seperators,
		char menuname)
	{
		JMenu tmpMenu = new JMenu(title);
		JMenuItem[] items = new JMenuItem[menuItems.length];

		for (int i = 0; i < menuItems.length; i++)
		{
			if (menuShortcuts[i] == ' ')
				items[i] = new JMenuItem(menuItems[i]);
			else
				items[i] = new JMenuItem(menuItems[i], menuShortcuts[i]);

			if (!(menuShortcuts[i] == ' '))
			{
				items[i].setAccelerator(
					KeyStroke.getKeyStroke(
						menuShortcuts[i],
						Toolkit
							.getDefaultToolkit()
							.getMenuShortcutKeyMask(),
						false));
			}
			items[i].addActionListener(listener);
			tmpMenu.add(items[i]);
		}

		for (int i = 0; i < seperators.length; i++)
			tmpMenu.insertSeparator(seperators[i]);

		if (menuname == 'f')
			m_fileItems = items;
		else if (menuname == 'e')
			m_editItems = items;
		else if (menuname == 'c')
			m_connItems = items;

		return tmpMenu;
	}

	/**
	 * @return Returns a reference to the array with the items
	 * of the Connection menu
	 */
	public JMenuItem[] getM_connItems()
	{
		return m_connItems;
	}

	/**
	 * @return Returns a reference to the array with the items
	 *  of the Edit menu
	 */
	public JMenuItem[] getM_editItems()
	{
		return m_editItems;
	}

	/**
	 * @return Returns a reference to the array with the items
	 * of the File menu
	 */
	public JMenuItem[] getM_fileItems()
	{
		return m_fileItems;
	}

	/**
	 * @return Returns a reference to the undo manager
	 */
	public UndoManager getM_undoManager()
	{
		return m_undoManager;
	}

}
