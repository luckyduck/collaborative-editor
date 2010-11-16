/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CClient.java,v 1.2 2004/10/05 12:54:05 vulture Exp $
 */
package mpedit.net;

// this would have been a 'struct' in C
public class CClient
{
	public int m_id;
	public boolean m_authed = false;
	public String m_nickname = null;
	public CThreadedSocket m_socket = null;
}
