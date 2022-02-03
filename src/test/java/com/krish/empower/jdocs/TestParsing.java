package com.krish.empower.jdocs;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;

public class TestParsing {

  private static Logger logger = LogManager.getLogger(TestParsing.class);

  /**
   * @param args
   * @throws IOException
   * @throws JdocsException
   */
  public static void main(String[] args) throws IOException, JdocsException {
    logger.info(parseDocModel("model_response"));
  }

  private static String parseDocModel(String type) throws IOException {
    String json = BaseUtils.getResourceAsString(TestParsing.class, "/jdocs/" + type + ".json");
    return insertReferredModels(json);
  }

  private static String insertReferredModels(String json) throws IOException {
    while (true) {
      StringBuilder sb = new StringBuilder(1024);
      Scanner scanner = new Scanner(json);
      boolean isReferred = false;

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        int indexPattern = line.indexOf("\"@here\"");

        if (indexPattern == 0) {
          isReferred = true;

          String tokens[] = line.split(":");
          String rfn = tokens[1].trim();
          int indexQuote = rfn.lastIndexOf('"');
          rfn = rfn.substring(1, indexQuote);

          String contents = BaseUtils.getResourceAsString(TestParsing.class, rfn);
          contents = contents.trim();
          sb.append(contents.substring(1, contents.length() - 1));

          if (line.charAt(line.length() - 1) == ',') {
            sb.append(',');
          }
        }
        else {
          sb.append(line);
        }
      }

      scanner.close();

      json = sb.toString();

      if (isReferred == false) {
        break;
      }
    }

    JsonNode node = JDocument.objectMapper.readTree(json);
    json = JDocument.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    return json;
  }

}
