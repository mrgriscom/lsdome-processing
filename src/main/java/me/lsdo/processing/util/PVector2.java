package me.lsdo.processing.util;

/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org
  Copyright (c) 2008 Dan Shiffman
  Copyright (c) 2008-10 Ben Fry and Casey Reas
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License version 2.1 as published by the Free Software Foundation.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */


import java.io.Serializable;


/**
 * ( begin auto-generated from PVector2.xml )
 *
 * A class to describe a two or three dimensional vector. This datatype
 * stores two or three variables that are commonly used as a position,
 * velocity, and/or acceleration. Technically, <em>position</em> is a point
 * and <em>velocity</em> and <em>acceleration</em> are vectors, but this is
 * often simplified to consider all three as vectors. For example, if you
 * consider a rectangle moving across the screen, at any given instant it
 * has a position (the object's location, expressed as a point.), a
 * velocity (the rate at which the object's position changes per time unit,
 * expressed as a vector), and acceleration (the rate at which the object's
 * velocity changes per time unit, expressed as a vector). Since vectors
 * represent groupings of values, we cannot simply use traditional
 * addition/multiplication/etc. Instead, we'll need to do some "vector"
 * math, which is made easy by the methods inside the <b>PVector2</b>
 * class.
 *
 * The methods for this class are extensive. For a complete list, visit the
 * <a
 * href="http://processing.googlecode.com/svn/trunk/processing/build/javadoc/core/">developer's reference.</a>
 *
 * ( end auto-generated )
 *
 * A class to describe a two or three dimensional vector.
 * <p>
 * The result of all functions are applied to the vector itself, with the
 * exception of cross(), which returns a new PVector2 (or writes to a specified
 * 'target' PVector2). That is, add() will add the contents of one vector to
 * this one. Using add() with additional parameters allows you to put the
 * result into a new PVector2. Functions that act on multiple vectors also
 * include static versions. Because creating new objects can be computationally
 * expensive, most functions include an optional 'target' PVector2, so that a
 * new PVector2 object is not created with each operation.
 * <p>
 * Initially based on the Vector3D class by <a href="http://www.shiffman.net">Dan Shiffman</a>.
 *
 */
public class PVector2 implements Serializable {

    /**
     * Generated 2010-09-14 by jdf
     */
    private static final long serialVersionUID = -6717872085945400694L;


    public float x;
    public float y;

    /** Array so that this can be temporarily used in an array context */
    transient protected float[] array;

    /**
     * Constructor for an empty vector: x, y, and z are set to 0.
     */
    public PVector2() {
    }


    /**
     * Constructor for a 3D vector.
     *
     * @param  x the x coordinate.
     * @param  y the y coordinate.
     */
    public PVector2(float x, float y) {
        this.x = x;
        this.y = y;
    }


    public float mag() {
        return (float) Math.sqrt(x*x + y*y);
    }

    public float magSq() {
        return (x*x + y*y);
    }


    public void add(PVector2 v) {
        x += v.x;
        y += v.y;
    }

    /**
     * @param x x component of the vector
     * @param y y component of the vector
     * @param z z component of the vector
     */
    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
    }

    static public PVector2 add(PVector2 v1, PVector2 v2) {
        return new PVector2(v1.x + v2.x,v1.y + v2.y);
    }



    public void sub(PVector2 v) {
        x -= v.x;
        y -= v.y;
    }

    /**
     * @param x the x component of the vector
     * @param y the y component of the vector
     * @param z the z component of the vector
     */
    public void sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
    }

    static public PVector2 sub(PVector2 v1, PVector2 v2) {
        return new PVector2(v1.x - v2.x, v1.y - v2.y);
    }

    public void rotate(float theta) {
        float xTemp = x;
        // Might need to check for rounding errors like with angleBetween function?
        x = (float) (x* Math.cos(theta) - y* Math.sin(theta));
        y = (float) (xTemp*Math.sin(theta) + y*Math.cos(theta));
    }

    public void mult(float n) {
        x *= n;
        y *= n;
    }

    static public PVector2 mult(PVector2 v, float n) {
        return new PVector2(v.x*n, v.y*n);
    }

    @Override
    public String toString() {
        return "[ " + x + ", " + y + "]";
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PVector2))
            return false;
        final PVector2 p = (PVector2) obj;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        return result;
    }
}
