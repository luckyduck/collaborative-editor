/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: PacketInconsistencyException.java,v 1.1.1.1 2004/10/04 18:45:39 vulture Exp $
 */
package mpedit.exception;

public class PacketInconsistencyException extends Exception
{
	public PacketInconsistencyException(String text)
	{
		super(text);
	}
}
