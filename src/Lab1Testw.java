import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class Lab1Testw {

    // 测试用例 1: 基本路径覆盖
    @Test
    public void testBasicPathCoverage() throws IOException {
        Lab1.Graph graph = new Lab1.Graph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        String result = Lab1.randomWalk(graph);

        assertTrue(result.contains("A B C") || result.contains("B C"));
    }

    // 测试用例 2: 无出边节点
    @Test
    public void testNodeWithNoOutgoingEdges() throws IOException {
        Lab1.Graph graph = new Lab1.Graph();
        graph.addEdge("A", "B");

        String result = Lab1.randomWalk(graph);

        assertTrue(result.contains("Reached a node with no outgoing edges. Stopping..."));
    }

    // 测试用例 3: 重复访问节点
    @Test
    public void testCycleDetection() throws IOException {
        Lab1.Graph graph = new Lab1.Graph();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        String result = Lab1.randomWalk(graph);

        assertTrue(result.contains("Repeated word detected:") && result.contains("Stopping..."));
    }

    // 测试用例 4: 空图
    @Test
    public void testEmptyGraph() throws IOException {
        Lab1.Graph graph = new Lab1.Graph();

        String result = Lab1.randomWalk(graph);

        assertEquals("", result.trim());
    }
}
