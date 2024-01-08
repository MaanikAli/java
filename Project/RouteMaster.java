import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class RouteMaster extends JFrame {
    // Data structures for the graph and Dijkstra's algorithm
    private Map<String, Map<String, Integer>> graph = new HashMap<>();
    private Set<String> settledNodes = new HashSet<>();
    private Set<String> unsettledNodes = new HashSet<>();
    private Map<String, String> predecessors = new HashMap<>();
    private Map<String, Integer> distance = new HashMap<>();

    // GUI components
    private JButton calculateButton;
    private JTextArea outputArea;
    private JTextArea graphDisplayArea;

    public RouteMaster() {
        // Setting up the main frame
        setTitle("RouteMaster");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Setting up the input panel with source, destination, and calculate button
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        JLabel sourceLabel = new JLabel("Source:");
        JTextField sourceField = new JTextField();
        JLabel destinationLabel = new JLabel("Destination:");
        JTextField destinationField = new JTextField();
        calculateButton = new JButton("Calculate");
        inputPanel.add(sourceLabel);
        inputPanel.add(sourceField);
        inputPanel.add(destinationLabel);
        inputPanel.add(destinationField);
        inputPanel.add(new JLabel()); // Empty label for spacing
        inputPanel.add(calculateButton);
        add(inputPanel, BorderLayout.NORTH);

        // Setting up the graph display area with scroll pane
        JPanel graphPanel = new JPanel(new BorderLayout());
        outputArea = new JTextArea();
        graphPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        graphDisplayArea = new JTextArea();
        JScrollPane graphScrollPane = new JScrollPane(graphDisplayArea);
        graphScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        graphPanel.add(graphScrollPane, BorderLayout.SOUTH);
        add(graphPanel, BorderLayout.CENTER);

        // Setting up buttons to add nodes and edges
        JButton addNodeButton = new JButton("Add Node");
        JButton addEdgeButton = new JButton("Add Edge");
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.add(addNodeButton);
        buttonPanel.add(addEdgeButton);
        add(buttonPanel, BorderLayout.EAST);

        // Setting background colors and fonts for various components
        setComponentStyles(sourceLabel, destinationLabel, sourceField, destinationField, calculateButton,
                addNodeButton, addEdgeButton, outputArea, graphDisplayArea);

        // Adding action listeners for the calculate, add node, and add edge buttons
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String source = sourceField.getText();
                    String destination = destinationField.getText();
                    validateInput(source, destination);
                    calculateShortestPath(source);
                    displayShortestPath(source, destination);
                    displayGraph(); // Display the graph after calculation
                } catch (Exception ex) {
                    outputArea.setText("Error: " + ex.getMessage());
                }
            }
        });

        addNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeName = JOptionPane.showInputDialog("Enter node name:");
                if (nodeName != null && !nodeName.isEmpty()) {
                    addNode(nodeName);
                    displayGraph();
                }
            }
        });

        addEdgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String source = JOptionPane.showInputDialog("Enter source node:");
                String target = JOptionPane.showInputDialog("Enter target node:");
                String weightStr = JOptionPane.showInputDialog("Enter edge weight:");
                if (source != null && target != null && !source.isEmpty() && !target.isEmpty() && weightStr != null && !weightStr.isEmpty()) {
                    try {
                        int weight = Integer.parseInt(weightStr);
                        addEdge(source, target, weight);
                        displayGraph();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid edge weight. Please enter a valid number.");
                    }
                }
            }
        });
    }

    // Method to set background colors and fonts for GUI components
    private void setComponentStyles(JComponent... components) {
        for (JComponent component : components) {
            component.setBackground(new Color(240, 240, 240)); // Light gray background
            component.setForeground(Color.BLACK);
            component.setFont(new Font("Arial", Font.PLAIN, 14));
            if (component instanceof JButton) {
                ((JButton) component).setForeground(Color.WHITE);
                ((JButton) component).setBackground(new Color(52, 152, 219)); // Blue button color
            }
        }
    }

    // Method to perform Dijkstra's algorithm for calculating the shortest path
    private void calculateShortestPath(String source) {
        if (graph.isEmpty()) {
            outputArea.setText("Error: Graph is empty. Add nodes and edges before calculating.");
            return;
        }

        distance.put(source, 0);
        unsettledNodes.add(source);

        while (!unsettledNodes.isEmpty()) {
            String node = getMinimum(unsettledNodes);
            settledNodes.add(node);
            unsettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    // Method to add a node to the graph
    private void addNode(String nodeName) {
        graph.putIfAbsent(nodeName, new HashMap<>());
        updateOutputArea();
    }

    // Method to add an edge to the graph
    private void addEdge(String source, String target, int weight) {
        graph.computeIfAbsent(source, k -> new HashMap<>()).put(target, weight);
        updateOutputArea();
    }

    // Method to update the graph display area
    private void updateOutputArea() {
        StringBuilder output = new StringBuilder("Current Graph:\n");

        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String node = entry.getKey();
            Map<String, Integer> edges = entry.getValue();

            output.append(node).append(" -> ");
            if (!edges.isEmpty()) {
                List<String> edgeList = new ArrayList<>();
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    edgeList.add(edge.getKey() + "(" + edge.getValue() + ")");
                }
                output.append(String.join(", ", edgeList));
            }
            output.append("\n");
        }

        graphDisplayArea.setText(output.toString());
    }

    // Method to display the entire graph in the graph display area
    private void displayGraph() {
        StringBuilder graphDisplay = new StringBuilder("Graph:\n");

        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String node = entry.getKey();
            Map<String, Integer> edges = entry.getValue();

            graphDisplay.append(node).append(" -> ");
            if (!edges.isEmpty()) {
                List<String> edgeList = new ArrayList<>();
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    edgeList.add(edge.getKey() + "(" + edge.getValue() + ")");
                }
                graphDisplay.append(String.join(", ", edgeList));
            }
            graphDisplay.append("\n");
        }

        graphDisplayArea.setText(graphDisplay.toString());
    }

    // Method to display the shortest path and total distance in the output area
    private void displayShortestPath(String source, String destination) {
        List<String> path = getShortestPath(destination);
        int totalDistance = getShortestDistance(destination);

        StringBuilder output = new StringBuilder();
        output.append("Shortest path from ").append(source).append(" to ").append(destination).append(":\n");

        if (totalDistance == Integer.MAX_VALUE) {
            output.append("No path found.");
        } else {
            output.append("Total Distance: ").append(totalDistance).append("\n");
            output.append("Path: ").append(String.join(" -> ", path));
        }

        outputArea.setText(output.toString());
    }

    // Method to validate input nodes before calculating the shortest path
    private void validateInput(String source, String destination) throws Exception {
        if (!graph.containsKey(source) || !graph.containsKey(destination)) {
            throw new Exception("Invalid source or destination node.");
        }
    }

    // Method to get the minimum distance vertex from the set of unsettled nodes
    private String getMinimum(Set<String> vertexes) {
        String minimum = null;
        for (String vertex : vertexes) {
            if (minimum == null || getShortestDistance(vertex) < getShortestDistance(minimum)) {
                minimum = vertex;
            }
        }
        return minimum;
    }

    // Method to find minimal distances to neighboring nodes
    private void findMinimalDistances(String node) {
        List<String> adjacentNodes = getNeighbors(node);
        for (String target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node) + getDistance(node, target));
                predecessors.put(target, node);
                unsettledNodes.add(target);
            }
        }
    }

    // Method to get the weight of an edge between two nodes
    private int getDistance(String node, String target) {
        return graph.get(node).get(target);
    }

    // Method to get the neighboring nodes of a given node
    private List<String> getNeighbors(String node) {
        return new ArrayList<>(graph.get(node).keySet());
    }

    // Method to get the shortest distance to a destination node
    private int getShortestDistance(String destination) {
        Integer d = distance.get(destination);
        return (d == null) ? Integer.MAX_VALUE : d;
    }

    // Method to get the shortest path to a target node
    public List<String> getShortestPath(String target) {
        List<String> path = new ArrayList<>();
        for (String step = target; step != null; step = predecessors.get(step)) {
            path.add(step);
        }
        Collections.reverse(path);
        return path;
    }

    // Main method to start the Swing GUI application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the look and feel to Nimbus for a more modern appearance
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }

            RouteMaster frame = new RouteMaster();
            frame.setVisible(true);
        });
    }
}
