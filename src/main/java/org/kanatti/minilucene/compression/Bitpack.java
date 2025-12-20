package org.kanatti.minilucene.compression;

import java.util.Arrays;

public class Bitpack {
    public static void main(String[] args) {
        // Example 1: Pack 3-bit values
        int[] values1 = {5, 3, 6, 7, 2, 1};
        byte[] packed1 = pack(values1);
        int bitsPerValue1 = bitsNeeded(max(values1));
        int[] unpacked1 = unpack(packed1, values1.length, bitsPerValue1);

        System.out.println("Example 1:");
        System.out.println("Original:  " + Arrays.toString(values1));
        System.out.println("Unpacked:  " + Arrays.toString(unpacked1));
        System.out.println("Match: " + Arrays.equals(values1, unpacked1));
        System.out.println();

        // Example 2: Pack 10-bit values
        int[] values2 = {1023, 512, 256, 127, 0};
        byte[] packed2 = pack(values2);
        int bitsPerValue2 = bitsNeeded(max(values2));
        int[] unpacked2 = unpack(packed2, values2.length, bitsPerValue2);

        System.out.println("Example 2:");
        System.out.println("Original:  " + Arrays.toString(values2));
        System.out.println("Unpacked:  " + Arrays.toString(unpacked2));
        System.out.println("Match: " + Arrays.equals(values2, unpacked2));
    }

    public static byte[] pack(int[] values) {
        int bitsPerValue = bitsNeeded(max(values));

        // Find how many bytes needed in total
        // This is faster than Math.ceil, check CeilDivisionBenchmark.
        int bytesLength = (bitsPerValue * values.length + 7) / 8;

        byte[] packed = new byte[bytesLength];

        int bitPos = 0;

        for (int value : values) {
            int bitsToWrite = bitsPerValue;

            while (bitsToWrite > 0) {
                int byteIndex = bitPos / 8;
                int bitOffset = bitPos % 8;
                int bitsAvailable = 8 - bitOffset;           // Space left in this byte
                int bitsThisRound = Math.min(bitsAvailable, bitsToWrite);

                // Extract the top bitsThisRound bits from value
                int shift = bitsToWrite - bitsThisRound;
                int mask = (1 << bitsThisRound) - 1;
                int chunk = (value >>> shift) & mask;

                // Pack into byte
                packed[byteIndex] |= (chunk << (bitsAvailable - bitsThisRound));

                bitPos += bitsThisRound;
                bitsToWrite -= bitsThisRound;
            }
        }

        return packed;
    }

    public static int[] unpack(byte[] packed, int numValues, int bitsPerValue) {
        int[] values = new int[numValues];
        int bitPos = 0;

        for (int i = 0; i < numValues; i++) {
            int value = 0;
            int bitsToRead = bitsPerValue;

            while (bitsToRead > 0) {
                int byteIndex = bitPos / 8;
                int bitOffset = bitPos % 8;
                int bitsAvailable = 8 - bitOffset;           // Bits left in this byte
                int bitsThisRound = Math.min(bitsAvailable, bitsToRead);

                // Extract bits from byte
                int shift = bitsAvailable - bitsThisRound;
                int mask = (1 << bitsThisRound) - 1;
                int chunk = (packed[byteIndex] >>> shift) & mask;

                // Add to value (shift existing bits left, add new chunk)
                value = (value << bitsThisRound) | chunk;

                bitPos += bitsThisRound;
                bitsToRead -= bitsThisRound;
            }

            values[i] = value;
        }

        return values;
    }

    private static int max(int[] ints) {
        int max = ints[0];
        for (int i = 0; i < ints.length; i++) {
            if (ints[i] > max) {
                max = ints[i];
            } 
        }
        return max;
    }

    private static int bitsNeeded(int n) {
        return n == 0 ? 1 : 32 - Integer.numberOfLeadingZeros(n);
    }
}
