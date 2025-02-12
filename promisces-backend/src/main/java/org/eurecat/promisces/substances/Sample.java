package org.eurecat.promisces.substances;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sample")
public class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String sampleMatrix;
    private String sampleMatrixType;

    private BigDecimal concentration;

    private Date samplingDate;

    private BigDecimal limitOfDetection;
    private BigDecimal limitOfQuantification;

    private String stationName;
}
