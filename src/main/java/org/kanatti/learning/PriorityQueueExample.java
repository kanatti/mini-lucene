package org.kanatti.learning;

import org.apache.lucene.util.PriorityQueue;

/**
 * Learning PriorityQueue - Lucene's optimized heap for search operations
 */
public class PriorityQueueExample {
    
    // Simple document class for demonstration
    static class Document {
        int id;
        float score;
        String title;
        
        Document(int id, float score, String title) {
            this.id = id;
            this.score = score;
            this.title = title;
        }
        
        @Override
        public String toString() {
            return String.format("Doc{id=%d, score=%.2f, title='%s'}", id, score, title);
        }
    }
    
    // Custom PriorityQueue for Documents (min-heap by score)
    static class DocumentPriorityQueue extends PriorityQueue<Document> {
        
        public DocumentPriorityQueue(int maxSize) {
            super(maxSize);
        }
        
        @Override
        protected boolean lessThan(Document a, Document b) {
            // Min-heap: return true if a has lower score than b
            return a.score < b.score;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Lucene PriorityQueue Learning ===\n");
        
        // Example 1: Top-K search results (min-heap for top scores)
        topKSearchExample();
        
        // Example 2: Using updateTop() optimization
        updateTopExample();
        
        // Example 3: insertWithOverflow for bounded collections
        overflowExample();
    }
    
    static void topKSearchExample() {
        System.out.println("1. Top-K Search Results:");
        
        // Create priority queue for top 3 documents (min-heap by score)
        DocumentPriorityQueue topDocs = new DocumentPriorityQueue(3);
        
        // Simulate finding documents with different scores
        Document[] allDocs = {
            new Document(1, 0.95f, "Lucene Tutorial"),
            new Document(2, 0.87f, "Search Basics"), 
            new Document(3, 0.92f, "Index Structure"),
            new Document(4, 0.78f, "Query Processing"),
            new Document(5, 0.99f, "Performance Tips"),
            new Document(6, 0.85f, "Analysis Chain")
        };
        
        // Collect top 3 documents
        for (Document doc : allDocs) {
            if (topDocs.size() < 3) {
                topDocs.add(doc);
                System.out.println("Added: " + doc + " (heap size: " + topDocs.size() + ")");
            } else if (doc.score > topDocs.top().score) {
                Document removed = topDocs.updateTop(doc);
                System.out.println("Replaced: " + removed + " with: " + doc);
            } else {
                System.out.println("Rejected: " + doc + " (score too low)");
            }
        }
        
        // Extract results manually (highest score first)
        System.out.println("\nTop 3 Results (highest score first):");
        Document[] results = new Document[topDocs.size()];
        for (int i = results.length - 1; i >= 0; i--) {
            results[i] = topDocs.pop();
        }
        for (int i = 0; i < results.length; i++) {
            System.out.println((i + 1) + ". " + results[i]);
        }
        System.out.println();
    }
    
    static void updateTopExample() {
        System.out.println("2. UpdateTop Optimization:");
        
        DocumentPriorityQueue pq = new DocumentPriorityQueue(3);
        
        // Add initial documents
        pq.add(new Document(1, 0.5f, "Doc1"));
        pq.add(new Document(2, 0.7f, "Doc2"));  
        pq.add(new Document(3, 0.6f, "Doc3"));
        
        System.out.println("Initial heap top: " + pq.top());
        
        // Simulate updating the score of the top document
        Document topDoc = pq.top();
        topDoc.score = 0.9f;  // Increase score
        
        System.out.println("After modifying top doc score to 0.9:");
        System.out.println("Before updateTop(): " + pq.top());
        
        pq.updateTop();  // Re-heapify
        System.out.println("After updateTop(): " + pq.top());
        System.out.println();
    }
    
    static void overflowExample() {
        System.out.println("3. InsertWithOverflow for Bounded Collections:");
        
        DocumentPriorityQueue boundedPQ = new DocumentPriorityQueue(2);  // Only keep top 2
        
        Document[] docs = {
            new Document(1, 0.3f, "Low Score"),
            new Document(2, 0.8f, "High Score"),
            new Document(3, 0.5f, "Medium Score"),
            new Document(4, 0.9f, "Highest Score")
        };
        
        for (Document doc : docs) {
            Document overflow = boundedPQ.insertWithOverflow(doc);
            if (overflow == null) {
                System.out.println("Added: " + doc + " (no overflow)");
            } else if (overflow == doc) {
                System.out.println("Rejected: " + doc + " (too low)");
            } else {
                System.out.println("Added: " + doc + ", evicted: " + overflow);
            }
        }
        
        System.out.println("\nFinal top 2 documents:");
        while (boundedPQ.size() > 0) {
            System.out.println(boundedPQ.pop());
        }
    }
}
