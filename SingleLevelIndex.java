/******************************************************************************
 * Java program to create a single level index.
 *
 * This program is written as the submission of home work.
 *
 * The purpose of this program is to create a single level index and understand
 * how these things work at the lower level.
 *
 * It takes 4 command line arguments:
 *
 * 1. -c or -l:
 *      -c = create an index
 *      -l = list the records in order of the indexes
 *
 * 2. Name of the data file:
 *      This is supposed to be a text file.
 *
 * 3. Name of the index file:
 *      This is supposed to be a binary file.
 *
 * 4. A number between 1 and 24 (both inclusive):
 *      This is the length of the key.
 *
 * Written by Nirbhay Sibal (nxs180002) at The University of Texas at Dallas
 * starting November 2, 2018.
 ******************************************************************************/

package com.sibalnirbhay;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Index class:
 *  Created to bind key and pointer (offset) as a single entity.
 *
 * An object of this class needs to be called by passing the
 * following parameters :
 *  key
 *  pointer
 *
 * KeyComparator is used to compare the keys and sort the array
 * of type Index based on ascending order of keys.
 **/
class Index {
    String key;
    long pointer;

    Index(String key, long pointer) {
        this.key = key;
        this.pointer = pointer;
    }

    public static Comparator<Index> KeyComparator = new Comparator<Index>() {
        @Override
        public int compare(Index o1, Index o2) {
            return o1.key.compareTo(o2.key);
        }
    };

}

public class SingleLevelIndex {

    public static void main(String[] args) throws IOException{

        /**
         * If the number of input parameters are incorrect, the following message will be
         * displayed, and the program will terminate.
         **/
        if (args.length!=4) {
            System.out.println("Inappropriate count of command line parameters.");
            System.exit(-1);
        }

        /**
         * If the first parameter is -c and -l, the following message will be displayed, and
         * the program will terminate.
         **/
        if (!args[0].equals("-c") && !args[0].equals("-l")) {
            System.out.println("The first parameter should either be '-c' or '-l'.");
            System.exit(-1);
        }

        String dataFile = args[1];
        String indexFile = args[2];
        int keyLength = Integer.parseInt(args[3]);

        /**
         * The key length entered by the user should be between 1 and 24.
         **/
        if (keyLength<1 || keyLength>24) {
            System.out.println("1 <= Key Length <= 24");
            System.exit(-1);
        }

        if (args[0].equals("-c")) {
            createIndex(dataFile, indexFile, keyLength);
        } else if (args[0].equals("-l")) {
            listIndex(dataFile, indexFile, keyLength);
        }

    }

    /**
     * If the first command line argument is -c, createIndex method will execute.
     *
     * @param dataFile:
     *                Name of the data file
     *
     * @param indexFile:
     *                 Name of the index file. This file will be a binary file.
     *
     * @param keyLength:
     *                 Length of the key entered by the user.
     **/
    public static void createIndex(String dataFile, String indexFile, int keyLength) throws IOException {
        RandomAccessFile randIFile = new RandomAccessFile(dataFile, "r");
        RandomAccessFile randOFile = new RandomAccessFile(indexFile,"rw");

        /**
         * The following piece of code counts the number of records in the data file.
         **/
        randIFile.seek(0);
        int recordCount = 0;
        while (randIFile.readLine()!=null) {
            recordCount++;
        }

        /**
         * An array of Index type is created. It's size is equal to the size of the records
         * in the data file.
         **/
        Index[] idxArr = new Index[recordCount];

        /**
         * The following piece of code iterates through the data file to create the keys.
         **/
        randIFile.seek(0);
        String key = (String) randIFile.readLine().substring(0,keyLength);
        long pointer = randIFile.getFilePointer();

        idxArr[0] = new Index(key, pointer);

        for (int i = 1; i<idxArr.length;i++) {
            pointer = randIFile.getFilePointer();
            Object reader = randIFile.readLine();
            key = ((String) reader).substring(0,keyLength);
            idxArr[i] = new Index(key, pointer);
        }

        /**
         * Sorting the array of Indexes in ascending of the keys.
         **/
        Arrays.sort(idxArr, Index.KeyComparator);

        /**
         * Writing the keys and their offsets to the index file.
         **/
        for (int i=0; i<idxArr.length; i++) {
            randOFile.writeBytes(idxArr[i].key);
            randOFile.writeLong(idxArr[i].pointer);
        }

        System.out.println("Index File "+indexFile+" created!");

        /**
         * Closing all the files
         **/
        randIFile.close();
        randOFile.close();
    }


    /**
     * If the first command line argument is -l, listIndex method will execute.
     *
     * @param dataFile:
     *                Name of the data file
     *
     * @param indexFile:
     *                 Name of the index file. This file is a binary file.
     *
     * @param keyLength:
     *                 Length of the key entered by the user.
     **/
    public static void listIndex(String dataFile, String indexFile, int keyLength) throws IOException {
        RandomAccessFile dFile = new RandomAccessFile(dataFile, "rw");
        RandomAccessFile idxFile = new RandomAccessFile(indexFile,"r");

        long pos;

        /**
         * The following piece of code counts the number of records in the data file.
         **/
        dFile.seek(0);
        int recordCount = 0;
        while (dFile.readLine()!=null) {
            recordCount++;
        }

        idxFile.seek(0);
        dFile.seek(0);

        /**
         * The following piece of code iterates through the index file. The indexes stored in this
         * file are sorted. So, we take offset from each line and print the line starting from that
         * offset from the data file.
         **/
        for (int i=0;i<recordCount;i++) {
            byte[] key = new byte[keyLength];
            idxFile.read(key);
            pos = idxFile.readLong();
            dFile.seek(pos+keyLength);
            System.out.println(new String(key)+dFile.readLine());
        }

        /**
         * Closing all the files
         **/
        dFile.close();
        idxFile.close();
    }
}
