/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CDocListener.java,v 1.1.1.1 2004/10/04 18:45:37 vulture Exp $
 */
package mpedit.gui;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import mpedit.net.CConnectionManager;

/**
 * the documentlistener which gets notified if something changed
 */
public class CDocListener implements DocumentListener
{
	// our connection manager
	CConnectionManager m_connManager;

	// a reference to the typing area, we need this to
	// read information from it such as inserted text and
	// the position from it.
	JTextArea m_text;

	public CDocListener(CConnectionManager cm, JTextArea textarea)
	{
		System.err.println("CDocListener::CDocListener");
		m_connManager = cm;
		m_text = textarea;
	}

	/**
	 * This method is called when something new
	 * was added to the Document
	 */
	public void insertUpdate(DocumentEvent docEvent)
	{
		System.err.println("CDocListener::insertUpdate");
		try
		{
			m_connManager.sendInsert(
				docEvent.getOffset(),
				m_text.getText(docEvent.getOffset(), docEvent.getLength()));
		} catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This method is called when something was
	 * removed from the document
	 */
	public void removeUpdate(DocumentEvent docEvent)
	{
		System.err.println("CDocListener::removeUpdate");
		m_connManager.sendRemove(docEvent.getOffset(), docEvent.getLength());
	}

	/**
	 * actually we dont really need this method. the
	 * only reason why you can see this here is because
	 * we implement the DocumentListener interface
	 */
	public void changedUpdate(DocumentEvent docEvent)
	{
		// TODO: implement styles in our document
	}
}
