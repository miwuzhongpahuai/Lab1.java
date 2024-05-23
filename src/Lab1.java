import java.io.*;
import java.util.*;


public class Lab1 {
    // D:\Graph\Graphviz\bin\dot.exe -Tpng -O graph.png D:\java0\se_lab1\src\mygraph.dot
    // 图数据结构
    static class Graph {
        Map<String, Set<String>> adjList = new HashMap<>(); // 邻接表
        Map<String, Map<String, Integer>> weights = new HashMap<>(); // 边权重

        // 添加边
        public void addEdge(String source, String target) {
            adjList.computeIfAbsent(source, k -> new HashSet<>()).add(target);
            weights.computeIfAbsent(source, k -> new HashMap<>()).compute(target, (k, v) -> (v == null) ? 1 : v + 1);
        }
    }

    // 从文本文件构建有向图
    static Graph buildGraph(String filePath) throws IOException {
        Graph graph = new Graph();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] words = line.toLowerCase().replaceAll("[^a-z\\s]", " ").trim().split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                graph.addEdge(words[i], words[i + 1]);
            }
        }
        reader.close();
        return graph;
    }

    // 展示有向图 (控制台输出)
    static void showDirectedGraph(Graph graph) {
        for (String source : graph.adjList.keySet()) {
            System.out.print(source + " --> ");
            for (String target : graph.adjList.get(source)) {
                int weight = graph.weights.get(source).get(target);
                System.out.print(target + "(" + weight + ") ");
            }
            System.out.println();
        }
    }

    // 查询桥接词
    static String queryBridgeWords(Graph graph, String word1, String word2) {
        if (!graph.adjList.containsKey(word1) || !graph.adjList.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        // 遍历 word1 的所有后邻词
        for (String target : graph.adjList.getOrDefault(word1, new HashSet<>())) {
            if (graph.weights.getOrDefault(target, new HashMap<>()).containsKey(word2)) {
                bridgeWords.add(target);
            }
        }
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            List<String> bridgeWordsList = new ArrayList<>(bridgeWords);
            if (bridgeWordsList.size() > 1) {
                return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWordsList.subList(0, bridgeWordsList.size() - 1)) + " and " + bridgeWordsList.get(bridgeWordsList.size() - 1) + ".";
            } else {
                return "The bridge words from " + word1 + " to " + word2 + " is: " + bridgeWordsList.get(0);
            }
        }
    }
    // 根据桥接词生成新文本
    static String generateNewText(Graph graph, String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return "Input text is empty!";
        }

        String[] words = inputText.toLowerCase().split("\\s+");
        StringBuilder newText = new StringBuilder();
        List<String> bridgeWordsList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < words.length - 1; i++) {
            newText.append(words[i]).append(" ");
            String result = queryBridgeWords(graph, words[i], words[i+1]);

            if (result.startsWith("The bridge words")) {
                // Extract bridge words from result and randomly select one
                String[] parts = result.substring(result.indexOf(':') + 2).split(",| and ");
                List<String> possibleBridgeWords = Arrays.asList(parts);
                String bridgeWord = possibleBridgeWords.get(random.nextInt(possibleBridgeWords.size())).trim();
                newText.append(bridgeWord).append(" ");
                bridgeWordsList.add(bridgeWord);
            }
        }
        newText.append(words[words.length - 1]);
        System.out.println("Bridge words used: " + bridgeWordsList);
        return newText.toString();
    }
    static String getRandomBridgeWord(Set<String> bridgeWords, Random random) {
        List<String> bridgeWordsList = new ArrayList<>(bridgeWords);
        return bridgeWordsList.get(random.nextInt(bridgeWordsList.size()));
    }


    // 计算两个单词之间的最短路径 (Dijkstra 算法)
    static String calcShortestPath(Graph graph, String word1, String word2) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        // 初始化所有 "word1" 节点的距离
        int word1Index = 0;
        for (String node : graph.adjList.keySet()) {
            if (node.startsWith(word1)) {
                distances.put(node + word1Index, 0);
                queue.offer(node + word1Index);
                word1Index++;
            } else {
                distances.put(node, Integer.MAX_VALUE);
            }
        }
        if (word1Index == 0) {
            return "No \"" + word1 + "\" in the graph!";
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            String currentWord = current.replaceAll("\\d", "");

            if (currentWord.equals(word2)) {
                return buildPath(previous, current)
                        .replaceAll("\\d", ""); // 去除数字
            }

            visited.add(current);

            Map<String, Integer> currentWeights = graph.weights.get(currentWord);
            if (currentWeights != null) {
                for (String neighbor : graph.adjList.get(currentWord)) {
                    if (!visited.contains(neighbor)) {
                        Integer weight = currentWeights.get(neighbor);
                        if (weight != null) {
                            int distance = distances.get(current) + weight;
                            if (distance < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                                distances.put(neighbor, distance);
                                previous.put(neighbor, current);
                                queue.offer(neighbor);
                            }
                        }
                    }
                }
            }
        }

        return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
    }

    private static String buildPath(Map<String, String> previous, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        return String.join(" --> ", path);
    }

    // 随机游走
    static String randomWalk(Graph graph) throws IOException {
        List<String> visitedNodes = new ArrayList<>();
        List<String> visitedEdges = new ArrayList<>();
        String current = new ArrayList<>(graph.adjList.keySet()).get(new Random().nextInt(graph.adjList.size()));
        visitedNodes.add(current);
        while (graph.adjList.containsKey(current)) {
            List<String> neighbors = new ArrayList<>(graph.adjList.get(current));
            if (neighbors.isEmpty()) {
                break;
            }
            String next = neighbors.get(new Random().nextInt(neighbors.size()));
            String edge = current + " --> " + next;
            if (visitedEdges.contains(edge)) {
                break;
            }
            visitedEdges.add(edge);
            visitedNodes.add(next);
            current = next;
        }
        String output = String.join(" ", visitedNodes);
        BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk.txt"));
        writer.write(output);
        writer.close();
        return output;
    }

    // 主程序入口
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path to the text file: ");
        String filePath = scanner.nextLine();
        Graph graph = buildGraph(filePath);

        // 主循环
        while (true) {
            System.out.println("\nChoose a function:");
            System.out.println("1. Show directed graph");
            System.out.println("2. Query bridge words");
            System.out.println("3. Generate new text");
            System.out.println("4. Calculate shortest path");
            System.out.println("5. Random walk");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    showDirectedGraph(graph);
                    break;
                case 2:
                    System.out.print("Enter the first word: ");
                    String word1 = scanner.nextLine().toLowerCase();
                    System.out.print("Enter the second word: ");
                    String word2 = scanner.nextLine().toLowerCase();
                    System.out.println(queryBridgeWords(graph, word1, word2));
                    break;
                case 3:
                    System.out.print("Enter a line of text: ");
                    String inputText = scanner.nextLine();
                    System.out.println("Generated text: " + generateNewText(graph, inputText));
                    break;
                case 4:
                    System.out.print("Enter the first word: ");
                    word1 = scanner.nextLine().toLowerCase();
                    System.out.print("Enter the second word: ");
                    word2 = scanner.nextLine().toLowerCase();
                    System.out.println(calcShortestPath(graph, word1, word2));
                    break;
                case 5:
                    System.out.println("Random walk result: " + randomWalk(graph));
                    break;
                case 0:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}