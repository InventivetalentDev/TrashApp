package org.inventivetalent.trashapp.common;

import java.lang.reflect.Array;

public class RotationBuffer {

    private static final byte COS             = (byte) 0;
    private static final byte SIN             = (byte) 1;
    private static final int  BUFFER_SIZE     = 10;
    public float[]           data            = new float[BUFFER_SIZE];
    private int               numberOfEntries = 0;
    public float[][]         unitVector      = ((float[][]) Array.newInstance(Float.TYPE, new int[]{BUFFER_SIZE, 3}));
    private int               head            = 0;
    private int               tail            = 0;

    public RotationBuffer() {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            this.unitVector[i][COS] = 0;
            this.unitVector[i][SIN] = 0;
            this.data[i] = 0;
        }
    }

    public void add(float toAdd) {
        if (this.head == BUFFER_SIZE) {
            this.head = 0;
        }
        this.data[this.head] = toAdd;
        this.unitVector[this.head][COS] = (float) Math.cos(toAdd);
        this.unitVector[this.head][SIN] = (float) Math.sin(toAdd);
        this.head++;
        if (this.numberOfEntries != BUFFER_SIZE) {
            this.numberOfEntries++;
        }
        if (this.head == this.tail) {
            this.tail++;
            if (this.tail == BUFFER_SIZE + 1) {
                this.tail = 1;
            }
        }
    }

    public float getAverageAzimuth() {
        double sumCos = 0.0d;
        double sumSin = 0.0d;
        for (int i = 0; i < this.numberOfEntries; i++) {
            sumCos += this.unitVector[i][COS];
            sumSin += this.unitVector[i][SIN];
        }
        double avgCos = sumCos / ((double) this.numberOfEntries);
        double avgSin = sumSin / ((double) this.numberOfEntries);
        float avgAzimuth = (float)(Math.round(2.0 * Math.toDegrees( Math.atan(avgSin / avgCos))) / 2.0);
        if (avgSin > 0.0d && avgCos > 0.0d) {
            return avgAzimuth;
        }
        if (avgSin >= 0.0d || avgCos <= 0.0d) {
            return avgAzimuth + 180.0f;
        }
        return avgAzimuth + 360.0f;
    }
}
