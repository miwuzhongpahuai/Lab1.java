import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


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
            String result = queryBridgeWords(graph, words[i], words[i + 1]);

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
    static List<String> calcShortestPaths(Lab1.Graph graph, String word1, String word2) {
        Map<String, Set<String>> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        List<String> paths = new ArrayList<>();

        int word1Index = 0;
        for (String node : graph.adjList.keySet()) {
            if (node.startsWith(word1)) {
                queue.offer(node + word1Index);
                previous.computeIfAbsent(node + word1Index, k -> new HashSet<>()).add("");
                word1Index++;
            }
        }

        if (word1Index == 0) {
            System.out.println("No \"" + word1 + "\" in the graph!");
            return paths;
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            String currentWord = current.replaceAll("\\d", "");
            visited.add(current);

            if (currentWord.equals(word2)) {
                paths.addAll(buildPaths(previous, current).stream()
                        .map(s -> s.replaceAll("\\d", ""))
                        .collect(Collectors.toList()));
            } else {
                Map<String, Integer> currentWeights = graph.weights.get(currentWord);
                if (currentWeights != null) {
                    for (String neighbor : graph.adjList.get(currentWord)) {
                        String neighborWithIndex = neighbor + "0"; // 默认索引为0
                        if (!visited.contains(neighborWithIndex)) {
                            queue.offer(neighborWithIndex);
                            previous.computeIfAbsent(neighborWithIndex, k -> new HashSet<>()).add(current);
                            visited.add(neighborWithIndex); // 添加到visited，避免重复访问
                        }
                    }
                }
            }
        }

        if (paths.isEmpty()) {
            System.out.println("No path from \"" + word1 + "\" to \"" + word2 + "\"!\"");
        }
        return paths;
    }

    static Set<String> buildPaths(Map<String, Set<String>> previous, String current) {
        Set<String> paths = new HashSet<>();
        if (previous.get(current).contains("")) { // 到达了起始节点
            paths.add(current);
        } else {
            for (String prev : previous.get(current)) {
                for (String path : buildPaths(previous, prev)) {
                    paths.add(path + " " + current);
                }
            }
        }
        return paths;
    }

    static void findAllPaths(Lab1.Graph graph, String current, String endWord, List<String> path, Set<String> visited, List<List<String>> allPaths) {
        visited.add(current);
        path.add(current);

        if (current.equals(endWord)) {
            allPaths.add(new ArrayList<>(path));
        } else {
            for (String neighbor : graph.adjList.getOrDefault(current, new HashSet<>())) {
                if (!visited.contains(neighbor)) {
                    findAllPaths(graph, neighbor, endWord, path, visited, allPaths);
                }
            }
        }
        path.remove(path.size() - 1);
        visited.remove(current);
    }

    static int getPathLength(List<String> path, Lab1.Graph graph) {
        int length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            length += graph.weights.getOrDefault(from, new HashMap<>()).getOrDefault(to, 0);
        }
        return length;
    }

    static List<String> calcAllPaths(Lab1.Graph graph, String word1, String word2) {
        List<List<String>> allPaths = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();

        for (String startNode : graph.adjList.keySet()) {
            if (startNode.startsWith(word1)) {
                findAllPaths(graph, startNode, word2, path, visited, allPaths);
            }
        }

        if (allPaths.isEmpty()) {
            System.out.println("No path from \"" + word1 + "\" to \"" + word2 + "\"!\"");
            return Collections.emptyList();
        }

        int shortestPathLength = allPaths.stream()
                .mapToInt(p -> getPathLength(p, graph))
                .min().orElse(-1);

        for (List<String> p : allPaths) {
            String pathString = String.join(" ", p);
            if (getPathLength(p, graph) == shortestPathLength) {
                System.out.println(">>> " + pathString + " <<< (shortest)");
            } else {
                System.out.println(pathString);
            }
        }

        return allPaths.stream()
                .map(p -> String.join(" ", p))
                .collect(Collectors.toList());
    }

    // 随机游走

    static String randomWalk(Graph graph) throws IOException {
        List<String> visitedNodes = new ArrayList<>();
        Set<String> visitedWords = new HashSet<>(); // 使用 Set 存储 visitedWords
        AtomicReference<String> current = new AtomicReference<>(new ArrayList<>(graph.adjList.keySet()).get(new Random().nextInt(graph.adjList.size())));
        visitedNodes.add(current.get());
        visitedWords.add(current.get().toLowerCase()); // 将第一个单词转换为小写存储

        System.out.println("Random walk started. Press Enter to stop...");
        System.out.println(current.get());

        Thread walkThread = new Thread(() -> {
            while (true) {
                List<String> neighbors = new ArrayList<>(graph.adjList.getOrDefault(current.get(), new HashSet<>()));

                if (neighbors.isEmpty()) {
                    System.out.println("Reached a node with no outgoing edges. Stopping...");
                    return;
                }

                String next = neighbors.get(new Random().nextInt(neighbors.size()));
                String edge = current.get() + " --> " + next;

                // 将下一个单词转换为小写进行比较
                if (!visitedWords.add(next.toLowerCase())) {
                    System.out.println("Repeated word detected: " + next + ". Stopping...");
                    return;
                }

                visitedNodes.add(next);
                visitedWords.add(next.toLowerCase()); // 将访问过的单词转换为小写存储
                current.set(next);
                System.out.println(current.get());

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        walkThread.start();

        new Scanner(System.in).nextLine();

        walkThread.interrupt();

        try {
            walkThread.join();
        } catch (InterruptedException e) {
            System.err.println("线程中断: " + e.getMessage());
            return "";
        }

        String output = String.join(" ", visitedNodes);
        BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk.txt"));
        writer.write(output);
        writer.close();
        System.out.println("\nRandom walk stopped.");
        System.out.println("Visited nodes: " + output);

        return output;
    }



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
                    List<String> shortestPaths = calcShortestPaths(graph, word1, word2);
                    if (!shortestPaths.isEmpty()) {
                        System.out.println("Shortest paths from \"" + word1 + "\" to \"" + word2 + "\":");
                        for (String path : shortestPaths) {
                            System.out.println(path);
                        }
                    }
                    System.out.println("all paths are :");
                    calcAllPaths(graph, word1, word2);
                    break;
                case 5:
                    try {
                        System.out.println("Random walk result: " + randomWalk(graph));
                    } catch (IOException e) {
                        System.err.println("随机游走过程中出现错误: " + e.getMessage());
                    }
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