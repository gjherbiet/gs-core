/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */

package org.graphstream.ui.layout.springbox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import org.graphstream.stream.SourceBase;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.LayoutListener;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.ParticleBoxListener;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.BarycenterCellData;
import org.miv.pherd.ntree.CellSpace;
import org.miv.pherd.ntree.OctreeCellSpace;
import org.miv.pherd.ntree.QuadtreeCellSpace;

/**
 * An implementation of spring algorithms to layout a graph.
 * 
 * <p>
 * This spring method use repulsive forces (electric field) between each nodes and attractive forces
 * (springs) between each node connected by an edge. To speed up the algorithm, a n-tree is used to
 * divide space. A Barnes-Hut like algorithm is used to speed up repulsion force influence when
 * nodes are far away.
 * </p>
 * 
 * <p>
 * This algorithm can be configured using several attributes put on the graph :
 * <ul>
 * 		<li>layout.force : a floating point number (default 0.5f), that allows to define the
 * 			importance of movement of each node at each computation step. The larger the value
 * 			the quicker nodes move to their position of lowest energy. However too high values
 * 			can generate non stable layouts and oscillations.</li>
 * 		<li>layout.quality : an integer between 0 and 4. With value 0 the layout is faster but
 * 			it also can be farther from equilibrium. With value 4 the algorithm tries to be as
 * 			close as possible from equilibrium (the n-tree and Barnes-Hut algorithms are disabled),
 * 			but the computation can take a lot of time (the algorithm becomes O(n^2)).</li>
 * </ul>
 * You can also put the following attributes on nodes :
 * <ul>
 * 		<li>layout.weight : The force of repulsion of a node. The larger the value, the more
 * 			the node repulses its neighbours.</li>
 * </ul>
 * And on edges :
 * <ul>
 * 		<li>layout.weight : the multiplier for the desired edge length. By default the algorithm
 * 			tries to make each edge of length one. This is the position of lowest energy for
 * 			a spring. This coefficient allows to modify this target spring length. Value larger
 * 			than one will make the edge longer. Values between 0 and 1 will make the edge
 * 			smaller.</li>
 * </ul>
 * </p>
 */
public class SpringBox extends SourceBase implements Layout, ParticleBoxListener
{
// Attributes -- Data
	
	/**
	 * The nodes representation and the n-tree. The particle-box is an implementation of a recursive
	 * space decomposition method that is used here to break the O(n^2) complexity into something
	 * that is closer to O(n log n).
	 */
	protected ParticleBox nodes;
	
	/**
	 * The set of edges.
	 */
	protected HashMap<String,EdgeSpring> edges = new HashMap<String,EdgeSpring>();
	
	/**
	 * Random number generator.
	 */
	protected Random random;
	
	/**
	 * The lowest node position.
	 */
	protected Point3 lo = new Point3( 0, 0, 0 );
	
	/**
	 * The highest node position.
	 */
	protected Point3 hi = new Point3( 1, 1, 1 );
	
	/**
	 * Set of listeners. These are listeners for specific movement events, however, the usual
	 * "Source" interface is usable to obtain xyz attributes.
	 */
	protected ArrayList<LayoutListener> listeners = new ArrayList<LayoutListener>();
	
	/**
	 * Output stream for statistics if in debug mode.
	 */
	protected PrintStream statsOut;
	
	/**
	 * Energy, and the history of energies.
	 */
	protected Energies energies = new Energies();
	
// Attributes -- Parameters
	
	/**
	 * The optimal distance between nodes.
	 */
	protected float k = 1f;

	/**
	 * Default attraction.
	 */
	protected float K1 = 0.06f; // 0.3 ??

	/**
	 * Default repulsion.
	 */
	protected float K2 = 0.024f; // 0.12 ??
	
	/**
	 * Global force strength. This is a factor in [0..1] that is used to scale all computed
	 * displacements.
	 */
	protected float force = 1f;
	
	/**
	 * The view distance at which the cells of the n-tree are explored exhaustively,
	 * after this the poles are used. This is a multiple of k. 
	 */
	protected float viewZone = 5f;

