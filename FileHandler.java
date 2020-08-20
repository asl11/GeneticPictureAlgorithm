package edu.rice.prettypictures;

import edu.rice.io.Files;
import edu.rice.json.Parser;
import edu.rice.json.Value;
import edu.rice.json.Value.JObject;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.Random;

public class FileHandler {
  Map<Integer, Seq<GeneTree>> storage;
  String path;
  private Random random = new Random();

  public FileHandler(String path) {
    this.path = path;
  }

  public Map<Integer, Seq<GeneTree>> readFromFile() {
    if (Files.read(path).isSuccess()
        && !Files.read(path).isEmpty()
        && Parser.parseJsonObject(Files.read(path).get()).isDefined()) {
      String filedata = Files.read(path).get();
      Map<String, Value> pictures = Parser.parseJsonObject(filedata).get().getMap();
      storage =
          pictures
              .mapValues(
                  json -> json.asJArray().getSeq().map(jsonTree -> GeneTree.of(jsonTree).get()))
              .mapKeys(Integer::parseInt);
    }
    return storage;
  }

  public void writeToFile(Map<Integer, Seq<GeneTree>> input) {
    storage = input;
    Map<String, Value> newinput =
        input
            .mapValues(value -> (Value) Value.JArray.fromSeq(value.map(GeneTree::toJson)))
            .mapKeys(Object::toString);
    Files.write(path, JObject.fromMap(newinput).toString());
  }

  public Option<GeneTree> getTree(int genNum, int imageNum) {
    return storage.get(genNum).fold(Option::none, treeList -> Option.of(treeList.get(imageNum)));
  }

  public Map<Integer, Seq<GeneTree>> getStorage() {
    return this.storage;
  }

  public void breed(int genNum, int geneLength, Seq<String> imageList) {
    GeneTree image1 =
        storage
            .get(genNum)
            .get()
            .get(Integer.parseInt(imageList.get(random.nextInt(imageList.length()))));
    GeneTree image2 =
        storage
            .get(genNum)
            .get()
            .get(Integer.parseInt(imageList.get(random.nextInt(imageList.length()))));
    Seq<GeneTree> newGeneration = new TestGenesWeek3(image1, image2, geneLength).getGenes();
    storage = storage.put(storage.keySet().length(), newGeneration);
    writeToFile(storage);
  }
}
