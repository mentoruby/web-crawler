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
	
	public void printLinksToCrawl() {
		for(String link : linksToCrawl) {
			System.out.println("link to crawl - " + link);
		}
	}
	
	public String getNextLinkToCrawl() {
		try {
			return linksToCrawl.pop();
		} catch (NoSuchElementException ex) {
			return null;
		}
	}
	
	public void addLinksInScope(String link) {
		synchronized (linksInScope) {
			if (!linksInScope.contains(link)) {
				if (!linksToCrawl.contains(link)) {
					linksToCrawl.add(link);
				}
				linksInScope.add(link);
				System.out.println("link included - " + link);
			}
		}
	}
	
	public void addLinksOutOfScope(String link) {
		synchronized (linksOutOfScope) {
			if (!linksOutOfScope.contains(link)) {
				linksOutOfScope.add(link);
				System.out.println("link excluded - " + link);
			}
		}	
	}
	
}
