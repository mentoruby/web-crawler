package crawler;

import java.util.ArrayList;
import java.util.List;

public class Starter {
	
	public static void printMenu() {
		System.out.println("Usage: crawler.Starter <arg0>");
		System.out.println("arg0: link to crawl");
	}
	
	public static void main(String[] args) throws Exception {
		if(args == null || args.length != 1) {
			System.out.println("Error: Invalid argument input!");
			printMenu();
			System.exit(0);
		}
		
		if(args[0] == null || args[0].isBlank()) {
			System.out.println("Error: arg[0] is mandatory input!");
			printMenu();
			System.exit(0);
		}
		
		LinkInfo linkInfo = new LinkInfo();
		linkInfo.addLinkInScope(args[0]);
		
		int maxNumOfThreads = 3;
		
		List<Crawler> crawlers = new ArrayList<Crawler>();
		for (int i=0; i<maxNumOfThreads; i++) {
			Crawler crawler = new Crawler();
			crawlers.add(crawler);
			crawler.start();
		}
		
		while(true) {
			int countIdle = 0;
			for (Crawler c : crawlers) {
				if (c.getState() == Thread.State.TERMINATED || c.getState() == Thread.State.TIMED_WAITING) {
					++countIdle;
				}
			}
			if (countIdle == maxNumOfThreads) {
				break;
			}
		}
		
		for(Crawler c : crawlers)
		{
			try {
				System.out.println(c.getName() + " status before interruption: " + c.getState());
				c.interrupt();
			} catch (Exception e) {
				System.out.println(Thread.currentThread().getName() + " Error: Fail to interrupt crawler thread " + c.getName());
				e.printStackTrace();
			}
		}
		
		linkInfo.printLinksToCrawl();
		linkInfo.printLinksOutOfScope();
		linkInfo.printLinksInScope();
		
		System.out.println("Finished");
	}
}
