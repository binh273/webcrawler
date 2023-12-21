package com.udacity.webcrawler.main;

import com.google.inject.Guice;

import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;


import javax.inject.Inject;


import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private static WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    
    String checkProfileOutputPath = config.getProfileOutputPath();
    if (!checkProfileOutputPath.isEmpty()) {
        Path profileOutputPath = Paths.get(checkProfileOutputPath);
        profiler.writeData(profileOutputPath);
    } else {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        profiler.writeData(outputStreamWriter);
    }

    String checkResultPath = config.getResultPath();
    if (!checkResultPath.isEmpty()) {
        Path resultPath = Paths.get(checkResultPath);
        resultWriter.write(resultPath);
    } else {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        resultWriter.write(outputStreamWriter);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }
//    CrawlerConfiguration config =
//	    new CrawlerConfiguration.Builder()
//	        .setImplementationOverride("com.udacity.webcrawler.ParallelWebCrawler")
//	        .setMaxDepth(10)
//	        .setPopularWordCount(3)
//	        .build();
//    CrawlResult result = crawler.crawl(config.getStartPages());
//    System.out.println("Result : " + result.toString());
    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
    
    
    
  }
}
