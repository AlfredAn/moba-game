package onlinegame.client.graphics;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * A 4x4 matrix with convenient methods to transform it.
 * 
 * @author Alfred
 */
final class Matrix4f
{
    private static final Matrix4f temp = new Matrix4f();
    
    public float
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33;
    
    /**
     * Creates a new matrix, set to identity.
     */
    private Matrix4f()
    {
        m00 = 1;
        m11 = 1;
        m22 = 1;
        m33 = 1;
    }
    
    /**
     * Creates a new matrix.
     * 
     * @param zero If true, the matrix will be set to zero. Otherwise, it will
     * be set to identity.
     */
    private Matrix4f(boolean zero)
    {
        if (!zero)
        {
            m00 = 1;
            m11 = 1;
            m22 = 1;
            m33 = 1;
        }
    }
    
    /**
     * Creates a new matrix that is a copy of another matrix.
     * 
     * @param src The source matrix.
     */
    private Matrix4f(Matrix4f src)
    {
        set(src);
    }
    
    /**
     * Sets the matrix to the identity matrix.
     */
    private void setIdentity()
    {
        m00 = 1; m01 = 0; m02 = 0; m03 = 0;
        m10 = 0; m11 = 1; m12 = 0; m13 = 0;
        m20 = 0; m21 = 0; m22 = 1; m23 = 0;
        m30 = 0; m31 = 0; m32 = 0; m33 = 1;
    }
    
    /**
     * Sets the entire matrix to zero.
     */
    private void setZero()
    {
        m00 = 0; m01 = 0; m02 = 0; m03 = 0;
        m10 = 0; m11 = 0; m12 = 0; m13 = 0;
        m20 = 0; m21 = 0; m22 = 0; m23 = 0;
        m30 = 0; m31 = 0; m32 = 0; m33 = 0;
    }
    
    /**
     * Sets this matrix to another matrix.
     * 
     * @param src The source matrix.
     */
    private void set(Matrix4f src)
    {
        m00 = src.m00; m01 = src.m01; m02 = src.m02; m03 = src.m03;
        m10 = src.m10; m11 = src.m11; m12 = src.m12; m13 = src.m13;
        m20 = src.m20; m21 = src.m21; m22 = src.m22; m23 = src.m23;
        m30 = src.m30; m31 = src.m31; m32 = src.m32; m33 = src.m33;
    }
    
    /**
     * Multiplies this matrix by the argument matrix.
     * 
     * @param m
     */
    private void mul(Matrix4f m)
    {
        mul(this, m);
    }
    
    /**
     * Multiplies the two argument matrices and places the result in this matrix.
     * 
     * @param m1 The left matrix.
     * @param m2 The right matrix.
     */
    private void mul(Matrix4f m2, Matrix4f m1)
    {
        float n00 = m1.m00 * m2.m00 + m1.m10 * m2.m01 + m1.m20 * m2.m02 + m1.m30 * m2.m03;
	float n01 = m1.m01 * m2.m00 + m1.m11 * m2.m01 + m1.m21 * m2.m02 + m1.m31 * m2.m03;
        float n02 = m1.m02 * m2.m00 + m1.m12 * m2.m01 + m1.m22 * m2.m02 + m1.m32 * m2.m03;
        float n03 = m1.m03 * m2.m00 + m1.m13 * m2.m01 + m1.m23 * m2.m02 + m1.m33 * m2.m03;
        
	float n10 = m1.m00 * m2.m10 + m1.m10 * m2.m11 + m1.m20 * m2.m12 + m1.m30 * m2.m13;
	float n11 = m1.m01 * m2.m10 + m1.m11 * m2.m11 + m1.m21 * m2.m12 + m1.m31 * m2.m13;
	float n12 = m1.m02 * m2.m10 + m1.m12 * m2.m11 + m1.m22 * m2.m12 + m1.m32 * m2.m13;
	float n13 = m1.m03 * m2.m10 + m1.m13 * m2.m11 + m1.m23 * m2.m12 + m1.m33 * m2.m13;
        
	float n20 = m1.m00 * m2.m20 + m1.m10 * m2.m21 + m1.m20 * m2.m22 + m1.m30 * m2.m23;
	float n21 = m1.m01 * m2.m20 + m1.m11 * m2.m21 + m1.m21 * m2.m22 + m1.m31 * m2.m23;
	float n22 = m1.m02 * m2.m20 + m1.m12 * m2.m21 + m1.m22 * m2.m22 + m1.m32 * m2.m23;
	float n23 = m1.m03 * m2.m20 + m1.m13 * m2.m21 + m1.m23 * m2.m22 + m1.m33 * m2.m23;
        
	float n30 = m1.m00 * m2.m30 + m1.m10 * m2.m31 + m1.m20 * m2.m32 + m1.m30 * m2.m33;
	float n31 = m1.m01 * m2.m30 + m1.m11 * m2.m31 + m1.m21 * m2.m32 + m1.m31 * m2.m33;
	float n32 = m1.m02 * m2.m30 + m1.m12 * m2.m31 + m1.m22 * m2.m32 + m1.m32 * m2.m33;
	float n33 = m1.m03 * m2.m30 + m1.m13 * m2.m31 + m1.m23 * m2.m32 + m1.m33 * m2.m33;
        
	m00 = n00; m01 = n01; m02 = n02; m03 = n03;
        m10 = n10; m11 = n11; m12 = n12; m13 = n13;
        m20 = n20; m21 = n21; m22 = n22; m23 = n23;
	m30 = n30; m31 = n31; m32 = n32; m33 = n33;
    }
    
