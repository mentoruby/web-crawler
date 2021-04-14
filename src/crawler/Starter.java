package crawler;

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
		linkInfo.addLinksInScope(args[0]);
		
		try {
			for (int i=0; i<2; i++) {
				Thread thread = new Thread(new Crawler());
				thread.start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			linkInfo.printLinksToCrawl();
			System.out.println("Finished!");
		}
	}
}
