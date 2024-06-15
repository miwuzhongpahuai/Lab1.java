import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class Lab1Test {

    @Test
    public void testQueryBridgeWords() {
        Lab1.Graph graph = new Lab1.Graph();
        graph.addEdge("hello", "java");
        graph.addEdge("java", "world");
        graph.addEdge("world", "thanks");
        graph.addEdge("hello", "java");

        // 测试用例1: word1 或 word2 不在图中
        assertEquals("No python or world in the graph!", Lab1.queryBridgeWords(graph, "python", "world"));
        assertEquals("No hello or python in the graph!", Lab1.queryBridgeWords(graph, "hello", "python"));
        System.out.println("No hello or python in the graph!" );

        // 测试用例2: word1 和 word2 都在图中且存在桥接词
        assertEquals("The bridge words from hello to world is: java", Lab1.queryBridgeWords(graph, "hello", "world"));
        System.out.println("The bridge words from hello to world is: java" );

        // 测试用例3: word1 和 word2 都在图中但不存在桥接词
        assertEquals("No bridge words from world to java!", Lab1.queryBridgeWords(graph, "world", "java"));
        System.out.println("No bridge words from world to java!" );

        // 测试用例4: word1 和 word2 为相同单词
        assertEquals("No bridge words from hello to hello!", Lab1.queryBridgeWords(graph, "hello", "hello"));
        System.out.println("No bridge words from hello to hello!" );

        // 测试用例5: word1 和 word2 为相邻单词
        assertEquals("No bridge words from world to hello!", Lab1.queryBridgeWords(graph, "world", "hello"));
        System.out.println("No bridge words from world to hello!" );


    }
}