    /**
     * Scales this matrix by the given factor.
     * 
     * @param f The scale factor.
     */
    private void scale(float f)
    {
        scale(this, f, f, f);
    }
    
    /**
     * Scales the source matrix by the given factor and puts the result in this matrix.
     * 
     * @param src The source matrix.
     * @param f The scale factor.
     */
    private void scale(Matrix4f src, float f)
    {
        temp.setToScaling(f);
    }
    
    /**
     * Scales this matrix by the given vector.
     * 
     * @param sx The x scale.
     * @param sy The y scale.
     */
    private void scale(float sx, float sy)
    {
        scale(this, sx, sy);
    }
    
    /**
     * Scales the source matrix by the given vector and puts the result in this matrix.
     * 
     * @param src The source matrix.
     * @param sx The x scale.
     * @param sy The y scale.
     */
    private void scale(Matrix4f src, float sx, float sy)
    {
        temp.setToScaling(sx, sy);
        mul(src, temp);
    }
    
    /**
     * Scales this matrix by the given vector.
     * 
     * @param sx The x scale.
     * @param sy The y scale.
     * @param sz The z scale.
     */
    private void scale(float sx, float sy, float sz)
    {
        scale(this, sx, sy, sz);
    }

    /**
     * Scales the source matrix by the given vector and puts the result in this matrix.
     * 
     * @param src The source matrix.
     * @param sx The x scale.
     * @param sy The y scale.
     * @param sz The z scale.
     */
    private void scale(Matrix4f src, float sx, float sy, float sz)
    {
        temp.setToScaling(sx, sy, sz);
        mul(src, temp);
    }
    
    /**
     * Sets this matrix to a scaling matrix.
     * 
     * @param f The scaling factor.
     */
    private void setToScaling(float f)
    {
        m00 = f; m01 = 0; m02 = 0; m03 = 0;
        m10 = 0; m11 = f; m12 = 0; m13 = 0;
        m20 = 0; m21 = 0; m22 = f; m23 = 0;
        m30 = 0; m31 = 0; m32 = 0; m33 = 1;
    }
    
    /**
     * Sets this matrix to a scaling matrix.
     * 
     * @param sx The x scale.
     * @param sy The y scale.
     */
    private void setToScaling(float sx, float sy)
    {
        m00 = sx; m01 = 0; m02 = 0; m03 = 0;
        m10 = 0; m11 = sy; m12 = 0; m13 = 0;
        m20 = 0; m21 = 0; m22 = 1; m23 = 0;
        m30 = 0; m31 = 0; m32 = 0; m33 = 1;
    }
    
    /**
     * Sets this matrix to a scaling matrix.
     * 
     * @param sx The x scale.
     * @param sy The y scale.
     * @param sz The z scale.
     */
    private void setToScaling(float sx, float sy, float sz)
    {
        m00 = sx; m01 = 0; m02 = 0; m03 = 0;
        m10 = 0; m11 = sy; m12 = 0; m13 = 0;
        m20 = 0; m21 = 0; m22 = sz; m23 = 0;
        m30 = 0; m31 = 0; m32 = 0; m33 = 1;
    }
    
    /**
     * Translates this matrix by the given vector.
     * 
     * @param dx The x component of the translation vector.
     * @param dy The y component.
     */
    private void translate(float dx, float dy)
    {
        translate(this, dx, dy, 0);
    }
    
    /**
     * Translates the source matrix by the given vector and puts the result in this matrix.
     * 
     * @param src The source matrix.
     * @param dx The x component of the translation vector.
     * @param dy The y component.
     */
    private void translate(Matrix4f src, float dx, float dy)
    {
        translate(src, dx, dy, 0);
    }
    
    /**
     * Translates this matrix by the given vector.
     * 
     * @param dx The x component of the translation vector.
     * @param dy The y component.
     * @param dz The z component.
     */
    private void translate(float dx, float dy, float dz)
    {
        translate(this, dx, dy, dz);
    }
    
    /**
     * Translates the source matrix by the given vector and puts the result in this matrix.
     * 
     * @param src The source matrix.
     * @param dx The x component of the translation vector.
     * @param dy The y component.
     * @param dz The z component.
     */
    private void translate(Matrix4f src, float dx, float dy, float dz)
    {
        //can be optimized
        temp.setToTranslation(dx, dy, dz);
        mul(src, temp);
    }
    
