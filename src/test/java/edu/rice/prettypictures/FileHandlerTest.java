package edu.rice.prettypictures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;

public class FileHandlerTest {
  @Test
  void testReadWrite() {
    FileHandler handler = new FileHandler("src/test/resources/testFileHandler.json");
    Map<Integer, Seq<GeneTree>> map = HashMap.empty();
    map = map.put(0, TestGenesWeek2.randomTrees(10))
        .put(1, TestGenesWeek2.randomTrees(10));
    handler.writeToFile(map);
    assertEquals(handler.readFromFile(),map);
  }

  @Test
  void testBreeding() {
    FileHandler handler = new FileHandler("src/test/resources/testFileHandler.json");
    Map<Integer, Seq<GeneTree>> map = HashMap.empty();
    map = map.put(0, TestGenesWeek2.randomTrees(10))
        .put(1, TestGenesWeek2.randomTrees(10));
    handler.writeToFile(map);
    handler.breed(0,10, List.of("1","3","5"));
    assertNotEquals(map,handler.getStorage());
    assertEquals(map.get(0).get().get(0), handler.getTree(0,0).get());
  }

}
