/*
 * Copyright (C) 2004 Alexander Zielke, Jan Brinkmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CServerManager.java,v 1.6 2004/10/07 21:33:12 vulture Exp $
 */
package mpedit.net;

import java.util.LinkedList;

import javax.swing.JOptionPane;

import mpedit.exception.PacketInconsistencyException;
import mpedit.gui.CMainFrame;

/**
 * @author Alexander Zielke, Jan Brinkmann
 * @see CConnectionManager
 */
public class CServerManager extends CConnectionManager
{
	private LinkedList m_clients;
	private int m_nextID = 1;
	private CListener m_listener;
	private String m_password;
	private int m_maxConnections = 8;

	public CServerManager(CMainFrame parent, int port, String password, String nickname)
	{
		super(parent);
		System.err.println("CServerManager::CServerManager()");
		//start the ServerSocket, which waits for connecting clients
		m_listener = new CListener(this, port);
		m_clients = new LinkedList();
		m_password = password;
		m_mainframe.getUserlist().addClient(nickname);

		CClient c = new CClient();
		c.m_id = 0;
		c.m_nickname = nickname;
		c.m_socket = null;
		m_clients.add(c);
	}

	/**
	 * This Method is called every time, when the CThreadedSocket receives Data
	 * The data is then translated via CProtokoll to an CPacket object
	 * which will then be processed and dispatched to the appropriated on-Function.
	 * This function will then do whatever is needded.
	 * @see CProtokoll
	 * @param data byte[]
	 */
	synchronized public void onData(byte[] data)
	{
		System.err.println("CServerManager::onData()");
		CPacket p = null;
		try
		{
			p = CProtokoll.receiveTranslate(data);

		} catch (PacketInconsistencyException e)
		{
			System.err.println("Recieved Inconsistent Packet");
			e.printStackTrace();
			return;
		}

		//if( p.m_clientID == 0 )
		//	JOptionPane.showMessageDialog(null, "Packet from: "+ p.m_clientID );

		if (p.m_action == CProtokoll.ADDTEXT)
			onInsert(p);
		else if (p.m_action == CProtokoll.REMOVETEXT)
			onRemove(p);
		else if (p.m_action == CProtokoll.REQUEST)
			onClientRequest(p);
		else if (p.m_action == CProtokoll.SYNC)
			onClientSync(p);
		else if (p.m_action == CProtokoll.GETCLIENTLIST)
			onClientRequestList(p);
		else if (p.m_action == CProtokoll.DISCONNECT)
			onClientDisconnect(p);
		else
			System.out.println("Unimplemented action in packet!");
	}

	/**
	 * This function receives a packet, which is Design for inserting Text
	 * into the document. The ServerManager class also distributes the Packet
	 * to all other clients except the one, who sent it.
	 * @param p CPacket
	 */
	public void onInsert(CPacket p)
	{
		//super.onRemove( p ); // do not use it anymore, because of excaption handling from insertString
		System.err.println("CServerManager::onInsert()");
		if (!m_mainframe.insertText(p.m_startpos, p.m_data))
		{
			CPacket q = new CPacket();
			q.m_action = CProtokoll.SYNC;
			getSocketByID(p.m_clientID).writeData(CProtokoll.sendTranslate(q));
		} else
		{
			//broadcast package to everyone except the sender
			sendPacketToAllExceptID(p, p.m_clientID);
		}

	}

	/**
	 * This functioon is designed for removing Text within the Document.
	 * It also broadcasts it to all users except the one who sent the packet.
	 * @param p CPacket
	 */
	public void onRemove(CPacket p)
	{
		//super.onRemove( p ); // do not use it anymore, because of excaption handling from insertString
		System.err.println("CServerManager::onRemove()");
		if (!m_mainframe.removeText(p.m_startpos, p.m_endpos))
		{
			CPacket q = new CPacket();
			q.m_action = CProtokoll.SYNC;
			getSocketByID(p.m_clientID).writeData(CProtokoll.sendTranslate(q));
		} else
		{
			//broadcast package to everyone except the sender
			sendPacketToAllExceptID(p, p.m_clientID);
		}
	}

