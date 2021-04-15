package crawler;

import java.util.ArrayList;
import java.util.List;

public class Starter {
	
	public static void printMenu() {
		System.out.println("Usage: crawler.Starter <arg0> (<arg1>)");
		System.out.println("arg0 (Mandatory): link to crawl");
		System.out.println("arg1 (Optional): number of threads to run, must be numeric, default 8");
	}
	
	public static void main(String[] args) throws Exception {
		if(args == null || args.length < 1 || args.length > 2) {
			System.out.println("Error: Invalid argument input!");
			printMenu();
			System.exit(0);
		}
		
		if(args[0] == null || args[0].length() == 0) {
			System.out.println("Error: arg[0] is mandatory input!");
			printMenu();
			System.exit(0);
		}
		
		int maxNumOfThreads = 8;
		if(args.length == 2 && args[1] != null && args[1].length() > 0) {
		    try {
		        maxNumOfThreads = Integer.parseInt(args[1]);
		    } catch (Exception e) {
		        System.out.println("Error: arg[1] must be numeric!");
	            printMenu();
	            System.exit(0);
		    }
		    
		    if (maxNumOfThreads <= 0) {
		        System.out.println("Error: arg[1] must be greater than zero!");
                printMenu();
                System.exit(0);
		    }
        }
		
		System.out.println("Number of threads to run: " + maxNumOfThreads);
		
		LinkInfo linkInfo = new LinkInfo();
		linkInfo.addLinkInScope(args[0]);
		
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
		linkInfo.printLinksNotAccessible();
		linkInfo.printLinksInScope();
		
		System.out.println("Finished");
	}
}
