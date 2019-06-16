/**
 * 该类对象可以表示图中的一条边
 */
public class Edge implements Comparable<Edge>
{
    private int v;
    private int w;
    public double weight;

    /**
     * 构造
     */
    public Edge(int v, int w, double weight)
    {
        if (v < 0)
        {
            throw new IllegalArgumentException("顶点v的值必须是一个非负整数");
        }
        if (w < 0)
        {
            throw new IllegalArgumentException("顶点w的值必须是一个非负整数");
        }
        if (Double.isNaN(weight))
        {
            throw new IllegalArgumentException("权重不能是 NaN");
        }
        this.v = v;
        this.w = w;
        this.weight = weight;
    }

    /**
     * 返回权重
     */
    public double weight()
    {
        return weight;
    }

    /**
     * 返回边的其中一个顶点v
     */
    public int either()
    {
        return v;
    }

    /**
     * 返回构成一条边的除vertex的另外一个顶点
     */
    public int other(int vertex)
    {
        if (vertex == v)
        {
            return w;
        } else if (vertex == w)
        {
            return v;
        } else
        {
            throw new IllegalArgumentException("不合法的顶点");
        }
    }

    @Override
    public int compareTo(Edge other)
    {
        if (this.weight() < other.weight())
        {
            return -1;
        } else if (this.weight() > other.weight())
        {
            return 1;
        } else
        {
            return 0;
        }
    }

    public String toString()
    {
        return String.format("%d-%d %.5f", v, w, weight);
    }

    public static void main(String[] args)
    {
        Edge e = new Edge(4, 5, 78.98);
        System.out.println(e);
    }

}
