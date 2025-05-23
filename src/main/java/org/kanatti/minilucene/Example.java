package org.kanatti.minilucene;

import org.kanatti.minilucene.search.ArrayBitSet;
import org.kanatti.minilucene.search.ArrayDISI;
import org.kanatti.minilucene.search.BitSet;
import org.kanatti.minilucene.search.BitSetDISI;
import org.kanatti.minilucene.search.DocIdSetIterator;

public class Example {
    public static void main(String[] args) {
        DocIdSetIterator disi = new ArrayDISI(new int[] { 10, 11, 12, 14, 15 });

        System.out.println("docId: " + disi.docId());
        System.out.println("docId: " + disi.advance(12));
        System.out.println("docId: " + disi.nextDoc());
        System.out.println("docId: " + disi.nextDoc());
        System.out.println("docId: " + disi.nextDoc());
        System.out.println("docId: " + disi.nextDoc());

        BitSet b = new ArrayBitSet(10);
        b.set(3);
        b.set(5);
        b.set(6);
        disi = new BitSetDISI(b);

        System.out.println("docId: " + disi.docId());
        System.out.println("docId: " + disi.advance(5));
        System.out.println("docId: " + disi.nextDoc());
        System.out.println("docId: " + disi.nextDoc());
        System.out.println("docId: " + disi.nextDoc());
        System.out.println("docId: " + disi.nextDoc());
    }
}

