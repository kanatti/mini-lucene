package org.kanatti.learning;

import org.apache.lucene.util.BytesRef;

/**
 * Learning BytesRef - the fundamental data type in Lucene
 */
public class BytesRefExample {
    
    public static void main(String[] args) {
        System.out.println("=== BytesRef Learning ===");
        
        // Create BytesRef from string
        BytesRef term1 = new BytesRef("hello");
        System.out.println("term1: " + term1.utf8ToString());
        System.out.println("term1 bytes: " + java.util.Arrays.toString(term1.bytes));
        System.out.println("term1 length: " + term1.length);
        
        // Create BytesRef from byte array
        byte[] bytes = "world".getBytes();
        BytesRef term2 = new BytesRef(bytes);
        System.out.println("term2: " + term2.utf8ToString());
        
        // Compare BytesRef objects
        BytesRef term3 = new BytesRef("hello");
        System.out.println("term1.equals(term3): " + term1.equals(term3));
        System.out.println("term1.compareTo(term2): " + term1.compareTo(term2));
    }
}