	/**
	 * This is called, whenever Text has to be removed.
	 * The ServerManager class distributes the Information to all Clients
	 * @param offset int
	 * @param length int
	 */
	public void sendRemove(int offset, int length)
	{
		//Broadcast an remove to all clients
		CPacket p = new CPacket();
		p.m_action = CProtokoll.REMOVETEXT;
		p.m_startpos = offset;
		p.m_endpos = length;
		sendPacketToAll(p);
	}

	/**
	 * This function is called every time, when Text is inserted in the document.
	 * It receives the offset and the String with the new data itself.
	 * The ServerManager also broadcasts the Packet to all clients
	 * @param offset int
	 * @param data String
	 */
	public void sendInsert(int offset, String data)
	{
		//Broadcast an insert to all clients
		CPacket p = new CPacket();
		p.m_action = CProtokoll.ADDTEXT;
		p.m_startpos = offset;
		p.m_data = data;
		sendPacketToAll(p);
	}

	/**
	 * Every time, when the CListener accepts a new Client, it calls this function.
	 * If the server is already full, it will send CProtokoll.UNAUTHORIZED to the client
	 * with the reason "Server full" in m_data.
	 * If there is space left, then the client gets an ID and is added to the List
	 * of clients. Passwordcheck is later in the onClientRequest() function
	 * @see CListener
	 * @param s CThreadedSocket
	 */
	public void onListenerAcceptedClient(CThreadedSocket s)
	{
		//the ServerSocket in CListener has accepted a new client
		System.err.println("CServerManager::onListenerAcceptedClient()");
		if (m_clients.size() >= m_maxConnections)
		{
			CPacket p = new CPacket();
			p.m_action = CProtokoll.UNAUTHORIZED;
			p.m_data = new String("Server full");
		} else
		{
			CClient c = new CClient();
			c.m_id = this.m_nextID++;
			c.m_socket = s;

			CPacket p = new CPacket();
			p.m_action = CProtokoll.ASSIGNID;
			p.m_startpos = c.m_id;

			byte[] by = CProtokoll.sendTranslate(p);
			s.writeData(by);
			//add the client to the list of all clients
			m_clients.add(c);
		}
	}

	/**
	 * This method is called by the MainFrame, if the Sync menuitem has been selected.
	 * The server sents SYNC to all clients.
	 * @see CProtokoll
	 */
	public void syncData()
	{
		if (JOptionPane
			.showConfirmDialog(
				null,
				"SYNC all clients?",
				"Sync all?",
				JOptionPane.YES_NO_OPTION)
			== JOptionPane.OK_OPTION)
		{
			CPacket p = new CPacket();
			p.m_action = CProtokoll.SYNC;
			sendPacketToAll(p);
		}
	}

	/**
	 * When the client sents CProtokoll.REQUEST, the onData() function will call
	 * this function to handle the request.
	 * This function will check the password, which is sent in CPacket.m_data.
	 * If the Password is corret, the Server will send CProtokoll.ACCEPT,
	 * else it will send CProtokoll.UNAUTHORIZED with reason "Incorrect Password"
	 * in CPacket.m_data. This function also receives the Username of the
	 * connecting client.
	 * @see CPacket
	 * @see CProtokoll
	 * @param p CPacket
	 */
	private void onClientRequest(CPacket p)
	{
		System.err.println("CServerManager::onClientRequest()");
		CPacket sendPacket = new CPacket();
		CThreadedSocket s = getSocketByID(p.m_clientID);

		String[] tokens = p.m_data.split("\0");

		String nickname = tokens[0], password; // the first data is the nickname
		try // to get password from string. maybe there is no password, then we just use a new empty string
			{
			password = tokens[1];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			password = new String();
		}

		if (!password.equals(m_password)
			|| p.m_clientID < 1) // check for password and correct id
		{
			sendPacket.m_action = CProtokoll.UNAUTHORIZED;
			sendPacket.m_data = new String("Incorrect Password");
		} else if (!checkNickname(nickname)) // check for double nickname
		{
			sendPacket.m_action = CProtokoll.UNAUTHORIZED;
			sendPacket.m_data = new String("Nickname already in use");
		} else // everything fine, register client and set as authed and notify all other clients
			{
			sendPacket.m_action = CProtokoll.ACCEPT;
			getClientByID(p.m_clientID).m_authed = true;
			//well, its not really implemented :/

			getClientByID(p.m_clientID).m_nickname = nickname;
			m_mainframe.getUserlist().addClient(nickname);

			CPacket q = new CPacket(); //notify all already connected clients
			q.m_action = CProtokoll.NEWCLIENT;
			q.m_data = nickname;
			sendPacketToAllExceptID(q, p.m_clientID);

		}

		//TODO: terminate socket if UNAUTHORIZED!
		// why the fuck is this socket terminated?
		s.writeData(CProtokoll.sendTranslate(sendPacket));
	}

