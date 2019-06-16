import java.util.ArrayList;
import java.util.List;

public class EdgeWeightedGraph
{
    private static final String NEWLINE = System.getProperty("line.separator");
    private int V;  //顶点数
    private int E;  //边数
    public List<Edge>[] adj;

    @SuppressWarnings("unchecked")
    public EdgeWeightedGraph(int V)
    {
        if (V < 0)
        {
            throw new IllegalArgumentException("顶点数必须是非负数");
        }
        this.V = V;
        this.E = 0;
        adj = (List<Edge>[]) new ArrayList[V];
        for (int v = 0; v < V; v++)
        {
            adj[v] = new ArrayList<Edge>();
        }
    }

    public EdgeWeightedGraph(EdgeWeightedGraph G)
    {
        this(G.V());
        this.E = G.E();
        for (int v = 0; v < G.V(); v++)
        {
            List<Edge> li = new ArrayList<Edge>();
            for (Edge e : G.adj[v])
            {
                li.add(e);
            }
            for (Edge e : li)
            {
                adj[v].add(e);
            }
        }
    }

    public int V()
    {
        return V;
    }

    public int E()
    {
        return E;
    }

    private void validateVertex(int v)
    {
        if (v < 0 || v >= V)
        {
            throw new IllegalArgumentException("顶点序号 " + v + " 不在 0 和 " + (V - 1) + "之间");
        }
    }

    /**
     * 添加边到无向非赋权图中
     */
    public void addEdge(Edge e)
    {
        int v = e.either();
        int w = e.other(v);
        validateVertex(v);
        validateVertex(w);
        adj[v].add(e);
        adj[w].add(e);
        E++;
    }

    /**
     * 返回顶点v的临接边

     */
    public Iterable<Edge> adj(int v)
    {
        validateVertex(v);
        return adj[v];
    }

    /**
     * 返回顶点v的度（邻接顶点数）
     */
    public int degree(int v)
    {
        validateVertex(v);
        return adj[v].size();
    }

    /**
     * 返回无向赋权图中的所有边
     */
    public Iterable<Edge> edges()
    {
        List<Edge> list = new ArrayList<Edge>();
        for (int v = 0; v < V; v++)
        {
            int selfLoops = 0;
            for (Edge e : adj(v))
            {
                // 无向图中，同一条边会出现在这条边的两个端点的邻接列表中，此处的条件 > 目的是避免重复查找
                if (e.other(v) > v)
                {
                    list.add(e);
                }
                // 对于自环，比如(5, 5, 0.8),添加边的时候会添加两次，但实际上只算一条边，所以此处只添加一条
                else if (e.other(v) == v)
                {
                    if (selfLoops % 2 == 0)
                    {
                        list.add(e);
                    }
                    selfLoops++;
                }
            }
        }
        return list;
    }

    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append(V + " " + E + NEWLINE);
        for (int v = 0; v < V; v++)
        {
            s.append(v + ": ");
            for (Edge e : adj[v])
            {
                s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    public static void main(String[] args)
    {

        //        0——————6
        //       /| \    |
        //      / |  \   |
        //     /  1   2  |
        //    /          |
        //   5———————————4
        //    \         /
        //     \       /
        //      \     /
        //       \   /
        //         3

        EdgeWeightedGraph g = new EdgeWeightedGraph(7);

        Edge e1 = new Edge(0, 1, 0.7);
        g.addEdge(e1);

        Edge e2 = new Edge(0, 2, 4.5);
        g.addEdge(e2);

        Edge e3 = new Edge(0, 5, 5.0);
        g.addEdge(e3);

        Edge e4 = new Edge(0, 6, 3.1);
        g.addEdge(e4);

        Edge e5 = new Edge(5, 4, 2.9);
        g.addEdge(e5);

        Edge e6 = new Edge(6, 4, 7.8);
        g.addEdge(e6);

        Edge e7 = new Edge(3, 4, 9.7);
        g.addEdge(e7);

        Edge e8 = new Edge(3, 5, 6.0);
        g.addEdge(e8);

        System.out.println(g);
        e8 = new Edge(3, 5, 7.0);
        g.addEdge(e8);

        EdgeWeightedGraph g1 = new EdgeWeightedGraph(g);
        System.out.println(g1);

    }

}
