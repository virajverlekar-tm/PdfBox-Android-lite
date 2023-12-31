package com.turtlemint.pdf_box;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * A SequentialSource backed by an InputStream.
 */
final class InputStreamSource implements SequentialSource
{
    private final PushbackInputStream input;
    private int position;

    /**
     * Constructor.
     *
     * @param input The input stream to wrap.
     */
    InputStreamSource(InputStream input)
    {
        this.input = new PushbackInputStream(input, 32767); // maximum length of a PDF string
        this.position = 0;
    }

    @Override
    public int read() throws IOException
    {
        int b = input.read();
        position++;
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        int n = input.read(b);
        if (n > 0)
        {
            position += n;
            return n;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        int n = input.read(b, offset, length);
        if (n > 0)
        {
            position += n;
            return n;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public long getPosition() throws IOException
    {
        return position;
    }

    @Override
    public int peek() throws IOException
    {
        int b = input.read();
        if (b != -1)
        {
            input.unread(b);
        }
        return b;
    }

    @Override
    public void unread(int b) throws IOException
    {
        input.unread(b);
        position--;
    }

    @Override
    public void unread(byte[] bytes) throws IOException
    {
        input.unread(bytes);
        position -= bytes.length;
    }

    @Override
    public void unread(byte[] bytes, int start, int len) throws IOException
    {
        input.unread(bytes, start, len);
        position -= len;
    }

    @Override
    public byte[] readFully(int length) throws IOException
    {
        byte[] bytes = new byte[length];
        int bytesRead = 0;
        do
        {
            int count = read(bytes, bytesRead, length - bytesRead);
            if (count < 0)
            {
                throw new EOFException();
            }
            bytesRead += count;
        } while (bytesRead < length);
        return bytes;
    }

    @Override
    public boolean isEOF() throws IOException
    {
        return peek() == -1;
    }

    @Override
    public void close() throws IOException
    {
        input.close();
    }
}
