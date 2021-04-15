package crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class Crawler extends Thread implements Runnable {
	private String path;
	private String domain;
	private LinkInfo linkInfo;
	
	public Crawler() {
		linkInfo = new LinkInfo();
	}
	
	private String getHTMLContent() throws Exception {
		HttpsURLConnection con = null;
		BufferedReader br = null;
		InputStreamReader isr = null;
		
		try {
			URL url = new URL(this.path);
			this.domain = url.getProtocol() + "://" + url.getHost();
			if (url.getPort() > 0) {
				this.domain += ":" + url.getPort();
			}
			
			con = (HttpsURLConnection) url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla");
			con.setRequestMethod("GET");
			
			int responseCode = con.getResponseCode();
			
			if (responseCode == HttpsURLConnection.HTTP_OK) {
				isr = new InputStreamReader(con.getInputStream());
				br = new BufferedReader(isr);
				
				StringBuffer content = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					content.append(line);
				}
				
				return content.toString();
			} else if (responseCode == 404 || responseCode == 403) { // special exception handling for error code 404 and 403
			    throw new PageLoadException(responseCode, "Invalid response code (" + responseCode + ")");
			} else {
				throw new Exception("Invalid response code (" + responseCode + ")");
			}
		} catch (Exception e) {
			System.out.println(Thread.currentThread().getName() + " Error: Fail to get HTML content from " + this.path);
			throw e;
		} finally {
			if(br != null) {
				try { br.close(); } catch(Exception e) {
					System.out.println(Thread.currentThread().getName() + " Error: Fail to close BufferedReader when accessing " + this.path);
				}
			}
			if(isr != null) {
				try { isr.close(); } catch(Exception e) {
					System.out.println(Thread.currentThread().getName() + " Error: Fail to close InputStreamReader when accessing " + this.path);
				}
			}
			if(con != null) {
				try { con.disconnect(); } catch(Exception e) {
					System.out.println(Thread.currentThread().getName() + " Error: Fail to disconnect HttpURLConnection when accessing " + this.path);
				}
			}
		}
	}
	
	private String formatHref(String href) {
		String link = removeQuotes(href);
		
		if (link == null || link.length() == 1) { // ignores single character "#", "/"
			return null;
		}
		
		link = link.toLowerCase();
		
		if (link.endsWith("/")) {
			link = link.substring(0, link.length() - 1);
		}
		
		if (this.domain != null && this.domain.length() > 0) {
			if(link.equals(this.domain)) {
				return this.domain;
			}
			link = link.replaceAll(this.domain, "");
		}
		
		if (link.startsWith("/")) {
			if (this.domain != null && this.domain.length() > 0) {
				return this.domain + link;
			}
			return link;
		}
		
		if (!link.contains("://") && !link.startsWith("#")) {
			return "http://" + link;
		}
		
		return link;
	}
	
	private boolean isLinkInScope(String href) {
		String link = href;
		
		if(this.domain != null && this.domain.length() > 0) {
			// not in the same domain as input link
			if(!link.startsWith(this.domain)) {
				return false;
			}
			
			link = link.replaceAll(this.domain, "");
		}
		
		// pages forbidden by Cloudfare
		if (link.contains("/cdn-cgi/l/email-protection")) {
		    return false;
		}
		
		// disallowed in robots.txt
		if (link.startsWith("/docs")
				|| link.startsWith("/referral")
				|| link.startsWith("/-staging-referral")
				|| link.startsWith("/install")) {
			return false;
		}
		
		// ignore scripting languages and pictures
		if (link.contains(".css")
				|| link.contains(".js")
				|| link.contains(".png")
				|| link.contains(".jpg")
				|| link.contains(".ico")
				|| link.contains(".xml")
				|| link.contains("@")) {
			return false;
		}
		
		return true;
	}
	
	private String removeQuotes(String href) {
		if(href == null || href.trim().length() == 0) {
			return null;
		}
		href = href.replaceAll("'", "");
		href = href.replaceAll("\"", "");
		href = href.trim();
		return href;
	}
	
	// extract <a> tag having href attribute
	// extract value of href attribute, embraced by either 'xxx' or "xxx"
	private static Pattern pattern = Pattern.compile("<[a|link][^>]+href\\s*=\\s*(\"[^\"]*\"|'[^']*')", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
	
	private Set<String> findLinks() throws Exception {
		String content = this.getHTMLContent();
		if(content == null || content.trim().length() == 0) {
			return null;
		}
		
		Set<String> links = new HashSet<String>();
		Matcher matcher = pattern.matcher(content);
		String href;
		while(matcher.find()){
			href = formatHref(matcher.group(1));
			if(href == null || href.isEmpty()) {
				continue;
			}
		    links.add(href);
		}
		return links;
	}

	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted())
		{
			try {
			    this.path = linkInfo.getNextLinkToCrawl();
				if (this.path == null || this.path.length() == 0) {
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						System.out.println(Thread.currentThread().getName() + " is interrupted during sleeping");
						return;
					}
					
					continue;
				}
				
				linkInfo.printLinkSizeStatus();
				
				System.out.println(Thread.currentThread().getName() + " starts to crawl " + this.path);
				
				Set<String> links = this.findLinks();
				
				if(links == null || links.isEmpty()) {
					System.out.println(Thread.currentThread().getName() + " No link found in " + this.path);
					continue;
				}
				
				int countInScope = 0;
				int countOutOfScope = 0;
				for (String link : links) {
					if (isLinkInScope(link)) {
						linkInfo.addLinkInScope(link);
						++countInScope;
					}
					else {
						linkInfo.addLinkOutOfScope(link);
						++countOutOfScope;
					}
				}
				
				System.out.println(Thread.currentThread().getName() + " Number of links found: " + links.size() + "/" + countInScope + "/" + countOutOfScope + " in " + this.path);

			} catch (PageLoadException ex) {
			    System.out.println(Thread.currentThread().getName() + " Error: Fail to find links in " + this.path + " due to " + ex.getMessage() + ". Stop crawling this link.");
			    // failed link no longer handled
			    linkInfo.addLinkNotAccessible(this.path);
			} catch (Exception e) {
				System.out.println(Thread.currentThread().getName() + " Error: Fail to find links in " + this.path + " due to " + e.getMessage() + ". Add it back to crawling list.");
				// add back the link for crawling later
				linkInfo.addLinkToCrawl(this.path);
			}
		}
	}
}