	/**
	 * The Barnes/Hut theta threshold to know if we use a pole or not.
	 */
	protected float theta = .7f;
	
	/**
	 * The quality level.
	 */
	protected int quality = 1;
	
	/**
	 * Number of nodes per space-cell.
	 */
	protected int nodesPerCell = 10;

// Attributes -- Statistics
	
	/**
	 * Current step.
	 */
	protected int time;
	
	/**
	 * The duration of the last step in milliseconds.
	 */
	protected long lastStepTime;
	
	/**
	 * The diagonal of the graph area at the current step.
	 */
	protected float area = 1;
	
	/**
	 * The maximum length of a node displacement at the current step.
	 */
	protected float maxMoveLength;
	
	/**
	 * Average move length.
	 */
	protected float avgLength;
	
	/**
	 * Number of nodes that moved during last step.
	 */
	protected int nodeMoveCount;
	
// Attributes -- Settings
		
	/**
	 * Compute the third coordinate ?.
	 */
	protected boolean is3D = false;
	
	/**
	 * Send node informations?.
	 */
	protected boolean sendNodeInfos = false;
	
	/**
	 * If true a file is created to output the statistics of the elastic box algorithm.
	 */
	protected boolean outputStats = false;
	
	/**
	 * If true a file is created for each node (!!!) and its movement statistics are logged.
	 */
	protected boolean outputNodeStats = false;
	
	/**
	 * If greater than one, move events are sent only every N steps. 
	 */
	protected int sendMoveEventsEvery = 1;

// Constructors
	
	public SpringBox()
	{
		this( false );
	}
	
	public SpringBox( boolean is3D )
	{
		this( is3D, new Random( System.currentTimeMillis() ) );
	}
	
	public SpringBox( boolean is3D, Random randomNumberGenerator )
	{
		CellSpace space;
		
		this.is3D   = is3D;
		this.random = randomNumberGenerator;

		//checkEnvironment();
		
		if( is3D )
		     space = new OctreeCellSpace( new Anchor( -1, -1, -1 ), new Anchor( 1, 1, 1 ) );
		else space = new QuadtreeCellSpace( new Anchor( -1, -1, -0.01f ), new Anchor( 1, 1, 0.01f ) );
		
		this.nodes = new ParticleBox( nodesPerCell, space, new BarycenterCellData() );
		
		nodes.addParticleBoxListener( this );
		setQuality( quality );
		
//		System.err.printf( "You are using the SpringBox (sur le retour) layout algorithm !%n" );
	}
	
//	protected void checkEnvironment()
//	{
//		Environment env = Environment.getGlobalEnvironment();
//		
//		if( env.hasParameter( "Layout.3d" ) )
//			this.is3D = env.getBooleanParameter( "Layout.3d" );
//	}

// Access

	public Point3 getLowPoint()
	{
		org.miv.pherd.geom.Point3 p = nodes.getNTree().getLowestPoint(); 
		return new Point3( p.x, p.y, p.z );
	}

	public Point3 getHiPoint()
	{
		org.miv.pherd.geom.Point3 p = nodes.getNTree().getHighestPoint(); 
		return new Point3( p.x, p.y, p.z );
	}
	
	public ParticleBox getSpatialIndex()
	{
		return nodes;
	}

	public long getLastStepTime()
	{
		return lastStepTime;
	}

	public String getLayoutAlgorithmName()
	{
		return "SpringBox's back";
	}

	public int getNodeMoved()
	{
		return nodeMoveCount;
	}

	public double getStabilization()
	{
		if( time > energies.getBufferSize() )
			return energies.getStabilization();
		
		return 1;
	}
	
	public int getSteps()
	{
		return time;
	}
	
	public int getQuality()
	{
		return quality;
	}
	
	public float getForce()
	{
		return force;
	}

// Commands

	public void setSendNodeInfos( boolean on )
	{
		sendNodeInfos = on;
	}
	
