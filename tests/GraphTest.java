import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class GraphTest {
    @Test
    public void testCalcShortest1() {
            // 创建测试文件
            Map<String, Map<String, Integer>> graph = Main.Graph.buildGraph("src/y.txt");
            // 调用被测函数
            String result = Main.Graph.calcShortestPath("a", "process", graph);
            System.out.println(result);
            // 验证结果
            assertNotNull(result);
            assertTrue(result.contains("a -> feature -> whereas -> the -> lab -> development -> and -> testing -> process"));
            assertTrue(result.contains("Path length: 9"));
      
    }

    @Test
    public void testCalcShortest2() {
      // 创建测试文件
      Map<String, Map<String, Integer>> graph = Main.Graph.buildGraph("src/y.txt");
      // 调用被测函数
      String result = Main.Graph.calcShortestPath("process", "test", graph);
      System.out.println(result);
      // 验证结果
      assertNotNull(result);
      assertTrue(result.contains("No path from \"process\" to \"test\"!"));

    }

    @Test
    public void testCalcShortest3() {
      // 创建测试文件
      Map<String, Map<String, Integer>> graph = Main.Graph.buildGraph("src/z.txt");
      // 调用被测函数
      String result = Main.Graph.calcShortestPath("without", "if", graph);
      System.out.println(result);
      // 验证结果
      assertNotNull(result);
      assertTrue(result.contains("No path from \"without\" to \"if\"!"));

    }

    @Test
    public void testCalcShortest4() {
      // 创建测试文件
      Map<String, Map<String, Integer>> graph = Main.Graph.buildGraph("src/z.txt");
      // 调用被测函数
      String result = Main.Graph.calcShortestPath("strong", "if", graph);
      System.out.println(result);
      // 验证结果
      assertNotNull(result);
      assertTrue(result.contains("No path from \"strong\" to \"if\"!"));
      
    }

    @Test
    public void testCalcShortest5() {
      // 创建测试文件
      Map<String, Map<String, Integer>> graph = Main.Graph.buildGraph("src/z.txt");
      // 调用被测函数
      String result = Main.Graph.calcShortestPath("w$%#^@!*&()_+/a", "", graph);
      System.out.println(result);
      // 验证结果
      assertNotNull(result);
      assertTrue(result.contains("One or both words are not in the graph!"));
      
    }

    @Test
    public void testCalcShortest6() {
      // 创建测试文件
      Map<String, Map<String, Integer>> graph = new HashMap<>();
      // 调用被测函数
      String result = Main.Graph.calcShortestPath("without", "test", graph);
      System.out.println(result);
      // 验证结果
      assertNotNull(result);
      assertTrue(result.contains("One or both words are not in the graph!"));
      
    }

}
