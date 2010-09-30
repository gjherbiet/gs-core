package org.graphstream.ui.geom;


/**
 * 2D point.
 *
 * A Point2 is a 2D location in an affine space described by three values along
 * the X, and Y axes. This differs from the Vector3 and Vector4 classes in that
 * it is only 2D and has no vector arithmetic bound to it (to points cannot be
 * added, this would have no mathematical meaning).
 *
 * @author Antoine Dutot
 * @since 20001121 creation
 * @version 0.1
 */
public class Point2
	implements java.io.Serializable
{
// Attributes
	
	private static final long serialVersionUID = 965985679540486895L;

	/**
	 * X axis value.
	 */
	public float x;
	
	/**
	 * Y axis value.
	 */
	public float y;
	
// Attributes -- Shared
	
	/**
	 * Specific point at (0,0).
	 */
	public static final Point2 NULL_POINT2 = new Point2( 0, 0 );

// Constructors

	/**
	 * New 2D point at (0,0).
	 */
	public
	Point2()
	{
	}
	
	/**
	 * New 2D point at (x,y).
	 */
	public
	Point2( float x, float y )
	{
		set( x, y );
	}
	
	/**
	 * New copy of other.
	 */
	public
	Point2( Point2 other )
	{
		copy( other );
	}
	
	/**
	 * New 2D point at (x,y).
	 */
	public void
	make( float x, float y )
	{
		set( x, y );
	}

// Accessors

	/**
	 * Are all components to zero?.
	 */
	public boolean
	isZero()
	{
		return( x == 0 && y == 0 );
	}

//	/**
//	 * Is other equal to this ?
//	 */
//	public boolean
//	equals( const Point2 < float > & other ) const
//	{
//		return( x == other.x
//		and     y == other.y
//		and     z == other.z );
//	}

	/**
	 * Create a new point linear interpolation of this and <code>other</code>.
	 * The new point is located between this and <code>other</code> if
	 * <code>factor</code> is between 0 and 1 (0 yields this point, 1 yields
	 * the <code>other</code> point). 
	 */
	public Point2
	interpolate( Point2 other, float factor )
	{
		Point2 p = new Point2(
			x + ( ( other.x - x ) * factor ),
			y + ( ( other.y - y ) * factor ) );

		return p;
	}

	/**
	 * Distance between this and <code>other</code>.
	 */
	public float
	distance( Point2 other )
	{
		float xx = other.x - x;
		float yy = other.y - y;
		return (float) Math.abs( Math.sqrt( ( xx * xx ) + ( yy * yy ) ) );
	}

// Commands

	/**
	 * Make this a copy of other.
	 */
	public void
	copy( Point2 other )
	{
		x = other.x;
		y = other.y;
	}

	/**
	 * Like #moveTo().
	 */
	public void
	set( float x, float y )
	{
		this.x = x;
		this.y = y;
	}

// Commands -- moving
	
	/**
	 * Move to absolute position (x,y).
	 */
	public void
	moveTo( float x, float y )
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Move of given vector (dx,dy).
	 */
	public void
	move( float dx, float dy )
	{
		this.x += dx;
		this.y += dy;
	}

	/**
	 * Move of given point <code>p</code>.
	 */
	public void
	move( Point2 p )
	{
		this.x += p.x;
		this.y += p.y;
	}
	
	/**
	 * Move horizontally of dx.
	 */
	public void
	moveX( float dx )
	{
		x += dx;
	}
	
	/**
	 * Move vertically of dy.
	 */
	public void
	moveY( float dy )
	{
		y += dy;
	}

	/**
	 * Scale of factor (sx,sy).
	 */
	public void
	scale( float sx, float sy )
	{
		x *= sx;
		y *= sy;
	}

	/**
	 * Scale by factor s.
	 */
	public void
	scale( Point2 s )
	{
		x *= s.x;
		y *= s.y;
	}
	
	/**
	 * Change only abscissa at absolute coordinate x.
	 */
	public void
	setX( float x )
	{
		this.x = x;
	}
	
	/**
	 * Change only ordinate at absolute coordinate y.
	 */
	public void
	setY( float y )
	{
		this.y = y;
	}

	/**
	 * Exchange the values of this and other.
	 */
	public void
	swap( Point2 other )
	{
		float t;

		if( other != this )
		{
			t       = this.x;
			this. x = other.x;
			other.x = t;
			
			t       = this.y;
			this. y = other.y;
			other.y = t;
		}
	}

// Commands -- misc.

	@Override
	public String
	toString()
	{
		StringBuffer buf;

		buf = new StringBuffer( "Point2[" );
		
		buf.append( x );
		buf.append( '|' );
		buf.append( y );
		buf.append( "]" );

		return buf.toString();
	}
}