	public void addListener( LayoutListener listener )
	{
		listeners.add( listener );
	}

	public void removeListener( LayoutListener listener )
	{
		int pos = listeners.indexOf( listener );
		
		if( pos >= 0 )
		{
			listeners.remove( pos );
		}
	}

	public void setForce( float value )
	{
		this.force = value;
	}

	public void setQuality( int qualityLevel )
	{
		quality = qualityLevel;
		
		switch( qualityLevel )
		{
			case 0:
				viewZone = k;
				break;
			case 1:
				viewZone = 2*k;
				break;				
			case 2:
				viewZone = 5*k;
				break;
			case 3:
				viewZone = 10*k;
				break;
			case 4:
				System.err.printf( "viewZone = -1%n" );
				viewZone = -1;
				break;
			default:
				System.err.printf( "invalid quality level %d%n", qualityLevel );
				break;
		}
	}

	public void clear()
	{
		// TODO
		throw new RuntimeException( "clear() TODO in ElasticBox. Sorry ;-)" );
	}

	public void compute()
	{
		long t1;
		
		computeArea();
		
		maxMoveLength = Float.MIN_VALUE;
		k             = 1f;
		t1            = System.currentTimeMillis();
		nodeMoveCount = 0;
		avgLength     = 0;
/*
		for( Edge edge : edges.values() )
			edge.attraction();
*/		
		nodes.step();

		if( nodeMoveCount > 0 )
			avgLength /= nodeMoveCount;
		
		// Ready for the next step.

		energies.storeEnergy();
		printStats();
		time++;
		lastStepTime = System.currentTimeMillis() - t1;
		
		for( LayoutListener listener: listeners )
			listener.stepCompletion( (float)getStabilization() );
	}
	
	/**
	 * Output some statistics on the layout process. This method is active only if
	 * {@link #outputStats} is true.
	 */
	protected void printStats()
	{
		if( outputStats )
		{
			if( statsOut == null )
			{
	            try
                {
	                statsOut = new PrintStream( "springBox.dat" );
                }
                catch( FileNotFoundException e )
                {
	                e.printStackTrace();
                }
			}
			
			if( statsOut != null )
			{
				float energyDiff = energies.getEnergy() - energies.getPreviousEnergyValue( 30 );
				
				statsOut.printf( Locale.US, "%f %d %f %f %f %f%n",
						getStabilization(), nodeMoveCount,
						energies.getEnergy(),
						energyDiff,
						maxMoveLength, avgLength,
						area );
				statsOut.flush();
			}
		}
	}
	
	protected void computeArea()
	{
		area = getHiPoint().distance( getLowPoint() );
	}
	
	public void shake()
	{
		energies.clearEnergies();
	}

// Graph representation
	
	protected void addNode( String sourceId, String id ) //throws SingletonException
	{
		nodes.addParticle( new NodeParticle( this, id ) );
	}

	public void moveNode( String id, float dx, float dy, float dz )
	{
		NodeParticle node = (NodeParticle) nodes.getParticle( id );
		
		if( node != null )
		{
			node.move( dx, dy, dz );
			energies.clearEnergies();
		}
	}

	public void freezeNode( String id, boolean on )
	{
		NodeParticle node = (NodeParticle) nodes.getParticle( id );
		
		if( node != null )
		{
			node.frozen = on;
		}
	}

	protected void setNodeWeight( String id, float weight )
	{
		NodeParticle node = (NodeParticle) nodes.getParticle( id );
		
		if( node != null )
			node.setWeight( weight );
	}

	protected void removeNode( String sourceId, String id )
	{
		NodeParticle node = (NodeParticle) nodes.removeParticle( id );
		
		if( node != null )
		{
			node.removeNeighborEdges();
		}
	}

