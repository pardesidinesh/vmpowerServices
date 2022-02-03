package com.krish.empower.flowret;

import com.krish.empower.jdocs.BaseUtils;
import com.krish.empower.jdocs.Document;
import com.krish.empower.jdocs.JDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class FileDao implements FlowretDao {

  private String filePath = null;
  private Map<String, Long> counters = new HashMap<>();

  public FileDao(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void write(String key, Document d) {
    FileWriter fw = null;
    try {
      fw = new FileWriter(filePath + key + ".json");
      fw.write(d.getPrettyPrintJson());
      fw.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Document read(String key) {
    InputStream is = null;
    try {
      is = new BufferedInputStream(new FileInputStream(filePath + key + ".json"));
    }
    catch (FileNotFoundException e) {
    }

    Document d = null;
    if (is != null) {
      String json = BaseUtils.getStringFromStream(is);
      d = new JDocument(json);
      try {
        is.close();
      }
      catch (IOException e) {
      }
    }

    return d;
  }

  @Override
  public synchronized long incrCounter(String key) {
    Long val = counters.get(key);
    if (val == null) {
      val = 0L;
      counters.put(key, val);
    }
    else {
      val = val + 1;
      counters.put(key, val);
    }
    return val;
  }

  public void delete(String key) {
    try {
      Files.deleteIfExists(Paths.get(filePath + CONSTS_FLOWRET.DAO.JOURNEY + CONSTS_FLOWRET.DAO.SEP + key + ".json"));
      Files.deleteIfExists(Paths.get(filePath + CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + key + ".json"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
