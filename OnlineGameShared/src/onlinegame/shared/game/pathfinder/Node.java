package onlinegame.shared.game.pathfinder;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.List;
import onlinegame.shared.MathUtil;

/**
 *
 * @author Alfred
 */
public final class Node implements Comparable<Node>
{
    public final double x, y;
    private final List<Node> neighbors;
    private final TDoubleList neighborCost;
    
    //for Pathfinder
    Node parent;
    double cost;
    double estCost;
    double costIfStartBlocked;
    double estCostIfStartBlocked;
    Node parentIfStartBlocked;
    boolean visited;
    boolean checkedVisibilityFromStart;
    int pathNodes;
    int pathNodesIfStartBlocked;
    //
    
    public Node(double x, double y)
    {
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<>();
        neighborCost = new TDoubleArrayList();
    }
    Node(double x, double y, int initialNeighborCapacity)
    {
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<>(initialNeighborCapacity);
        neighborCost = new TDoubleArrayList(initialNeighborCapacity);
    }
    
    public double connect(Node other)
    {
        return connect(other, 1);
    }
    public double connect(Node other, boolean twoWay)
    {
        return connect(other, 1, twoWay);
    }
    private double connect(Node other, double weight)
    {
        return connect(other, weight, true);
    }
    private double connect(Node other, double weight, boolean twoWay)
    {
        double neighborCost = MathUtil.dist(x, y, other.x, other.y) * weight;
        addNeighbor(other, neighborCost);
        if (twoWay)
        {
            other.addNeighbor(this, neighborCost);
        }
        return neighborCost;
    }
    
    void addNeighbor(Node n, double cost)
    {
        neighbors.add(n);
        neighborCost.add(cost);
    }
    
    void removeNeighbor(int i)
    {
        neighbors.remove(i);
        neighborCost.removeAt(i);
    }
    
    void removeNeighbor(Node n)
    {
        int index = neighbors.indexOf(n);
        removeNeighbor(index);
    }
    
    public int numNeighbors()
    {
        return neighbors.size();
    }
    
    public Node getNeighbor(int i)
    {
        return neighbors.get(i);
    }
    
    public double getNeighborCost(int i)
    {
        return neighborCost.get(i);
    }
    
    public double compareFunc()
    {
        return Math.min(estCost, estCostIfStartBlocked);
    }
    
    @Override
    public int compareTo(Node n)
    {
        double a = compareFunc();
        double b = n.compareFunc();
        //return (a < b) ? 1 : ((a == b) ? 0 : -1);
        return (a < b) ? -1 : ((a == b) ? 0 : 1);
        //return (estCost < n.estCost) ? -1 : ((estCost == n.estCost) ? 0 : 1);
    }
}
