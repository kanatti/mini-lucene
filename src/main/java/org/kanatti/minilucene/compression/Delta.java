package org.kanatti.minilucene.compression;

import java.util.Arrays;

public class Delta {
    public static void main(String[] args) {
        int[] docIds = { 0, 1, 2, 5, 7, 8, 9 };
        int[] encoded = encode(docIds);
        int[] decoded = decode(encoded);
        System.out.println(Arrays.toString(docIds));
        System.out.println(Arrays.toString(encoded));
        System.out.println(Arrays.toString(decoded));
    }

    public static int[] encode(int[] input) {
        int[] deltas = new int[input.length];
        deltas[0] = input[0];
        for (int i = 1; i < input.length; i++) {
            deltas[i] = input[i] - input[i - 1];
        }
        return deltas;
    }

    public static int[] decode(int[] deltas) {
        int[] input = new int[deltas.length];
        input[0] = deltas[0];
        for (int i = 1; i < deltas.length; i++) {
            input[i] = input[i-1] + deltas[i];
        }
        return input;
    }
}