	protected void addEdge( String sourceId, String id, String from, String to, boolean directed )
//			throws NotFoundException, SingletonException
	{
		NodeParticle n0 = (NodeParticle) nodes.getParticle( from );
		NodeParticle n1 = (NodeParticle) nodes.getParticle( to );
		
		if( n0 != null && n1 != null )
		{
			EdgeSpring e = new EdgeSpring( id, n0, n1 );
			EdgeSpring o = edges.put( id, e );
			
			if( o != null )
			{
				//throw new SingletonException( "edge '"+id+"' already exists" );
				System.err.printf( "edge '%s' already exists%n", id );
			}
			else
			{
				n0.registerEdge( e );
				n1.registerEdge( e );
			}
		}
	}

	protected void addEdgeBreakPoint( String edgeId, int points )
	{
		System.err.printf( "edge break points are not handled yet." );
	}
	
	protected void ignoreEdge( String edgeId, boolean on )
	{
		EdgeSpring edge = edges.get( edgeId );
		
		if( edge != null )
		{
			edge.ignored = on;
		}
	}

	protected void setEdgeWeight( String id, float weight )
	{
		EdgeSpring edge = edges.get( id );
		
		if( edge != null )
			edge.weight = weight;
	}

	protected void removeEdge( String sourceId, String id )
	{
		EdgeSpring e = edges.remove( id );
		
		if( e != null )
		{
			e.node0.unregisterEdge( e );
			e.node1.unregisterEdge( e );
		}
	}

	public void outputPos( String filename ) throws IOException
	{
		// TODO Auto-generated method stub
	}

	public void inputPos( String filename ) throws IOException
	{
		// TODO Auto-generated method stub
	}

// Particle box listener

	public void particleAdded( Object id, float x, float y, float z, Object mark )
	{
	}

	public void particleMarked( Object id, Object mark )
	{
	}

	public void particleMoved( Object id, float x, float y, float z )
	{
		if( ( time % sendMoveEventsEvery ) == 0 )
		{
			for( LayoutListener listener: listeners )
				listener.nodeMoved( (String)id, x, y, z );
			
			Object xyz[] = new Object[3];
			xyz[0] = x; xyz[1] = y; xyz[2] = z;

			sendNodeAttributeChanged( getLayoutAlgorithmName(), (String)id, "xyz", xyz, xyz );
		}
	}

	public void particleRemoved( Object id )
	{
	}

	public void stepFinished( int time )
	{
	}

	public void particleAdded( Object id, float x, float y, float z )
    {
    }

	public void particleAttributeChanged( Object id, String attribute, Object newValue,
            boolean removed )
    {
    }	
	
// Output interface

	public void edgeAdded( String graphId, long time, String edgeId, String fromNodeId, String toNodeId, boolean directed )
	{
		addEdge( graphId, edgeId, fromNodeId, toNodeId, directed );
		sendEdgeAdded( graphId, time, edgeId, fromNodeId, toNodeId, directed );
	}
	
	public void nodeAdded( String graphId, long time, String nodeId )
	{
		addNode( graphId, nodeId );
		sendNodeAdded( graphId, time, nodeId );
	}
	
	public void edgeRemoved( String graphId, long time, String edgeId )
	{
		removeEdge( graphId, edgeId );
		sendEdgeRemoved( graphId, time, edgeId );
	}
	
	public void nodeRemoved( String graphId, long time, String nodeId )
	{
		removeNode( graphId, nodeId );
		sendNodeRemoved( graphId, time, nodeId );
	}
	
	public void graphCleared( String graphId, long time )
	{
		clear();
		sendGraphCleared( graphId, time );
	}
	
	public void stepBegins( String graphId, long time, double step )
	{
		sendStepBegins( graphId, time, step );
	}

	public void graphAttributeAdded( String graphId, long time, String attribute, Object value )
    {
		graphAttributeChanged_( graphId, attribute, null, value );
		sendGraphAttributeAdded( graphId, time, attribute, value );
    }
	
	public void graphAttributeChanged( String graphId, long time, String attribute, Object oldValue, Object newValue )
	{
		graphAttributeChanged_( graphId, attribute, oldValue, newValue );
		sendGraphAttributeChanged( graphId, time, attribute, oldValue, newValue );
	}

