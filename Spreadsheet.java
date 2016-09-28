import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.lang.Character;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;


public class Spreadsheet {
    private static int numCols, numRows, numCells;
    private static String[][] inputs;
    private static List<Integer>[] graph;
    private static float[] result;


    private static abstract class Cell {
        protected String s;

        Cell(String s) {
            this.s = s;
        }

        public abstract float eval();
    }

    private static class Number extends Cell {
        Number(String value) {
            super(value);
        }

        public float eval() {
            if (this.s.charAt(0) == '-') {
                return -Float.parseFloat(this.s.substring(1));
            }
            return Float.parseFloat(this.s);
        }
    }

    private static class Reference extends Cell {
        Reference(String value) {
            super(value);
        }

        public float eval() {
            return result[cellToIndex((this.s))];
        }

    }

    private static boolean isNumber(String s) {
        return Character.isDigit(s.charAt(0)) ||
                (s.length() >= 2 && s.charAt(0) == '-' && Character.isDigit(s.charAt(1)));
    }

    private static boolean isReference(String s) {
        return Character.isUpperCase(s.charAt(0));
    }

    private static int cellToIndex(String name) {
        int rowIndex = name.charAt(0) - 'A';
        int colIndex = 0;
        try {
            colIndex = Integer.parseInt(name.substring(1)) - 1;
        } catch (NumberFormatException e) {
            System.out.print(String.format("Invalid col index: %s", name));
            System.exit(1);
        }
        if (rowIndex > numRows - 1 || colIndex > numCols - 1) {
            throw new IndexOutOfBoundsException(String.format(
                    "Row num: %d, Col num: %d, Cell name: %s", numRows, numCols, name));
        }
        return rowIndex * numCols + colIndex;

    }

    private static void dfs(List<Integer>[] graph,
                            boolean[] used,
                            List<Integer> sorted,
                            int u,
                            Set<Integer> stack) {
        used[u] = true;
        stack.add(u);
        for (int v: graph[u]) {
            if (stack.contains(v)) {
                System.out.println(String.format("Detect cyclic dependencies on node %d", v));
                System.exit(1);
            }
            if (!used[v]) {
                dfs(graph, used, sorted, v, stack);
            }
        }
        sorted.add(u);
        stack.remove(u);
    }

    private static List<Integer> topologicalSort(List<Integer>[] graph) {
        boolean[] used = new boolean[numCells];
        Set<Integer> stack = new HashSet<>();
        List<Integer> sorted = new ArrayList<>();
        for (int i = 0; i < numCells; ++i) {
            if (!used[i]) {
                dfs(graph, used, sorted, i, stack);
            }
        }
        return sorted; // from sink to root
    }

    private static float calculate(int index) {
        Stack<Cell> stack = new Stack<>();
        try {
            for (String token : inputs[index]) {
                if (isReference(token)) {
                    stack.push(new Reference(token));
                } else if (isNumber(token)) {
                    stack.push(new Number(token));
                } else {
                    float b = stack.pop().eval();
                    float a;
                    float c;
                    switch (token) {
                        case "++":
                            c = b + 1;
                            break;
                        case "--":
                            c = b - 1;
                            break;
                        case "+":
                            a = stack.pop().eval();
                            c = a + b;
                            break;
                        case "-":
                            a = stack.pop().eval();
                            c = a - b;
                            break;
                        case "*":
                            a = stack.pop().eval();
                            c = a * b;
                            break;
                        case "/":
                            a = stack.pop().eval();
                            c = a / b;
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                    stack.push(new Number(Float.toString(c)));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Invalid expression: %s", joinStrings(" ", inputs[index])));
        }
        float res = stack.pop().eval();
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException(String.format("Invalid expression: %s", joinStrings(" ", inputs[index])));
        }
        return res;
    }

    private static String joinStrings(String delimiter, String[] strs) {
        if (strs.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(strs[0]);
        for (int i = 1; i < strs.length; ++i) {
            sb.append(delimiter);
            sb.append(strs[i]);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            numCols = sc.nextInt();
            numRows = sc.nextInt();
            sc.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid inputs");
            System.exit(1);
        }
        numCells = numCols * numRows;
        inputs = new String[numCells][];
        result = new float[numCells];
        graph = new List[numCells];
        for (int i = 0; i < numCells; ++i) {
            String[] temp = sc.nextLine().split(" ");
            inputs[i] = temp;
            graph[i] = new ArrayList<>();
            for (String s : temp) {
                if (isReference(s)) {
                    graph[i].add(cellToIndex(s));
                }
            }
        }
        List<Integer> sorted = topologicalSort(graph);
        for (int j: sorted) {
            result[j] = calculate(j);
        }
        System.out.println(String.format("%d %d", numCols, numRows));
        for (int k = 0; k < numCells; ++k) {
            System.out.println(String.format("%.5f", result[k]));
        }
    }
}
