/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CConnectionManager.java,v 1.5 2004/10/07 13:19:32 vulture Exp $
 */
package mpedit.net;

import mpedit.gui.CMainFrame;

/**
 * This is the ConnectionManager which handles the server or the client.
 * @author Alexander Zielke, Jan Brinkmann
 */
public abstract class CConnectionManager
{
    protected CMainFrame m_mainframe;

    /**
     * This is the default construktor. It takes a references to CMainFrame to call the methods,
     * which insert and remove text and to get access to the userlist.
     * @param frame CMainFrame
     */
    public CConnectionManager( CMainFrame frame )
    {
        this.m_mainframe = frame;
    }

    public abstract void sendRemove( int start, int offset );

    public abstract void sendInsert( int offset, String data );

    public abstract void onData( byte[] data );

    public abstract void destroy();

    public abstract void syncData();

    public abstract void onSocketException(CThreadedSocket s, Exception e);

    //public abstract void onClosedSocket(CThreadedSocket s);

    /**
     * This method removes Text from the Document.
     * The positions are defined in CPacket
     * The offset ist m_startpos and the length is m_endpos
     * @see CMainFrame #removeText
     * @param p CPacket
     */
    protected void onRemove( CPacket p )
    {
        m_mainframe.removeText( p.m_startpos, p.m_endpos );
    }

    /**
     * This method inserts an string at the given ofset m_startpos from the packet
     * into the document. The String is defined in the Packet as m_data
     * @param p CPacket
     */
    protected void onInsert( CPacket p )
    {
        m_mainframe.insertText( p.m_startpos, p.m_data );
    }

    /**
     * This method is called, when a new Client has joined the session.
     * The name of the client is saved in the Packet as m_data
     * @param p CPacket
     */
    protected void onNewClient( CPacket p )
    {
        m_mainframe.getUserlist().addClient( p.m_data );
    }
}
