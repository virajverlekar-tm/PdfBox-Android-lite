package com.turtlemint.pdf_box;

/**
 * Object representing the physical reference to an indirect pdf object.
 */
public class COSObjectKey implements Comparable<COSObjectKey>
{
    private final long number;
    private int generation;

    /**
     * Constructor.
     *
     * @param object The object that this key will represent.
     */
    public COSObjectKey(COSObject object)
    {
        this(object.getObjectNumber(), object.getGenerationNumber());
    }

    /**
     * Constructor.
     *
     * @param num The object number.
     * @param gen The object generation number.
     */
    public COSObjectKey(long num, int gen)
    {
        number = num;
        generation = gen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        COSObjectKey objToBeCompared = obj instanceof COSObjectKey ? (COSObjectKey)obj : null;
        return objToBeCompared != null &&
                objToBeCompared.getNumber() == getNumber() &&
                objToBeCompared.getGeneration() == getGeneration();
    }

    /**
     * This will get the object generation number.
     *
     * @return The object generation number.
     */
    public int getGeneration()
    {
        return generation;
    }

    /**
     * This will set the generation number. It is intended for fixes only.
     *
     * @param genNumber the new generation number.
     *
     * @deprecated will be removed in the next major release
     */
    public void fixGeneration(int genNumber)
    {
        generation = genNumber;
    }

    /**
     * This will get the object number.
     *
     * @return The object number.
     */
    public long getNumber()
    {
        return number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        // most likely generation is 0. Shift number 4 times (fast as multiply)
        // to support generation numbers up to 15
        return Long.valueOf((number << 4) + generation).hashCode();
    }

    @Override
    public String toString()
    {
        return number + " " + generation + " R";
    }

    @Override
    public int compareTo(COSObjectKey other)
    {
        if (getNumber() < other.getNumber())
        {
            return -1;
        }
        else if (getNumber() > other.getNumber())
        {
            return 1;
        }
        else
        {
            if (getGeneration() < other.getGeneration())
            {
                return -1;
            }
            else if (getGeneration() > other.getGeneration())
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }

}
