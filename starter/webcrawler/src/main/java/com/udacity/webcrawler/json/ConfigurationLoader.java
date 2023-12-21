package com.udacity.webcrawler.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;
  static ObjectMapper objectMapper = new ObjectMapper();
  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {

	  try ( BufferedReader bufferedReader = new BufferedReader(new FileReader(String.valueOf(path)))) {
          return read(bufferedReader);
      } catch (IOException e) {
          e.printStackTrace();
          return new CrawlerConfiguration.Builder().build();
      }

  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    // This is here to get rid of the unused variable warning.
//	  Objects.requireNonNull(reader);
      objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
      
      try {
          CrawlerConfiguration crawlerConfig = objectMapper.readValue(Objects.requireNonNull(reader), CrawlerConfiguration.Builder.class).build();
          return crawlerConfig;
      } catch (IOException e) {
          e.printStackTrace();
          return null;
      }
  }
}
