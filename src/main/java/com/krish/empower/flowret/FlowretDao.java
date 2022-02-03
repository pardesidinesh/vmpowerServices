package com.krish.empower.flowret;

import com.krish.empower.jdocs.Document;


public interface FlowretDao {

  /**
   * Method invoked to write the document to the data store
   *
   * @param key the key used to identify the document
   * @param d   the JDocs document
   */
  public void write(String key, Document d);

  /**
   * The method used to read a document from the data store
   *
   * @param key the key of the document to read
   * @return
   */
  public Document read(String key);

  /**
   * The method used to increment the value of a counter
   *
   * @param key the key for the counter
   * @return the incremented value of the counter
   */
  public long incrCounter(String key);

}
