/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CListener.java,v 1.2 2004/10/05 12:54:05 vulture Exp $
 */
package mpedit.net;

import java.io.IOException;
import java.net.*;

/**
 * This is the Class, which listens for incoming connections, creates new Sockets
 * upon connection and then tells the ConnectionManager about the new Connection.
 * @author Alexander Zielke, Jan Brinkmann
 */
public class CListener
    extends Thread
{
    private CServerManager m_manager;
    private int m_port;
    
    private ServerSocket m_serverSocket;

    public CListener( CServerManager manager, int port )
    {
        super( "Listener" ); // set the threads name "aus zweabug decken"
        System.err.println( "CListener::CListener()" );
        this.m_manager = manager;
        this.m_port = port;
        this.start(); // autostart thread afer consturctor call
    }

    /**
     * This is the Thread which listens for incomming connections.
     * If there is one, the Server will create a Socket and then call the
     * ServerManagers onListenerAcceptedClient method
     * @see CServerManager
     */
    public void run()
    {
        System.err.println( "CListener::run()" );
        try
        {
            m_serverSocket = new ServerSocket( m_port );
            while( true )
            {
                if( this.isInterrupted() )
                    break;

                // create new socket and wait for client to connect
                Socket client = new Socket();
                client = m_serverSocket.accept();
                m_manager.onListenerAcceptedClient( new CThreadedSocket( m_manager, client ) );
            }
        }
        catch( SocketException e )
        {
            //Do nothing! Server has been killed!
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    /**
     * This function is used to close the ServerSocket and stop it from
     * listening to incoming Connections.
     */
    public void close()
    {
		try
		{
			m_serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }
}