	/**
	 * This function is called, when a Client sends DISCONNECT.
	 * It _should_ remove the Client from the list. Well, i think it does not remove
	 * it, bracause there are Sending errors, which should not be there
	 * @param p CPacket
	 */
	private void onClientDisconnect(CPacket p)
	{
		CClient c = getClientByID(p.m_clientID);
		CPacket q = new CPacket();
		q.m_action = CProtokoll.DISCONNECT;
		q.m_data = c.m_nickname;
		sendPacketToAllExceptID(q, p.m_clientID);
		//c.m_socket.close();
		m_clients.remove(c);
		m_mainframe.getUserlist().removeClient(c.m_nickname);
	}

	/**
	 * This function handles request from the clients to get a new list
	 * The Client list is sent in one packet, sperated by '\0'
	 * @param p CPacket
	 */
	private void onClientRequestList(CPacket p)
	{
		StringBuffer buf = new StringBuffer();
		synchronized (m_clients)
		{
			//TODO: get the server nickname from m_nickname. remove it from the m_clients list
			int size = m_clients.size(), pos = 0;
			while (pos < size)
			{
				buf.append(((CClient) m_clients.get(pos)).m_nickname);
				buf.append('\0');
				++pos;
			}
		}
		CPacket q = new CPacket();
		q.m_action = CProtokoll.CLIENTLISTDATA;
		q.m_data = new String(buf);
		getSocketByID(p.m_clientID).writeData(CProtokoll.sendTranslate(q));
	}

	/**
	 * This function is called, when a Client requests a SYNC.
	 * It retrieves the current Text, splits it in CPacket with an m_data length
	 * of 128 and then sends them one after the other to the Client.
	 * @param p CPacket
	 */
	private void onClientSync(CPacket p)
	{
		String text = m_mainframe.getM_text().getText();
		int offset = 0, length = text.length();
		CPacket q = new CPacket();
		while (offset < length)
		{
			q.m_action = CProtokoll.SYNCDATA;
			try
			{
				q.m_startpos = 1;
				q.m_data = text.substring(offset, offset + 128);
			} catch (StringIndexOutOfBoundsException e)
			{
				q.m_startpos = 0;
				q.m_data = text.substring(offset);
			}
			offset += 128;
			getSocketByID(p.m_clientID).writeData(CProtokoll.sendTranslate(q));
		}
	}

	/**
	 * Call this method when you want to destroy the ServerManager.
	 * It sends EOF to all clients, so they know that the server has been killed.
	 * Then it trys to close all sockets.
	 */
	public void destroy()
	{
		System.err.println("CServerManager::destroy()");
		CPacket p = new CPacket();
		p.m_action = CProtokoll.EOF;
		sendPacketToAll(p);

		m_listener.close();
		int n = 0, size = m_clients.size();
		CClient c;
		while (n < size)
		{
			c = ((CClient) m_clients.get(n));
			if (c.m_id != 0)
				c.m_socket.close();
			++n;
		}
	}

