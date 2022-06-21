package ru.vsu.cs.aisd.g92.lyigina_p_s;

import java.util.*;
import java.util.function.Consumer;

public class GraphAlgorithms {

    /**
     * Поиск в глубину, реализованный рекурсивно
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void dfsRecursion(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];

        class Inner {
            void visit(Integer curr) {
                visitor.accept(curr);
                visited[curr] = true;
                for (Integer v : graph.adjacencies(curr)) {
                    if (!visited[v]) {
                        visit(v);
                    }
                }
            }
        }
        new Inner().visit(from);
    }

    /**
     * Поиск в глубину, реализованный с помощью стека
     * (не совсем "правильный"/классический, т.к. "в глубину" реализуется только "план" обхода, а не сам обход)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void dfs(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(from);
        visited[from] = true;
        while (!stack.empty()) {
            Integer curr = stack.pop();
            visitor.accept(curr);
            for (Integer v : graph.adjacencies(curr)) {
                if (!visited[v]) {
                    stack.push(v);
                    visited[v] = true;
                }
            }
        }
    }

    /**
     * Поиск в ширину, реализованный с помощью очереди
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void bfs(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(from);
        visited[from] = true;
        while (queue.size() > 0) {
            Integer curr = queue.remove();
            visitor.accept(curr);
            for (Integer v : graph.adjacencies(curr)) {
                if (!visited[v]) {
                    queue.add(v);
                    visited[v] = true;
                }
            }
        }
    }

    /**
     * Поиск в глубину в виде итератора
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    public static Iterable<Integer> dfs(Graph graph, int from) {
        return new Iterable<Integer>() {
            private Stack<Integer> stack = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                stack = new Stack<>();
                stack.push(from);
                visited = new boolean[graph.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! stack.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = stack.pop();
                        for (Integer adj : graph.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                stack.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    /**
     * Поиск в ширину в виде итератора
     * (начальная вершина также включена)
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    public static Iterable<Integer> bfs(Graph graph, int from) {
        return new Iterable<Integer>() {
            private Queue<Integer> queue = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                queue = new LinkedList<>();
                queue.add(from);
                visited = new boolean[graph.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! queue.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = queue.remove();
                        for (Integer adj : graph.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                queue.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }


    /**
     * Алгоритм Дейкстры
     * (простейшая реализация без приоритетной очереди за n^2)
     */
    public static class MinDistanceSearchResult {
        public double d[];
        public int from[];
    }

    public static MinDistanceSearchResult dijkstra(WeightedGraph graph, int source, int target) {
        int n = graph.vertexCount();

        double[] d = new double[n];
        int[] from = new int[n];
        boolean[] found = new boolean[n];

        Arrays.fill(d, Double.MAX_VALUE);
        d[source] = 0;
        int prev = -1;
        for (int i = 0; i < n; i++) {
            // ищем "ненайденную" вершину с минимальным d[i]
            // (в общем случае обращение к приоритетной очереди)
            int curr = -1;
            for (int j = 0; j < n; j++) {
                if (!found[j] && (curr < 0 || d[j] < d[curr])) {
                    curr = j;
                }
            }

            found[curr] = true;
            from[curr] = prev;
            if (curr == target) {
                break;
            }
            for (WeightedGraph.WeightedEdgeTo v : graph.adjacenciesWithWeights(curr)) {
                if (d[curr] + v.weight() < d[v.to()]) {
                    d[v.to()] = d[curr] + v.weight();
                    // в общем случае надо было изменить в приоритетной очереди приоритет для v.to()
                }
            }
        }

        // возвращение результата
        MinDistanceSearchResult result = new MinDistanceSearchResult();
        result.d = d;
        result.from = from;
        return result;
    }

