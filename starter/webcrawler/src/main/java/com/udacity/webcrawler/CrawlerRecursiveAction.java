package com.udacity.webcrawler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

public class CrawlerRecursiveAction extends RecursiveAction {
	
	private String url;
    private final List<Pattern> ignoredUrls;
    private Instant deadline;
    private final Clock clock;
    private final PageParserFactory parserFactory;
    private int maxDepth;
    private ConcurrentMap<String, Integer> counts;
    private ConcurrentSkipListSet<String> visitedUrls;
    
    public CrawlerRecursiveAction(String url,
    							   Instant deadline,
                                   int maxDepth,
                                   Clock clock,
                                   ConcurrentMap<String, Integer> counts,
                                   ConcurrentSkipListSet<String> visitedUrls,
                                   List<Pattern> ignoredUrls,
                                   PageParserFactory parserFactory) {
    	this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.clock = clock;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
    }



    public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public Instant getDeadline() {
		return deadline;
	}



	public void setDeadline(Instant deadline) {
		this.deadline = deadline;
	}



	public int getMaxDepth() {
		return maxDepth;
	}



	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}



	public ConcurrentMap<String, Integer> getCounts() {
		return counts;
	}



	public void setCounts(ConcurrentMap<String, Integer> counts) {
		this.counts = counts;
	}



	public ConcurrentSkipListSet<String> getVisitedUrls() {
		return visitedUrls;
	}



	public void setVisitedUrls(ConcurrentSkipListSet<String> visitedUrls) {
		this.visitedUrls = visitedUrls;
	}



	public List<Pattern> getIgnoredUrls() {
		return ignoredUrls;
	}



	public Clock getClock() {
		return clock;
	}



	public PageParserFactory getParserFactory() {
		return parserFactory;
	}



	@Override
    protected void compute() {
		List<CrawlerRecursiveAction> subtasks = new ArrayList<>();
		// Condition return null
		if(maxDepth == 0 || clock.instant().isAfter(deadline))
		{
			return ;
		}

		if(ignoredUrls.stream().anyMatch(pattern -> pattern.matcher(url).matches()) || visitedUrls.contains(url))
		{
			return ;
		}
		// If not return null add url and getLink() create subtasks
		visitedUrls.add(url);
		
		PageParser.Result result = parserFactory.get(url).parse();
		
		result.getWordCounts().forEach((k,v) -> counts.merge(k, v, Integer::sum));
		
		
		for(String link: result.getLinks()) {
			subtasks.add(new CrawlerRecursiveAction(link, deadline, maxDepth-1, clock, counts, visitedUrls, ignoredUrls, parserFactory));
		}
		
		invokeAll(subtasks);
        
    }
}