import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

//运行
//javac -encoding UTF-8 Main.java
//java Main y.txt

/**
 * The entry point of the application, which interacts with the user through a menu-driven console
 * interface.
 *
 * <p>The application allows users to:
 *
 * <ul>
 *     <li>Show a directed graph built from a given file</li>
 *     <li>Query bridge words between two words</li>
 *     <li>Generate new text by inserting bridge words</li>
 *     <li>Calculate the shortest path between words</li>
 *     <li>Perform a random walk on the graph</li>
 * </ul>
 * <p>
 * Each option in the menu corresponds to a specific functionality implemented in the class.
 * The graph is built from a file whose path is provided as a command-line argument.
 * </p>
 */
public class Main {

  /**
   * The entry point of the application. Reads the file path argument from the command line, and
   * builds a directed graph from the file using the {@link Graph#buildGraph} method.
   */

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("请提供文件");
      System.exit(1);
    }

    String filePath = args[0]; // 获取文件路径参数
    Map<String, Map<String, Integer>> graph = Graph.buildGraph(filePath);

    while (true) {
      System.out.println("1. Show directed graph");
      System.out.println("2. Query BridgeWords");
      System.out.println("3. Generate New Text");
      System.out.println("4. Calc Shortest Path");
      System.out.println("5. Random Walk");
      System.out.println("0. Exit");
      Scanner scanner = new Scanner(System.in, Charset.forName("UTF-8"));
      int choice = scanner.nextInt();
      scanner.nextLine();
      switch (choice) {
        case 0:
          return;
        case 1:
          Graph.showDirectedGraph(graph);
          // 读取文件并获取Dot语言描述的有向图
          String dotGraph = Graph.generateDotGraph(graph);

          // 保存Dot语言描述的有向图到文件
          String dotFilePath = "graph.dot";
          Graph.saveDotFile(dotGraph, dotFilePath);

          // 使用Graphviz生成图片
          String imageFilePath = "graph.png";
          Graph.generateImage(dotFilePath, imageFilePath);

          System.out.println("Graph visualization saved as: " + imageFilePath);
          break;
        case 2:
          System.out.print("Enter word1: ");
          String word1 = scanner.nextLine();
          word1 = word1.toLowerCase();
          System.out.print("Enter word2: ");
          String word2 = scanner.nextLine();
          word1 = word1.toLowerCase();
          String bridgeWords = Graph.queryBridgeWords(word1, word2, graph);
          System.out.println(bridgeWords);
          break;
        case 3:
          System.out.println("Enter your text:");
          String inputText = scanner.nextLine();
          inputText = inputText.toLowerCase();
          String outputText = Graph.generateNewText(inputText, graph);
          System.out.println("Modified text with bridge words:");
          System.out.println(outputText);
          ;
          break;
        case 4:
          System.out.print("Enter the first word: ");
          word1 = scanner.nextLine();
          word1 = word1.toLowerCase();
          System.out.print(
              "Enter the second word "
                  + "(or press Enter to calculate paths from the first word to all others): ");
          String input = scanner.nextLine();
          String shortestPath;

          if (input.isEmpty() || word1.equals(input.toLowerCase())) {
            // 用户只输入了一个单词，调用新的 calcShortestPathSingleWord 方法
            shortestPath = Graph.calcShortestPathSingleWord(word1, graph);
            System.out.println(shortestPath);
          } else {
            word2 = input.toLowerCase();
            // 用户输入了两个单词，调用现有的 calcShortestPath 方法
            shortestPath = Graph.calcShortestPath(word1, word2, graph);
            String[] parts = shortestPath.split("\n");
            if (parts.length == 1) {
              System.out.println(parts[0]);
            } else {
              String paths = parts[1];
              List<String> path = List.of(paths.split(" -> "));
              String dotshortestGraph = Graph.generateDotGraphWithHighlight(graph, path);
              String dotPath = "shortest_path.dot";
              String imagePath = "shortest_path.png";
              Graph.saveDotFile(dotshortestGraph, dotPath);
              Graph.generateImage(dotPath, imagePath);
              System.out.println(shortestPath);
            }
          }
          break;
        case 5:
          System.out.println("Starting random walk...");
          System.out.println("Press Enter to continue or type 'stop' to end:");
          String walkResult = Graph.randomWalk(graph);
          System.out.println("Random walk result:");
          System.out.println(walkResult);
          try {
            Graph.writeToFile(walkResult, "random_walk.txt");
            System.out.println("Random walk result saved to random_walk.txt");
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        default:
          System.out.println("Invalid choice. Please try again.");
      }
    }
  }


  static class Graph {

    public static Map<String, Map<String, Integer>> buildGraph(String filePath) {
      Map<String, Map<String, Integer>> graph = new HashMap<>();
      try (BufferedReader br = new BufferedReader(
          new FileReader(filePath, StandardCharsets.UTF_8))) {
        StringBuilder str = new StringBuilder(); // 使用 StringBuilder 替代字符串连接
        String line;
        while ((line = br.readLine()) != null) {
          line = line.replaceAll("[^a-zA-Z\\s]", " "); // 去除标点符号
          line = line.toLowerCase(); // 转换为小写
          str.append(line).append(" "); // 追加当前行并添加空格以分隔单词
        }
        //System.out.println(str);
        String[] words = str.toString().trim().split("\\s+"); // 拆分成单词
        for (int i = 0; i < words.length - 1; i++) {
          String currentWord = words[i];
          String nextWord = words[i + 1];
          if (!graph.containsKey(currentWord)) {
            graph.put(currentWord, new HashMap<>());
          }
          Map<String, Integer> neighbors = graph.get(currentWord);
          neighbors.put(nextWord, neighbors.getOrDefault(nextWord, 0) + 1);
        }
        String currentWord = words[words.length - 1];
        if (!graph.containsKey(currentWord)) {
          graph.put(currentWord, new HashMap<>());
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
      return graph;
    }

    public static void showDirectedGraph(Map<String, Map<String, Integer>> graph) {
      // 打印有向图
      for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
        String node = entry.getKey(); // 获取键
        Map<String, Integer> neighbors = entry.getValue(); // 获取值
        if (!neighbors.isEmpty()) {
          System.out.print(node + "->");
          for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
            System.out.print(neighborEntry.getKey() + "(" + neighborEntry.getValue() + ") ");
          }
          System.out.println();
        } else {
          // 如果没有邻居节点，只打印节点本身
          System.out.println(node);
        }
      }
    }


    public static String generateDotGraph(Map<String, Map<String, Integer>> graph) {
      // 生成Dot语言描述的有向图
      StringBuilder dotGraphBuilder = new StringBuilder();
      dotGraphBuilder.append("digraph G {\n");
      for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
        String node = entry.getKey(); // 获取当前节点
        Map<String, Integer> neighbors = entry.getValue(); // 获取当前节点的邻接节点映射
        for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
          String neighbor = neighborEntry.getKey(); // 获取邻接节点
          Integer weight = neighborEntry.getValue(); // 获取边的权重
          dotGraphBuilder.append(node + " -> " + neighbor + " [label=" + weight + "];\n");
        }
      }
      // 读取文件并解析出有向图的边和节点z
      dotGraphBuilder.append("}");
      return dotGraphBuilder.toString();
    }

    // 将字符串表示的有向图保存到dot文件
    public static void saveDotFile(String dotGraph, String dotFilePath) {
      try (FileWriter writer = new FileWriter(dotFilePath, StandardCharsets.UTF_8)) {
        writer.write(dotGraph);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // 使用Graphviz生成图片
    public static void generateImage(String dotFilePath, String imageFilePath) {
      try {
        // 执行Graphviz命令行
        ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng", "-o", imageFilePath,
            dotFilePath);
        Process process = processBuilder.start();
        process.waitFor();
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }

    public static String queryBridgeWords(String word1, String word2,
        Map<String, Map<String, Integer>> graph) {
      StringBuilder result = new StringBuilder();
      // 检查word1和word2是否存在于图中
      if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
        return "No word1 or word2 in the graph!";
      }

      Map<String, Integer> word1Neighbors = graph.get(word1);

      // 查找桥接词
      boolean hasBridgeWords = false;
      for (String bridgeWord : word1Neighbors.keySet()) {
        if (graph.containsKey(bridgeWord) && graph.get(bridgeWord).containsKey(word2)) {
          result.append(bridgeWord).append(", ");
          hasBridgeWords = true;
        }
      }

      if (!hasBridgeWords) {
        return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
      } else {
        result.insert(0, "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
        result.setLength(result.length() - 2); // 移除最后的逗号和空格
        result.append(".");
        return result.toString();
      }
    }

    public static String generateNewText(String text, Map<String, Map<String, Integer>> graph) {
      if (text == null || text.trim().isEmpty()) {
        return text; // 处理空文本情况
      }
      SecureRandom rand = new SecureRandom();

      String[] words = text.replaceAll("[^a-zA-Z\\s]", " ").split("\\s+");
      ;
      if (words.length == 1) {
        return text; // 处理只有一个单词的情况
      }

      StringBuilder modifiedText = new StringBuilder();

      for (int i = 0; i < words.length - 1; i++) {
        modifiedText.append(words[i]).append(" ");
        String bridgeWordResult = queryBridgeWords(words[i], words[i + 1], graph);

        // 检查桥接词的格式
        if (bridgeWordResult.startsWith("The bridge words")) {
          // 提取桥接词部分
          String bridgeWords = bridgeWordResult.substring(bridgeWordResult.indexOf(':') + 2,
              bridgeWordResult.length() - 1);
          String[] bridgeWordsArray = bridgeWords.split(", ");
          String selectedBridgeWord = bridgeWordsArray[rand.nextInt(bridgeWordsArray.length)];
          modifiedText.append(selectedBridgeWord).append(" ");
        }
      }
      modifiedText.append(words[words.length - 1]);

      return modifiedText.toString();
    }

    public static String calcShortestPathSingleWord(String word,
        Map<String, Map<String, Integer>> graph) {
      if (!graph.containsKey(word)) {
        return "The word is not in the graph!";
      }

      // 遍历图中的所有单词
      StringBuilder result = new StringBuilder();
      for (String targetWord : graph.keySet()) {
        if (!targetWord.equals(word)) {
          // 调用现有的 calcShortestPath 方法计算单源最短路径
          String shortestPath = calcShortestPath(word, targetWord, graph);
          result.append(shortestPath).append("\n");
        }
      }

      // 移除最后一个换行符
      if (result.length() > 0) {
        result.setLength(result.length() - 1);
      }

      return result.toString();
    }

    public static String calcShortestPath(String word1, String word2,
        Map<String, Map<String, Integer>> graph) {
      if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
        return "One or both words are not in the graph!";
      }
      /*
      distances: 存储从起点到每个节点的最短距离。
      previousNodes: 存储每个节点的前一个节点，用于构建路径。
      nodes: 优先队列，用于选择具有最小距离的节点进行扩展。
      visited: 记录已访问的节点，避免重复访问。
      * */
      Map<String, Integer> distances = new HashMap<>();
      Map<String, String> previousNodes = new HashMap<>();
      PriorityQueue<String> nodes = new PriorityQueue<>(Comparator.comparingInt(distances::get));
      Set<String> visited = new HashSet<>();

      for (String node : graph.keySet()) {
        if (node.equals(word1)) {
          distances.put(node, 0);
        } else {
          distances.put(node, Integer.MAX_VALUE);
        }
        nodes.add(node);
      }

      while (!nodes.isEmpty()) {
        String closestNode = nodes.poll();
        visited.add(closestNode);

        if (distances.get(closestNode) == Integer.MAX_VALUE) {
          break;
        }
        visited.add(closestNode);
        Map<String, Integer> neighbors = graph.get(closestNode);
        for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
          if (visited.contains(neighbor.getKey())) {
            continue;
          }

          int newDist = distances.get(closestNode) + neighbor.getValue();
          //System.out.println(closestNode + "->" + neighbor.getKey() + " " + newDist);
          if (distances.containsKey(neighbor.getKey()) && newDist < distances.get(
              neighbor.getKey())) {
            distances.put(neighbor.getKey(), newDist);
            previousNodes.put(neighbor.getKey(), closestNode);
            nodes.remove(neighbor.getKey());
            nodes.add(neighbor.getKey());
          }
        }
      }

      if (distances.get(word2) == Integer.MAX_VALUE) {
        return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
      }

      List<String> path = new ArrayList<>();
      for (String at = word2; at != null; at = previousNodes.get(at)) {
        path.add(at);
      }
      Collections.reverse(path);
      StringBuilder result = new StringBuilder();
      result.append("The shortest path from \"").append(word1).append("\" to \"").append(word2)
          .append("\" is:\n");
      result.append(String.join(" -> ", path));
      result.append("\nPath length: ").append(distances.get(word2));
      return result.toString();
    }

    public static String generateDotGraphWithHighlight(Map<String, Map<String, Integer>> graph,
        List<String> path) {
      StringBuilder dotGraphBuilder = new StringBuilder();
      dotGraphBuilder.append("digraph G {\n");

      Set<String> pathEdges = new HashSet<>();
      for (int i = 0; i < path.size() - 1; i++) {
        pathEdges.add(path.get(i) + "->" + path.get(i + 1));
      }

      for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
        String node = entry.getKey();
        Map<String, Integer> neighbors = entry.getValue();
        for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
          String neighbor = neighborEntry.getKey();
          String edge = node + "->" + neighbor;
          dotGraphBuilder.append(node).append(" -> ").append(neighbor);
          if (pathEdges.contains(edge)) {
            dotGraphBuilder.append(" [label=").append(neighborEntry.getValue())
                .append(", color=red, penwidth=2.0]");
          } else {
            dotGraphBuilder.append(" [label=").append(neighborEntry.getValue()).append("]");
          }
          dotGraphBuilder.append(";\n");
        }
      }

      dotGraphBuilder.append("}");
      return dotGraphBuilder.toString();
    }

    //    String calcShortestPath(String word1, String word2){
    //
    //    }
    //
    public static String randomWalk(Map<String, Map<String, Integer>> graph) {
      if (graph.isEmpty()) {
        return "Graph is empty!";
      }

      SecureRandom rand = new SecureRandom();
      List<String> nodes = new ArrayList<>(graph.keySet());
      String startNode = nodes.get(rand.nextInt(nodes.size()));

      StringBuilder walkPath = new StringBuilder();
      Set<String> visitedEdges = new HashSet<>();
      String currentNode = startNode;

      Scanner scanner = new Scanner(System.in, Charset.forName("UTF-8"));

      while (true) {
        walkPath.append(currentNode).append(" ");
        Map<String, Integer> neighbors = graph.get(currentNode);

        if (neighbors == null || neighbors.isEmpty()) {
          break;
        }

        List<String> neighborList = new ArrayList<>(neighbors.keySet());
        String nextNode = neighborList.get(rand.nextInt(neighborList.size()));
        String edge = currentNode + "->" + nextNode;

        if (visitedEdges.contains(edge)) {
          break;
        }

        visitedEdges.add(edge);
        currentNode = nextNode;
        String userInput = scanner.nextLine();
        if ("stop".equalsIgnoreCase(userInput)) {
          break;
        }
      }
      return walkPath.toString().trim();
    }

    public static void writeToFile(String content, String fileName) throws IOException {
      try (FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
        writer.write(content);
      }
    }
  }
}