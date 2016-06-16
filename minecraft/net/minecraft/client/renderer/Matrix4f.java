package net.minecraft.client.renderer;

public class Matrix4f extends org.lwjgl.util.vector.Matrix4f
{
    public Matrix4f(float[] matrix)
    {
        this.m00 = matrix[0];
        this.m01 = matrix[1];
        this.m02 = matrix[2];
        this.m03 = matrix[3];
        this.m10 = matrix[4];
        this.m11 = matrix[5];
        this.m12 = matrix[6];
        this.m13 = matrix[7];
        this.m20 = matrix[8];
        this.m21 = matrix[9];
        this.m22 = matrix[10];
        this.m23 = matrix[11];
        this.m30 = matrix[12];
        this.m31 = matrix[13];
        this.m32 = matrix[14];
        this.m33 = matrix[15];
    }

    public Matrix4f()
    {
        this.m00 = this.m01 = this.m02 = this.m03 = this.m10 = this.m11 = this.m12 = this.m13 = this.m20 = this.m21 = this.m22 = this.m23 = this.m30 = this.m31 = this.m32 = this.m33 = 0.0F;
    }
}
