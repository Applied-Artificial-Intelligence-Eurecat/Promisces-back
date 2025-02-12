package org.eurecat.promisces.substances;

public class AverageReturn {
    public String itemName;
    public float p;
    public float m;
    public float k;
    public int numberOfSubstances;

    public AverageReturn(String itemName, float p, float m, float k, int numberOfSubstances) {
        this.itemName = itemName;
        this.p = p;
        this.m = m;
        this.k = k;
        this.numberOfSubstances = numberOfSubstances;
    }
}
