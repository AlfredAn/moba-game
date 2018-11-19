package onlinegame.shared.game.pathfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Alfred
 */
public final class Graph
{
    private final List<Node> nodes;
    
    public Graph()
    {
        this.nodes = new ArrayList<>();
    }
    public Graph(Node[] nodes)
    {
        this.nodes = new ArrayList<>(nodes.length);
        for (int i = 0; i < nodes.length; i++)
        {
            this.nodes.add(nodes[i]);
        }
    }
    public Graph(Collection<Node> nodes)
    {
        this.nodes = new ArrayList<>(nodes);
    }
    
    public void add(Node n)
    {
        nodes.add(n);
    }
    
    public void remove(int i)
    {
        nodes.remove(i);
    }
    
    public void remove(Node n)
    {
        nodes.remove(n);
    }
    
    public void removeLast()
    {
        nodes.remove(nodes.size() - 1);
    }
    
    public int size()
    {
        return nodes.size();
    }
    
    public Node get(int i)
    {
        return nodes.get(i);
    }
}
