package com.turtlemint.pdf_box;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * This will parse a PDF 1.5 object stream and extract all of the objects from the stream.
 */
public class PDFObjectStreamParser extends BaseParser
{
    private List<COSObject> streamObjects = null;
    private final int numberOfObjects;
    private final int firstObject;

    /**
     * Constructor.
     *
     * @param stream The stream to parse.
     * @param document The document for the current parsing.
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFObjectStreamParser(COSStream stream, COSDocument document) throws IOException
    {
        super(new InputStreamSource(stream.createInputStream()));
        this.document = document;
        // get mandatory number of objects
        numberOfObjects = stream.getInt(COSName.N);
        if (numberOfObjects == -1)
        {
            throw new IOException("/N entry missing in object stream");
        }
        if (numberOfObjects < 0)
        {
            throw new IOException("Illegal /N entry in object stream: " + numberOfObjects);
        }
        // get mandatory stream offset of the first object
        firstObject = stream.getInt(COSName.FIRST);
        if (firstObject == -1)
        {
            throw new IOException("/First entry missing in object stream");
        }
        if (firstObject < 0)
        {
            throw new IOException("Illegal /First entry in object stream: " + firstObject);
        }
    }

    /**
     * This will parse the tokens in the stream.  This will close the
     * stream when it is finished parsing.
     *
     * @throws IOException If there is an error while parsing the stream.
     */
    public void parse() throws IOException
    {
        try
        {
            Map<Integer, Long> offsets = readOffsets();
            streamObjects = new ArrayList<COSObject>(offsets.size());
            for (Entry<Integer, Long> offset : offsets.entrySet())
            {
                COSBase cosObject = parseObject(offset.getKey());
                COSObject object = new COSObject(cosObject);
                object.setGenerationNumber(0);
                object.setObjectNumber(offset.getValue());
                streamObjects.add(object);
                if (PDFBoxConfig.isDebugEnabled())
                {
                    Log.d("PdfBox-Android", "parsed=" + object);
                }
            }
        }
        finally
        {
            seqSource.close();
        }
    }

    /**
     * This will get the objects that were parsed from the stream.
     *
     * @return All of the objects in the stream.
     */
    public List<COSObject> getObjects()
    {
        return streamObjects;
    }

    private Map<Integer, Long> readOffsets() throws IOException
    {
        // according to the pdf spec the offsets shall be sorted ascending
        // but we can't rely on that, so that we have to sort the offsets
        // as the sequential parsers relies on it, see PDFBOX-4927
        Map<Integer, Long> objectNumbers = new TreeMap<Integer, Long>();
        long firstObjectPosition = seqSource.getPosition() + firstObject - 1;
        for (int i = 0; i < numberOfObjects; i++)
        {
            // don't read beyond the part of the stream reserved for the object numbers
            if (seqSource.getPosition() >= firstObjectPosition)
            {
                break;
            }
            long objectNumber = readObjectNumber();
            int offset = (int) readLong();
            objectNumbers.put(offset, objectNumber);
        }
        return objectNumbers;
    }

    private COSBase parseObject(int offset) throws IOException
    {
        long currentPosition = seqSource.getPosition();
        int finalPosition = firstObject + offset;
        if (finalPosition > 0 && currentPosition < finalPosition)
        {
            // jump to the offset of the object to be parsed
            seqSource.readFully(finalPosition - (int) currentPosition);
        }
        return parseDirObject();
    }

}
