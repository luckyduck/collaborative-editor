/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CFileActions.java,v 1.1.1.1 2004/10/04 18:45:35 vulture Exp $
 */
package mpedit.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JTextArea;

public class CFileActions
{
	/**
	 *
	 * @param fileName Where do you want to save your document ?
	 * @return Returns true if nothing went wrong , false if the opposite is correct
	 */
	public boolean saveDocument(String fileName, JTextArea textarea)
	{
		boolean trueOrFalse = false;

		StringBuffer textBuffer = new StringBuffer(textarea.getText());
		try
		{
			PrintWriter outFile =
				new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			outFile.print(textBuffer.toString() + "\n");
			outFile.close();
			trueOrFalse = true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return trueOrFalse;
	}

	/**
	 *
	 * @param fileName The name of the file we want to open
	 * @return Returns a StringBuffer which is filled with data from the file
	 */
	public StringBuffer openDocument(String fileName)
	{
		StringBuffer returnBuffer = new StringBuffer();
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			try
			{
				String line;

				while ((line = in.readLine()) != null)
					returnBuffer.append(line + "\n");

				in.close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		return returnBuffer;

	}
}