    /**
     * Алгоритм Белмана-Форда
     * O(n*m)
     */
    public static MinDistanceSearchResult belmanFord(WeightedGraph graph, int source) {
        int n = graph.vertexCount();

        double[] d = new double[n];
        int[] from = new int[n];

        Arrays.fill(d, Double.MAX_VALUE);
        d[source] = 0;
        for (int i = 0; i < n - 1; i++) {
            boolean changed = false;
            // обход всех связей (в данной реализации - цикл в цикле)
            for (int j = 0; j < n; j++) {
                for (WeightedGraph.WeightedEdgeTo v : graph.adjacenciesWithWeights(j)) {
                    if (d[v.to()] > d[j] + v.weight()) {
                        d[v.to()] = d[j] + v.weight();
                        from[v.to()] = j;
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }

        // возвращение результата
        MinDistanceSearchResult result = new MinDistanceSearchResult();
        result.d = d;
        result.from = from;
        return result;
    }

    private static boolean check(List<Integer> list, int k) {
        int mx = 0, mn = Integer.MAX_VALUE;
        for (int i = 0; i < list.size()-1; i++) {
            mn = Math.min(list.get(i), mn);
            mx = Math.max(list.get(i), mx);
        }
        return mx <= mn*k;
    }

    private static boolean hasFriends(Graph graph, int teamNum, int v, int[] res) {
        for (int i = 0; i < graph.vertexCount(); i++) {
            if (res[i] == teamNum && graph.isAdj(i, v)){
                return true;
            }
        }
        return false;
    }

    private static void splitAlg(Graph graph, int[] res, int teamNum, int currTeam) {
        Queue<Integer> queue = new LinkedList<>();
        int maxAdjCountV1 = -1, maxAdjCountV2 = -1;

        for (int i = 0; i < graph.vertexCount(); i++) {
            if (res[i] == teamNum) {
                queue.add(i);
                res[i] = -1;
                if (maxAdjCountV1 == -1 || graph.getAdjCount(i) > graph.getAdjCount(maxAdjCountV1)){
                    maxAdjCountV2 = maxAdjCountV1;
                    maxAdjCountV1 = i;
                } else if (maxAdjCountV2 == -1 || graph.getAdjCount(i) > graph.getAdjCount(maxAdjCountV2))
                    maxAdjCountV2 = i;
            }
        }
        boolean v1, v2;
        v1 = queue.remove(maxAdjCountV1);
        v2 = queue.remove(maxAdjCountV2);
        if (!v1){
            res[maxAdjCountV2] = teamNum;
            return;
        }
        if (!v2){
            res[maxAdjCountV1] = teamNum;
            return;
        }

        int oldTeamSize = 1, newTeamSize = 1;
        res[maxAdjCountV2] = currTeam;
        res[maxAdjCountV1] = teamNum;
        while (!queue.isEmpty()) {
            int v = queue.poll();
            if (newTeamSize <= oldTeamSize) {
                if (hasFriends(graph, currTeam, v, res)) {
                    res[v] = currTeam;
                    newTeamSize++;
                } else if (hasFriends(graph, teamNum, v, res)) {
                    res[v] = teamNum;
                    oldTeamSize++;
                } else
                    queue.add(v);
            } else {
                if (hasFriends(graph, teamNum, v, res)) {
                    res[v] = teamNum;
                    oldTeamSize++;
                } else if (hasFriends(graph, currTeam, v, res)) {
                    res[v] = currTeam;
                    newTeamSize++;
                } else
                    queue.add(v);
            }
        }
    }

    private static int[] split(Graph graph, int[] teams, int teamNum,
                               int currTeam, int m, int k, boolean first) {
        int[] res = teams.clone();
        if (!first) {
            splitAlg(graph, res, teamNum, currTeam);
            currTeam++;
        }

        //
        if (currTeam-1 != m) {
            int[] res1 = res.clone();
            for (int i = 1; i < currTeam; i++) {
                res = split(graph, res1, i, currTeam, m, k, false);
                if (res != null)
                    break;
            }
        } else if (res != null) {
            List<Integer> teamSize = new ArrayList<>();
            for (int i = 0; i < currTeam; i++){
                teamSize.add(0);
            }
            for (int i : res){
                teamSize.set(i-1, teamSize.get(i-1)+1);
            }
            if (check(teamSize, k))
                return res;
            else
                return null;
        }
        return res;
    }

    public static int[] connectedTeams(Graph graph, int m, int k) {
        int n = graph.vertexCount();
        if (m > n)
            return null;
        int[] teams = new int[n];
        List<Integer> teamSize = new ArrayList<>();
        teamSize.add(0);
        int currTeam = 1;
        for (int v1 = 0; v1 < n; v1++) {
            if (teams[v1] == 0) {
                for (int v2 : dfs(graph, v1)) {
                    teams[v2] = currTeam;
                    teamSize.set(currTeam-1, teamSize.get(currTeam-1)+1);
                }
                currTeam++;
                teamSize.add(0);
            }
        }

        if (currTeam-1 > m)
            return null;
        if (currTeam-1 == m) {
            if (!check(teamSize, k))
                return null;
        } else {
            teams = split(graph, teams, 1, currTeam, m, k, true);
        }
        return teams;
    }

    public static int[] completeTeams(Graph graph, int m, int k) {
        int n = graph.vertexCount();
        if (m > n)
            return null;
        int[] teams = new int[n];

        return teams;
    }
}