	protected void graphAttributeChanged_( String graphId, String attribute, Object oldValue, Object newValue )
    {
		if( attribute.equals( "layout.force" ) )
		{
			if( newValue instanceof Number )
				setForce( ((Number)newValue).floatValue() );
			System.err.printf( "layout.elasticBox.force: %f%n", ((Number)newValue).floatValue() );
		}
		else if( attribute.equals( "layout.quality" ) )
		{
			if( newValue instanceof Number )
			{
				int q = ((Number)newValue).intValue();
				
				q = q > 4 ? 4 : q;
				q = q < 0 ? 0 : q;
				
				setQuality( q );
				System.err.printf( "layout.elasticBox.quality: %d%n", q );
			}
		}
		else if( attribute.equals( "layout.exact-zone" ) )
		{
			if( newValue instanceof Number )
			{
				float factor = ((Number)newValue).floatValue();
				
				factor = factor > 1 ? 1 : factor;
				factor = factor < 0 ? 0 : factor;
				
				viewZone = factor;
				System.err.printf( "layout.elasticBox.exact-zone: %f of [0..1]%n", viewZone );
			}
		}
		else if( attribute.equals( "layout.output-stats" ) )
		{
			if( newValue == null )
			     outputStats = false;
			else outputStats = true;
			
			System.err.printf( "layout.elasticBox.output-stats: %b%n", outputStats );
		}
    }

	public void graphAttributeRemoved( String graphId, long time, String attribute )
    {
		sendGraphAttributeRemoved( graphId, time, attribute );
    }

	public void nodeAttributeAdded( String graphId, long time, String nodeId, String attribute, Object value )
    {
		nodeAttributeChanged_( graphId, nodeId, attribute, null, value );
		sendNodeAttributeAdded( graphId, time, nodeId, attribute, value );
    }

	public void nodeAttributeChanged( String graphId, long time, String nodeId, String attribute, Object oldValue, Object newValue )
	{
		nodeAttributeChanged_( graphId, nodeId, attribute, oldValue, newValue );
		sendNodeAttributeChanged( graphId, time, nodeId, attribute, oldValue, newValue );
	}
	
	protected void nodeAttributeChanged_( String graphId, String nodeId, String attribute, Object oldValue, Object newValue )
    {
		if( attribute.equals( "layout.weight" ) )
		{
			if( newValue instanceof Number )
				setNodeWeight( nodeId, ((Number)newValue).floatValue() );
			else if( newValue == null )
				setNodeWeight( nodeId, 1 );
		}
    }

	public void nodeAttributeRemoved( String graphId, long time, String nodeId, String attribute )
    {
		sendNodeAttributeRemoved( graphId, time, nodeId, attribute );
    }

	public void edgeAttributeAdded( String graphId, long time, String edgeId, String attribute, Object value )
    {
		edgeAttributeChanged_( graphId, edgeId, attribute, null, value );
		sendEdgeAttributeAdded( graphId, time, edgeId, attribute, value );
    }

	public void edgeAttributeChanged( String graphId, long time, String edgeId, String attribute, Object oldValue, Object newValue )
	{
		edgeAttributeChanged_( graphId, edgeId, attribute, oldValue, newValue );
		sendEdgeAttributeChanged( graphId, time, edgeId, attribute, oldValue, newValue );
	}

	protected void edgeAttributeChanged_( String graphId, String edgeId, String attribute, Object oldValue, Object newValue )
    {
		if( attribute.equals( "layout.weight" ) )
		{
			if( newValue instanceof Number )
				setEdgeWeight( edgeId, ((Number)newValue).floatValue() );
			else if( newValue == null )
				setEdgeWeight( edgeId, 1 );
		}
		else if( attribute.equals( "layout.ignored" ) )
		{
			if( newValue instanceof Boolean )
				ignoreEdge( edgeId, (Boolean)newValue );
		}
    }

	public void edgeAttributeRemoved( String graphId, long time, String edgeId, String attribute )
    {
		sendEdgeRemoved( attribute, time, edgeId );
    }
}