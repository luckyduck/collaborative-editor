/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CClientManager.java,v 1.5 2004/10/07 13:19:32 vulture Exp $
 */
package mpedit.net;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import mpedit.exception.PacketInconsistencyException;
import mpedit.gui.CMainFrame;

/**
 * This is the Connection Manager for a Client-Session. It handles input from the client and
 * sends it to the server and receives signals from the Server, such as Syncs and ClientLists.
 * It is derived from CConnectionManager for easier usage in the MainFrame
 * @author Alexander Zielke
 */
public class CClientManager
    extends CConnectionManager
{
    private int m_id;
    private CThreadedSocket m_socket;
    private String m_nickname;
    private String m_password;

    private StringBuffer m_syncBuffer;

    public CClientManager( CMainFrame parent, String hostname,
                           int port, String password, String nickname )
    {
        super( parent );
        System.err.println( "CClientManager::CClientManager()" );
        try
        {
            m_socket = new CThreadedSocket( this, hostname, port );
        }
        catch( IOException e )
        {
            JOptionPane.showMessageDialog( null, "Error initializing Socket" );
        }
        this.m_password = password;
        this.m_nickname = nickname;
    }

    /**
     * This function is called whenever the Client receives Data.
     * It then translates it into an CPacket and dispatches it to the
     * appropriate function to handle the request
     * @see CProtokoll
     * @see CPacket
     * @param data byte[]
     */
    public void onData( byte[] data )
    {
        System.err.println( "CClientManager::onData()" );
        CPacket p = null;
        try
        {
            p = CProtokoll.receiveTranslate( data );
        }
        catch( PacketInconsistencyException e )
        {
            JOptionPane.showMessageDialog( null, "Inconsistent Packet received!" );
            return;
        }

        if( p.m_action == CProtokoll.ADDTEXT )
            onInsert( p );
        else if( p.m_action == CProtokoll.REMOVETEXT )
            onRemove( p );
        else if( p.m_action == CProtokoll.NEWCLIENT )
            onNewClient( p );
        else if( p.m_action == CProtokoll.SYNCDATA )
            onSyncData( p );
        else if( p.m_action == CProtokoll.SYNC )
            onSync( p );
        else if( p.m_action == CProtokoll.CLIENTLISTDATA )
            onClientListData( p );
        else if( p.m_action == CProtokoll.DISCONNECT )
            onDisconnect( p );
        else if( p.m_action == CProtokoll.EOF )
            onEOF( p );
        else if( p.m_action == CProtokoll.ACCEPT )
            onAccept( p );
        else if( p.m_action == CProtokoll.UNAUTHORIZED )
            onUnauthorized( p );
        else if( p.m_action == CProtokoll.ASSIGNID )
            onAssignID( p );
        else
            System.out.println( "Unimplemented action in Packet!" );
    }

    /**
     * This method is called when the user of this terminal has removed text
     * inside his document. The function sends a REMOVETEXT packet to the server.
     * @param start int
     * @param offset int
     */
    public void sendRemove( int start, int offset )
    {
        System.err.println( "CClientManager::sendRemove()" );
        //create a new packet and fill it with data
        CPacket p = new CPacket();
        p.m_action = CProtokoll.REMOVETEXT;
        p.m_clientID = this.m_id;
        p.m_startpos = start;
        p.m_endpos = offset;

        //translate the packet to an bytearray, so it can be sent
        byte[] b = CProtokoll.sendTranslate( p );
        m_socket.writeData( b );
    }

    /**
     * This function is called when the user inserts a string.
     * It sends an ADDTEXT packet to the server, containing the offset and the
     * String itself.
     * @param offset int
     * @param data String
     */
    public void sendInsert( int offset, String data )
    {
        System.err.println( "CClientManager::sendInsert()" );
        CPacket p = new CPacket();
        p.m_action = CProtokoll.ADDTEXT;
        p.m_clientID = this.m_id;
        p.m_startpos = offset;
        p.m_data = data;

        m_socket.writeData( CProtokoll.sendTranslate( p ) );
    }

    /**
     * This function is called when the client wants to disconnect from
     * the server. It sends a DISCONNECT packet to the server and then closes the
     * socket.
     */
    public void destroy()
    {
        CPacket p = new CPacket();
        p.m_clientID = m_id;
        p.m_action = CProtokoll.DISCONNECT;
        m_socket.writeData( CProtokoll.sendTranslate( p ) );
        m_socket.close();
        m_socket = null;
    }

    /**
     * This function is called by the MainFrame when the User has requested a sync of Documents
     */
    public void syncData()
    {
        CPacket p = new CPacket();
        p.m_action = CProtokoll.SYNC;
        p.m_clientID = m_id;
        m_socket.writeData( CProtokoll.sendTranslate( p ) );
    }

    /**
     * This function is called when SyncData arrives. Most times the sync is initiaded
     * through the client. This function uses a StringBuffer to buffer the packets until
     * the Packet with m_startpos == 0 arrives. It indicates the last packet of
     * the SYNCDATA chain. The Stringbuffer is then casted to a String and inserted
     * into the document. The document will be erased completly prior insertion.
     * @param p CPacket
     */
    private void onSyncData( CPacket p )
    {
        System.err.println( "CClientManager::onSyncData()" );
        if( m_syncBuffer == null ) //if there is no sync buffer, then it hat to be the start of a sync
        {
            m_syncBuffer = new StringBuffer(); //create a new one
            m_mainframe.removeAllText(); // and delete all text inside the document
        }
        if( p.m_startpos == 0 )
        {
            m_syncBuffer.append( p.m_data );
            m_mainframe.insertText( 0, new String( m_syncBuffer ) );
            m_syncBuffer = null;
        }
        else
        {
            m_syncBuffer.append( p.m_data );
        }
    }

    /**
     * This function is called when a User disconnects from the Server. The User is removed from
     * the Userlist.
     * @param p CPacket
     */
    private void onDisconnect( CPacket p )
    {
        m_mainframe.getUserlist().removeClient( p.m_data );
    }

    /**
     * This method is called when a Packet with a new Client list
     * arrives. It is then processed to an array of String objects and then
     * passed to the client list, which will be prior erased.
     * @param CPacket p
     */
    private void onClientListData( CPacket p )
    {
        m_mainframe.getUserlist().removeAllClients();
        StringTokenizer tok = new StringTokenizer( p.m_data, "\0" );
        while( tok.hasMoreTokens() )
            m_mainframe.getUserlist().addClient( tok.nextToken() );
    }

    /**
     * This method is called, when the Server sent EOF, indicating that it is shutting down.
     * Kill the socket and empty client list. This is done by a call of killConnManager of the
     * mainframe. The mainframe the resets itself so a new connection can be established.
     * A message box will notify the user of server shutdown.
     * @param p CPacket
     */
    private void onEOF( CPacket p )
    {
        JOptionPane.showMessageDialog( null, "EOF: Server shutdown" );
        m_mainframe.killConnManager();
    }

    /**
     * This function is called, when the Server sent a SYNC Packet.
     * It indicates, that the server had an BadLocaltionExcetption and that the
     * documents are out of Sync. Resync is recommended.
     * @param p CPacket
     */
    private void onSync( CPacket p )
    {
        if( JOptionPane.showConfirmDialog( null, "Documents out of sync! Perform resync?",
                                           "Out of Sync!", JOptionPane.YES_NO_OPTION ) ==
            JOptionPane.OK_OPTION )
        {
            CPacket q = new CPacket();
            q.m_action = CProtokoll.SYNC;
            q.m_clientID = m_id;
            m_socket.writeData( CProtokoll.sendTranslate( q ) );
        }
    }

    /**
     * This function is called when the server accepts an REQUEST.
     * The client then sends SYNC to get the actual content of the document.
     * @param CPacket p
     */
    private void onAccept( CPacket p )
    {
        System.err.println( "CClientManager::onAccept()" );
        CPacket q = new CPacket();
        q.m_action = CProtokoll.SYNC;
        q.m_clientID = m_id;
        m_socket.writeData( CProtokoll.sendTranslate( q ) );
        q.m_action = CProtokoll.GETCLIENTLIST;
        m_socket.writeData( CProtokoll.sendTranslate( q ) );
    }

    /**
     * This function is called, when the requested connection ins UNAUTHORIZED.
     * It will Display an MessageDialog with UNAUTHORIZED and the reason.
     * Then it tells the MainFrame to kill the connection via the ConnectionManagers
     * destory method.
     * @param CPacket p
     */
    private void onUnauthorized( CPacket p )
    {
        //Connection was not Accepted, kill it!
        //The reason is in p.m_data
        JOptionPane.showMessageDialog( null, "UNAUTHORIZED: " + p.m_data );
        m_mainframe.killConnManager();
    }

    /**
     * The first stuff which is sent is an ASSIGNID packet from the server to the
     * client. Clients with an invalid ID will be Dropped immediatly.
     * @param CPacket p
     */
    private void onAssignID( CPacket p )
    {
        this.m_id = p.m_startpos;
        sendRequest();
    }

    /**
     * This is the first packet which is client->server.
     * It sends the password as m_data.
     */
    private void sendRequest()
    {
        CPacket p = new CPacket();
        p.m_action = CProtokoll.REQUEST;
        p.m_clientID = m_id;
        //TODO: add nickname to m_data
        p.m_data = new String( "" + m_nickname + "\0" + m_password );
        m_socket.writeData( CProtokoll.sendTranslate( p ) );
    }


    public void onSocketException( CThreadedSocket s, Exception e )
    {
        JOptionPane.showMessageDialog(null, "SocketException: "+e.getCause());
        m_mainframe.killConnManager();
    }
}
