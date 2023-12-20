package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
@Wrapped
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final int maxDepth;
  private final PageParserFactory parserFactory;
  private final List<Pattern> ignoredUrls;
  private final ForkJoinPool threadPool;

  @Inject
  ParallelWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount,
      PageParserFactory parserFactory,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.threadPool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.parserFactory = parserFactory;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
	  // 
      ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();
      ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
      Instant deadline = clock.instant().plus(timeout);
      
      // Create task for urls begin process 
      List<RecursiveAction> tasks = startingUrls.stream().map(url -> new CrawlerRecursiveAction(url,
			   deadline,
               maxDepth,
               clock,
               counts,
               visitedUrls,
               ignoredUrls,
               parserFactory)).collect(Collectors.toList());
      // invoke tasks each thread
      tasks.forEach(threadPool::invoke);
      // waiting tasks of each thread done
      threadPool.shutdown();
      
      try {
    	  // waiting if tasks process to timeout
    	  threadPool.awaitTermination(timeout.toMillis(), TimeUnit.MICROSECONDS);
      }catch (Exception e) {
		Thread.currentThread().interrupt();
	}
      
     if(counts.isEmpty()) {
    	 return new CrawlResult.Builder().setWordCounts(counts).setUrlsVisited(visitedUrls.size()).build();
     }else {
    	 return new CrawlResult.Builder().setWordCounts(WordCounts.sort(counts, popularWordCount)).setUrlsVisited(visitedUrls.size()).build();
     }
  }
  
  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
