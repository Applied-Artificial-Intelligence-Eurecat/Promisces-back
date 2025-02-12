package org.eurecat.promisces.substances;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityReturn {
    public String name;
    public float p;
    public float m;
    public float t;
    public float k;
    public float likeness;

    public SimilarityReturn(String n, float s) {
        this.name = n;
        this.likeness = s;
        this.p = 0.0F;
        this.m = 0.0F;
        this.t = 0.0F;
        this.k = 0.0F;
    }
}
