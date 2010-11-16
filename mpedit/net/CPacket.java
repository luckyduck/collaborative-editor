/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CPacket.java,v 1.3 2004/10/06 15:24:15 vulture Exp $
 */
package mpedit.net;

// this would have been a 'struct' in C
//TODO: reimplements with 3 strings for error checking with the
// "the jumping letters"-bug
public class CPacket
{
	public byte m_action;
	public int m_clientID;
	public int m_startpos;
	public int m_endpos;
	public String m_data;
}
