/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CThreadedSocket.java,v 1.6 2004/10/07 13:19:32 vulture Exp $
 */
package mpedit.net;

import java.io.*;
import java.net.*;
import com.vulture.util.CByteBuffer;

import javax.swing.JOptionPane;

import mpedit.exception.PacketInconsistencyException;

public class CThreadedSocket
    extends Thread
{
    private Socket m_socket;
    private CConnectionManager m_manager;
    private InputStream m_in;
    private OutputStream m_out;

    private CByteBuffer m_buffer;

    private int m_curLength = 0;
    private int m_curPosition = 0;

    public CThreadedSocket( CConnectionManager cm, Socket s )
        throws IOException
    {
        super( "ThreadedSocket" );
        System.err.println( "CThreadedSocket::CThreadedSocket()" );
        this.m_in = s.getInputStream();
        this.m_out = s.getOutputStream();
        this.m_manager = cm;

        this.start();
    }

    public CThreadedSocket( CConnectionManager cm, String hostname, int port )
        throws IOException
    {
        super( "ThreadedSocket" );
        System.err.println( "CThreadedSocket::CThreadedSocket()" );
        this.m_socket = new Socket( hostname, port );
        this.m_in = m_socket.getInputStream();
        this.m_out = m_socket.getOutputStream();
        this.m_manager = cm;

        this.start();
    }

    public void run()
    {
        System.err.println( "CThreadedSocket::run()" );
        try
        {
            while( true )
            {
                this.m_curLength = receivePacketSize();
                collectData();
            }
        }
        catch( Exception e )
        {
            m_manager.onSocketException(this, e);
        }
    }

    public void close()
    {
        try
        {
            m_socket.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * This function writes an bytearray to the socket.
     * First the length of the array will be sent, then the data
     * and last but not least a terminator (null)
     * @param data byte[]
     */
    public void writeData( byte[] data )
    {
        System.err.println( "CThreadedSocket::writeData()" );
        try
        {
            m_out.write( CProtokoll.intToByte( data.length ) ); //length of packet
            m_out.write( data ); //the packet itself
            m_out.write( ( byte )0 ); // terminator
        }
        catch( IOException e )
        {
            System.err.println( "Error while sending Data" );
            e.printStackTrace();
        }
    }

    /**
     * This function sends a Packet. The function automatically converts
     * The Packet to an ByteArray.
     * @deprecated
     * @param p CPacket
     */
    public void writeData( CPacket p )
    {
        System.err.println( "CThreadedSocket::writeData()" );
        byte[] data = CProtokoll.sendTranslate( p );
        try
        {
            m_out.write( CProtokoll.intToByte( data.length ) ); //length of packet
            m_out.write( data ); //the packet itself
            m_out.write( ( byte )0 ); // terminator
        }
        catch( IOException e )
        {
            JOptionPane.showMessageDialog( null, "Error while sending Data" );
            e.printStackTrace();
        }
    }

    /**
     * This method waits for 4 bytes, which will be the size of the next Packet,
     * which is then be collected by the collectData function
     * @throws IOException
     * @throws PacketInconsistencyException
     * @return int iSize
     * @throws IOException
     * @throws PacketInconsistencyException
     * @throws SocketException
     *
     */
    private int receivePacketSize()
        throws IOException, PacketInconsistencyException, SocketException
    {
        System.err.println( "CThreadedSocket::receivePacketSize()" );
        byte[] size = new byte[4];
        int n = 0;

        while( n < 4 )
        {
            size[n] = ( byte )m_in.read();
            ++n;
        }
        int iSize = CProtokoll.byteToInt( size );
        if( iSize > 65535 )
            throw new PacketInconsistencyException( "Packet Size is waaay too large" );
        return iSize;
    }

    /**
     * This method contains a loop, that gets n bytes of data. The datalength was
     * prior received bei receivePacketSize. If all data has been collected and the
     * Terminator was also in the correct place, then it can be sent to the ConnectionManagers
     * onData function.
     * @see #receivePacketSize
     * @see CConnectionManager
     * @throws IOException
     * @throws SocketException
     * @throws PacketInconsistencyException
     */
    private void collectData()
        throws IOException, SocketException, PacketInconsistencyException
    {
        System.err.println( "CThreadedSocket::collectData()" );
        m_curPosition = 0;
        byte by;

        //initialize an new ByteBuffer, that temporarly holds the received data
        this.m_buffer = new CByteBuffer();

        while( true ) //i like endless loops :)
        {
            by = ( byte )m_in.read();
            //check for last byte to receive. Has to be zero, if not there is an error
            if( m_curLength == m_curPosition )
            {
                //TODO: add exception handling
                if( by != 0 )
                    throw new PacketInconsistencyException( "Non-terminated Exception" );
                m_manager.onData( m_buffer.getBytes() );
                break; //stop data-collection
            }
            else
            {
                //append received data and increment currentPosition
                m_buffer.append( by );
                m_curPosition++;
            }
        }
    }
}
