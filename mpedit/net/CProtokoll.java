/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CProtokoll.java,v 1.2 2004/10/05 12:54:05 vulture Exp $
 */
package mpedit.net;

import mpedit.exception.PacketInconsistencyException;

/**
 * This class defines Protokoll actions. It Translates a CPacket in an bytearray, which then can
 * be send by the socket. It also retranslates an bytearray back to a CPacket for easier usage.
 * This class also defines constants for use in a CPacket as m_action.
 * It also has functions to convert an int to an byte array with 4 fields and vice versa.
 * @author Alexander Zielke
 */
public class CProtokoll
{
    /**
     * This field indicates that that a client wants to SYNC or that the server requests
     * the client to resync.
     */
    public static final byte SYNC = 1;

    /**
     * This field indicates, that a Packet contains data of an sync.
     */
    public static final byte SYNCDATA = 2;

    /**
     * If the server receives this signal, it tries to authenticate the client via a password,
     * sent in the same packet.
     */
    public static final byte REQUEST = 3;

    /**
     * This field indicates, that the server has accepted the connection.
     */
    public static final byte ACCEPT = 4;

    /**
     * This indicates, that a client disconnected from the server.
     */
    public static final byte DISCONNECT = 5;

    /**
     * The server was shut down, if a client receives this signal.
     */
    public static final byte EOF = 6;

    /**
     * If a client wants to request a new ClientList, then it sends a packet with this action.
     */
    public static final byte GETCLIENTLIST = 7;

    /**
     * This field indicates, that the Packet contains a Client List
     */
    public static final byte CLIENTLISTDATA = 8;

    /**
     * This action is sent, when a new client was accepted. It is send from the server to all already
     * connected clients, so they can update their client lists.
     */
    public static final byte NEWCLIENT = 9;

    /**
     * This field indicates, that text has been inserted.
     */
    public static final byte ADDTEXT = 10;

    /**
     * This indicates, that Text has been removed from the Document
     */
    public static final byte REMOVETEXT = 11;

    /**
     * If the server does not accept the connection, then it sends this packet to the client.
     * The reason is in m_data as a String
     */
    public static final byte UNAUTHORIZED = 12;

    /**
     * This is the first packet. The server sends it to the client with the Client ID
     */
    public static final byte ASSIGNID = 13;

    /**
     * This function translates a CPacket to an byte array.
     * @param action CPacket
     * @return byte[]
     */
    public static byte[] sendTranslate( CPacket action )
    {
        System.err.println( "CProtokoll::sendTranslate()" );
        byte byAction = action.m_action;
        byte[] byClientInt = intToByte( action.m_clientID );
        byte[] byStartInt = intToByte( action.m_startpos );
        byte[] byStopInt = intToByte( action.m_endpos );
        //TODO: use CharsetEncoder for conversion to bytes, because of platform-independency
        if( action.m_data == null )
            action.m_data = new String();
        byte[] byString = action.m_data.getBytes();

        int length = 1 + 4 + 4 + 4 + byString.length;

        byte[] by = new byte[length];

        by[0] = byAction;

        int n = 0;
        while( n < 4 )
        {by[1 + n] = byClientInt[n];
        ++n;
        }

        n = 0;
        while( n < 4 )
        {by[5 + n] = byStartInt[n];
        ++n;
        }

        n = 0;
        while( n < 4 )
        {by[9 + n] = byStopInt[n];
        ++n;
        }

        n = 0;
        while( n < byString.length )
        {by[13 + n] = byString[n];
        ++n;
        }

        return by;
    }

    /**
     * This function translates an bytearray back to a CPacket
     * @param data byte[]
     * @throws PacketInconsistencyException
     * @return CPacket
     */
    public static CPacket receiveTranslate( byte[] data )
        throws PacketInconsistencyException
    {
        System.err.println( "CProtokoll::receiveTranslate()" );
        if( data.length < 13 )
            throw new PacketInconsistencyException( "Packetsize was too small" );

        CPacket action = new CPacket();

        action.m_action = data[0];
        byte[] int_id = new byte[4];
        byte[] int_start = new byte[4];
        byte[] int_stop = new byte[4];

        int n = 0;
        while( n < 4 )
        {int_id[n] = data[1 + n];
        ++n;
        }
        action.m_clientID = byteToInt( int_id );

        n = 0;
        while( n < 4 )
        {int_start[n] = data[5 + n];
        ++n;
        }
        action.m_startpos = byteToInt( int_start );

        n = 0;
        while( n < 4 )
        {int_stop[n] = data[9 + n];
        ++n;
        }
        action.m_endpos = byteToInt( int_stop );

        StringBuffer buf = new StringBuffer();
        n = 0;
        try
        {
            while( true )
            {
                buf.append( ( char )data[13 + n] );
                ++n;
            }
        }
        catch( ArrayIndexOutOfBoundsException e )
        {;
        } //Do nothing, we just waited for it to happen!

        action.m_data = new String( buf );

        return action;
    }

    /**
     * This function converts an int to an bytearray with 4 fields
     * @param i int
     * @return byte[]
     */
    public static byte[] intToByte( int i )
    {
        byte[] by = new byte[4];

        int n = 0;
        while( n < 4 )
        {
            by[n] = ( byte ) ( ( i >> ( ( 3 - n ) * 8 ) ) & 0xFF );
            ++n;
        }

        return by;
    }

    /**
     * This function converts an bytearray with 4 fields to an int
     * @param by byte[]
     * @throws NumberFormatException
     * @return int
     */
    public static int byteToInt( byte[] by )
        throws NumberFormatException
    {
        if( by.length != 4 )
            throw new NumberFormatException( "Cannot convert byte to int" );
        int i = 0, n = 0;

        while( n < 4 )
        {
            i = ( i << 8 ) | ( ( ( int )by[n] ) & 0xFF );
            ++n;
        }
        return i;
    }
}
