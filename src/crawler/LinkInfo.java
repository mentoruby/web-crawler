package crawler;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LinkInfo {
	private static Set<String> linksInScope = Collections.synchronizedSet(new HashSet<String>());
	private static ConcurrentLinkedDeque<String> linksToCrawl = new ConcurrentLinkedDeque<String>();
	private static Set<String> linksOutOfScope = Collections.synchronizedSet(new HashSet<String>());
    private static Set<String> linksNotAccessisble = Collections.synchronizedSet(new HashSet<String>());
    
    /*
     * Collections.synchronizedSet
     * It is imperative that the user manually synchronize on the returned set when iterating over it.
     * Failure to follow this advice may result in non-deterministic behavior.
     */

	public int getNumberOfLinksAlreadyInScope() {
	    return linksInScope.size();
	}
	
	public int getNumberOfLinksPendingToCrawl() {
		/*
		 * this method is NOT a constant-time operation. 
		 * Because of the asynchronous nature of these deques, determining the current number of elements requires traversing them all to count them.
		 * Additionally, it is possible for the size to change during execution of this method, 
		 * in which case the returned result will be inaccurate. 
		 * Thus, this method is typically not very useful in concurrent applications.
		 */
		return linksToCrawl.size();
	}
	
	public String getNextLinkToCrawl() {
		try {
			return linksToCrawl.pop();
		} catch (NoSuchElementException ex) {
			return null;
		}
	}
	
	public synchronized void addLinkToCrawl(String link) {
	    // contains() itself is thread-safe, but not including the subsequence actions
	    // synchronized is used to encapsulate both contains() and add() together to be a single, atomic operation
	    if (!linksToCrawl.contains(link)) {
            linksToCrawl.add(link);
        }
	}
	
	public synchronized void addLinkInScope(String link) {
        // contains() itself is thread-safe, but not including the subsequence actions
        // synchronized is used to encapsulate all sequential actions to be a single and atomic operation
	    if (!linksInScope.contains(link)) {
            this.addLinkToCrawl(link); // another synchronized method, be careful of dead loop
            linksInScope.add(link);
        }
	}
	
	public void addLinkOutOfScope(String link) {
		linksOutOfScope.add(link);
	}
    
    public void addLinkNotAccessible(String link) {
        linksNotAccessisble.add(link);
    }
	
	public void printLinksInScope() {
		synchronized (linksInScope) {
			System.out.println("number of links within scope: " + linksInScope.size());
			int count = 1;
			for (String link : linksInScope) {
				System.out.println(count + ": " + link);
				++count;
			}
		}
	}
	
	public void printLinksOutOfScope() {
		synchronized (linksOutOfScope) {
			System.out.println("number of links not in scope: " + linksOutOfScope.size());
//			int count = 1;
//			for (String link : linksOutOfScope) {
//				System.out.println(count + ": " + link);
//				++count;
//			}
		}
	}
    
    public void printLinksNotAccessible() {
        synchronized (linksNotAccessisble) {
            System.out.println("number of links not accessible: " + linksNotAccessisble.size());
            int count = 1;
            for (String link : linksNotAccessisble) {
                System.out.println(count + ": " + link);
                ++count;
            }
        }
    }
	
	public void printLinksToCrawl() {
		synchronized (linksToCrawl) {
			System.out.println("number of links remaining to crawl: " + linksToCrawl.size());
            int count = 1;
			for(String link : linksToCrawl) {
                System.out.println(count + ": " + link);
                ++count;
			}
		}
	}
    
    public void printLinkSizeStatus() {
        int numOfLinksInScope = this.getNumberOfLinksAlreadyInScope();
        int numOfLinksTBC = this.getNumberOfLinksPendingToCrawl();
        System.out.println(Thread.currentThread().getName() + " Number of links: " + numOfLinksInScope + "(in scope) " + numOfLinksTBC + "(pending to crawl)");
    }
}