    /**
     * Sets this matrix to a translation matrix.
     * 
     * @param dx The x component of the translation vector.
     * @param dy The y component.
     */
    private void setToTranslation(float dx, float dy)
    {
        setToTranslation(dx, dy, 0);
    }
    
    /**
     * Sets this matrix to a translation matrix.
     * 
     * @param dx The x component of the translation vector.
     * @param dy The y component.
     * @param dz The z component.
     */
    private void setToTranslation(float dx, float dy, float dz)
    {
        m00 = 1; m01 = 0; m02 = 0; m03 = dx;
        m10 = 0; m11 = 1; m12 = 0; m13 = dy;
        m20 = 0; m21 = 0; m22 = 1; m23 = dz;
        m30 = 0; m31 = 0; m32 = 0; m33 = 1;
    }
    
    /**
     * Sets this matrix to an orthographic projection matrix.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    private void setOrthoProjection(float x1, float y1, float x2, float y2)
    {
        setOrthoProjection(x1, y1, 1, x2, y2, -1);
    }
    
    /**
     * Sets this matrix to an orthographic projection matrix.
     * 
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     */
    private void setOrthoProjection(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        float s = 1f / (x2 - x1);
        m00 = 2f * s;
        m01 = 0;
        m02 = 0;
        m03 = -(x2 + x1) * s;
        
        s = 1f / (y2 - y1);
        m10 = 0;
        m11 = 2f * s;
        m12 = 0;
        m13 = -(y2 + y1) * s;
        
        s = 1f / (z2 - z1);
        m20 = 0;
        m21 = 0;
        m22 = -2f * s;
        m23 = -(z2 + z1) * s;
        
        m30 = 0;
        m31 = 0;
        m32 = 0;
        m33 = 1;
    }
    
    /**
     * Puts this matrix (as 16 floats) into a {@link ByteBuffer} in column-major (OpenGL) order.
     * 
     * @param buf The buffer.
     */
    private void putBuffer(ByteBuffer buf)
    {
        putBuffer(buf.asFloatBuffer());
    }
    
    /**
     * Puts this matrix into a {@link FloatBuffer} in column-major (OpenGL) order.
     * 
     * @param buf The buffer.
     */
    private void putBuffer(FloatBuffer buf)
    {
        buf.put(m00); buf.put(m01); buf.put(m02); buf.put(m03);
        buf.put(m10); buf.put(m11); buf.put(m12); buf.put(m13);
        buf.put(m20); buf.put(m21); buf.put(m22); buf.put(m23);
        buf.put(m30); buf.put(m31); buf.put(m32); buf.put(m33);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Matrix4f))
        {
            return false;
        }
        
        Matrix4f m = (Matrix4f)o;
        
        return (m00 == m.m00 && m01 == m.m01 && m02 == m.m02 && m03 == m.m03
             && m10 == m.m10 && m11 == m.m11 && m12 == m.m12 && m13 == m.m13
             && m20 == m.m20 && m21 == m.m21 && m22 == m.m22 && m23 == m.m23
             && m30 == m.m30 && m31 == m.m31 && m32 == m.m32 && m33 == m.m33);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61 * hash + Float.floatToIntBits(m00);
        hash = 61 * hash + Float.floatToIntBits(m01);
        hash = 61 * hash + Float.floatToIntBits(m02);
        hash = 61 * hash + Float.floatToIntBits(m03);
        hash = 61 * hash + Float.floatToIntBits(m10);
        hash = 61 * hash + Float.floatToIntBits(m11);
        hash = 61 * hash + Float.floatToIntBits(m12);
        hash = 61 * hash + Float.floatToIntBits(m13);
        hash = 61 * hash + Float.floatToIntBits(m20);
        hash = 61 * hash + Float.floatToIntBits(m21);
        hash = 61 * hash + Float.floatToIntBits(m22);
        hash = 61 * hash + Float.floatToIntBits(m23);
        hash = 61 * hash + Float.floatToIntBits(m30);
        hash = 61 * hash + Float.floatToIntBits(m31);
        hash = 61 * hash + Float.floatToIntBits(m32);
        hash = 61 * hash + Float.floatToIntBits(m33);
        return hash;
    }
    
    /**
     * 
     * @return A {@link String} representation of this matrix.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        
        sb.append("[[");
        sb.append(m00).append(", ").append(m01).append(", ").append(m02).append(", ").append(m03).append("]\n [");
        sb.append(m10).append(", ").append(m11).append(", ").append(m12).append(", ").append(m13).append("]\n [");
        sb.append(m20).append(", ").append(m21).append(", ").append(m22).append(", ").append(m23).append("]\n [");
        sb.append(m30).append(", ").append(m31).append(", ").append(m32).append(", ").append(m33).append("]]");
        
        return sb.toString();
    }
}