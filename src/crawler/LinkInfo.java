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
	
	public void printLinksInScope() {
		synchronized (linksInScope) {
			System.out.println("links within scope: ");
			int count = 1;
			for (String link : linksInScope) {
				System.out.println(count + ": " + link);
				++count;
			}
		}
	}
	
	public void printLinksOutOfScope() {
		synchronized (linksOutOfScope) {
			System.out.println("links not in scope: ");
			int count = 1;
			for (String link : linksOutOfScope) {
				System.out.println(count + ": " + link);
				++count;
			}
		}
	}
	
	public void printLinksToCrawl() {
		synchronized (linksToCrawl) {
			System.out.println("links remaining to crawl: ");
			for(String link : linksToCrawl) {
				System.out.println("link to crawl - " + link);
			}
		}
	}
	
	public String getNextLinkToCrawl() {
		try {
			return linksToCrawl.pop();
		} catch (NoSuchElementException ex) {
			return null;
		}
	}
	
	public void addLinkToCrawl(String link) {
		synchronized (linksToCrawl) {
			if (!linksToCrawl.contains(link)) {
				linksToCrawl.add(link);
			}
		}
	}
	
	public void addLinkInScope(String link) {
		linksInScope.add(link);
	}
	
	public void addLinkOutOfScope(String link) {
		linksOutOfScope.add(link);
	}
	
}
