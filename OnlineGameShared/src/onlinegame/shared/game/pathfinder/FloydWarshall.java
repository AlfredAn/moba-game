package onlinegame.shared.game.pathfinder;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.Arrays;
import onlinegame.shared.Logger;
import onlinegame.shared.Timer;

/**
 *
 * @author Alfred
 */
public final class FloydWarshall
{
    private final float[] dist;
    private final TObjectIntMap<Node> nodeIdMap;
    public final int numVertices;
    
    private FloydWarshall(float[] dist, TObjectIntMap<Node> nodeIdMap, int numVertices)
    {
        this.dist = dist;
        this.nodeIdMap = nodeIdMap;
        this.numVertices = numVertices;
    }
    
    public float getDistance(Node n1, Node n2)
    {
        return getDistanceFast(getNodeIndex(n1), getNodeIndex(n2));
    }
    
    public float getDistance(int n1, int n2)
    {
        if (n1 < 0 || n2 < 0 || n1 >= dist.length || n2 >= dist.length)
        {
            throw new IndexOutOfBoundsException();
        }
        return getDistanceFast(n1, n2);
    }
    
    public float getDistanceFast(int n1, int n2)
    {
        return dist[n1 + n2 * numVertices];
    }
    
    public int getNodeIndex(Node n)
    {
        return nodeIdMap.get(n);
    }
    
    /**
     * Runtime: O(n<sup>3</sup>)
     * <br />
     * Memory: O(n<sup>2</sup>)
     * @param g
     * @return
     */
    public static FloydWarshall generate(Graph g)
    {
        Logger.log("Starting Floyd-Warshall algorithm...");
        
        int numVertices = g.size();
        if (numVertices > 46340) //46340 == (int)Math.sqrt(Integer.MAX_VALUE);
        {
            throw new OutOfMemoryError("Graph is too large (max 46340 nodes).");
        }
        
        Timer t = new Timer();
        
        Logger.log("Initializing array...");
        float[] dist = new float[numVertices * numVertices];
        
        //initialize array to infinity
        Arrays.fill(dist, Float.POSITIVE_INFINITY);
        
        TObjectIntMap<Node> nodeIdMap = new TObjectIntHashMap<>(2 * numVertices + 1, .5f, -1);
        
        for (int i = 0; i < numVertices; i++)
        {
            //set the diagonals (the path from a vertex to itself) to 0
            dist[i + i * numVertices] = 0;
            
            //add the node to the map
            nodeIdMap.put(g.get(i), i);
        }
        
        //initialize edge costs
        for (int i = 0; i < numVertices; i++)
        {
            Node n1 = g.get(i);
            int numEdges = n1.numNeighbors();
            for (int j = 0; j < numEdges; j++)
            {
                Node n2 = n1.getNeighbor(j);
                //Edge e = n1.getEdge(j);
                //Node n2 = e.second == n1 ? e.first : e.second;
                int i2 = nodeIdMap.get(n2);
                
                dist[i + i2 * numVertices] = (float)n1.getNeighborCost(j);
            }
        }
        
        t.print("Starting main Floyd-Warshall loop...");
        int lastFrac = -1;
        
        //main loop
        int kTimesNumVertices = 0;
        for (int k = 0; k < numVertices; k++)
        {
            for (int i = 0; i < numVertices; i++)
            {
                int jTimesNumVertices = 0;
                for (int j = 0; j < numVertices; j++)
                {
                    int ijIndex = i + jTimesNumVertices;
                    float ikPluskj = dist[i + kTimesNumVertices] + dist[k + jTimesNumVertices];
                    if (dist[ijIndex] > ikPluskj)
                    {
                        dist[ijIndex] = ikPluskj;
                    }
                    jTimesNumVertices += numVertices;
                }
            }
            kTimesNumVertices += numVertices;
            
            if ((k & 15) == 0)
            {
                int frac = (k * 100) / numVertices;
                if (frac > lastFrac)
                {
                    t.print(frac + "%");
                    lastFrac = frac;
                }
            }
        }
        
        t.print("100%");
        
        //unoptimized main loop
        /*
        for (int k = 0; k < numVertices; k++)
        {
            for (int i = 0; i < numVertices; i++)
            {
                for (int j = 0; j < numVertices; j++)
                {
                    if (dist[i + j * numVertices] > dist[i + k * numVertices] + dist[k + j * numVertices])
                    {
                        dist[i + j * numVertices] = dist[i + k * numVertices] + dist[k + j * numVertices];
                    }
                }
            }
        }
        */
        
        return new FloydWarshall(dist, nodeIdMap, numVertices);
    }
}





