	/**
	 * If an Exception within the CThreadedSocket occuers, it tells the ConnectionManager about
	 * it, so it can get shut down properly.
	 * @param s CThreadedSocket
	 * @param e Exception
	 */
	public void onSocketException(CThreadedSocket s, Exception e)
	{
		int n = 0, size = m_clients.size();
		CClient c;
		while (n < size)
		{
			c = (CClient) m_clients.get(n);
			if (c.m_socket == s)
			{
				CPacket q = new CPacket();
				q.m_action = CProtokoll.DISCONNECT;
				q.m_data = c.m_nickname;
				sendPacketToAllExceptID(q, q.m_clientID);
				m_clients.remove(n);
				break;
			}
			++n;
		}
	}

	/**
	 * This method will send the CPacket p to all clients in the Clientlist
	 * m_clients
	 * @param p CPacket
	 */
	private void sendPacketToAll(CPacket p)
	{
		System.err.println("CServerManager::sendPacketToAll()");
		//This method goes through all elements in m_clients and send them the packet
		byte[] by = CProtokoll.sendTranslate(p);

		int size = m_clients.size();
		int pos = 0;
		CClient c;

		while (pos < size)
		{
			c = (CClient) m_clients.get(pos);
			//TODO: remove when implemented server nickname not in m_clients
			if (c.m_id != 0)
				c.m_socket.writeData(by);
			++pos;
		}
	}

	/**
	 * This function will send the packet p to all clients, except then one
	 * with the id 'id'
	 * @param p CPacket
	 * @param id int
	 */
	private void sendPacketToAllExceptID(CPacket p, int id)
	{
		System.err.println("CServerManager::sendPacketToAllExceptID()");
		byte[] by = CProtokoll.sendTranslate(p);
		int curid, pos = 0, size = m_clients.size();
		while (pos < size)
		{
			curid = ((CClient) m_clients.get(pos)).m_id;
			//TODO: remove when implemented server nickname not in m_clients
			if (curid != id && curid != 0)
				 (((CClient) m_clients.get(pos)).m_socket).writeData(by);
			++pos;
		}

	}

	/**
	 * This function returns the CThreadedSocket, which belongs to the
	 * client with the ID 'id'. It returns null if there was no suck id
	 * or if id == 0
	 * @param id int
	 * @return CThreadedSocket
	 */
	private CThreadedSocket getSocketByID(int id)
	{
		System.err.println("CServerManager::getSocketByID()");
		return getClientByID(id).m_socket;
	}

	/**
	 * this function iterates through the LinkedList m_clients to find
	 * the requested CClient object with the proper clientID.
	 * @param id int
	 * @return CClient
	 */
	synchronized private CClient getClientByID(int id)
	{
		System.err.println("CServerManager::getClientByID()");
		if (id == 0)
			return null;
		int n = 0, size = m_clients.size();
		while (n < size)
		{
			if (((CClient) m_clients.get(n)).m_id == id)
				break;
			++n;
		}
		if (((CClient) m_clients.getLast()).m_id != id)
			return null;
		return (CClient) m_clients.get(n);
	}

	/**
	 * This method is used to check all nicknames. It prevents a client from using the same
	 * nickname as an already connected client.
	 * @param nickname String
	 * @return boolean
	 */
	synchronized private boolean checkNickname(String nickname)
	{
		if (nickname.equals(""))
			return false;
		int n = 0, size = m_clients.size();
		while (n < size)
		{
			try
			{
				if (((CClient) m_clients.get(n))
					.m_nickname
					.equalsIgnoreCase(nickname))
					return false;
			} catch (NullPointerException e)
			{
				//well...at least one nick might not be in the client list. So there
				//will be _at least one_ NullPointerException. Nothing to worry about :)
			}
			++n;
		}
		return true;
	}

	/*public void onClosedSocket(CThreadedSocket s)
	  {
	   int size = m_clients.size(), pos = 0;
	   while( pos < size )
	   {
	 if( ((CClient)m_clients.get(pos)).m_socket.toString().equals(s) )
	 {
	  on ((CClient)m_clients.get(pos)).m_id
	 break;
	 }
	 ++pos;
	   }
	 }*/
}
