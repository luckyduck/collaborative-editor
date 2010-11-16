/*
 * Copyright (C) 2004 Alexander Zielke
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * $Id: CByteBuffer.java,v 1.1.1.1 2004/10/04 18:45:34 vulture Exp $
 */


package com.vulture.util;


/**
 * This class is a simple implementation of an ByteBuffer. ITs pretty much like the StringBuffer
 * except that it holds bytes.
 * @author not attributable
 * @version 1.0
 */
public class CByteBuffer
{
    private byte []data;
    private int size;
    private int capacity;

    /**
     * The standard constructor creates a new byte buffer with the default size of 16
     */
    public CByteBuffer()
    {
        this(16);
    }

    /**
     * This constructor creates a new byte buffer with the specified capacity
     * @param capacity int
     */
    public CByteBuffer(int capacity)
    {
        data = new byte[capacity];
        size = 0;
        this.capacity = capacity;
    }

    /**
     * With this function one can append a single byte to the buffer
     * @param arg0 byte
     */
    public void append(byte arg0)
    {
        if( size + 1 > capacity )
            grow(4);
        data[size] = arg0;
        size++;
    }

    /**
     * With this function one can append an array of bytes to the array
     * @param arg0 byte[]
     */
    public void append(byte []arg0)
    {
        if( size + arg0.length > capacity )
            grow(arg0.length);
        for(int i = 0; i < arg0.length; ++i)
        {
            data[size] = arg0[i];
            ++size;
        }
    }

    /**
     * This function is used to grow the array by an specified amount
     * @param amount int
     */
    private void grow(int amount)
    {
        capacity = capacity+amount;
        byte []newarray = new byte[capacity];
        for( int i = 0; i < size; ++i)
        {
            newarray[i] = data[i];
        }
        data = newarray;
    }

    /**
     * This function returns all data of the buffer
     * @return byte[]
     */
    public byte[] getBytes()
    {
        byte by[] = new byte[size];
        for(int i = 0; i < size; ++i)
        {
            by[i] = data[i];
        }
        return by;
    }

}
