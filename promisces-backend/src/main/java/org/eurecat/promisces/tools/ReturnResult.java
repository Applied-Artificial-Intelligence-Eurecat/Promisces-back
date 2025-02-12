package org.eurecat.promisces.tools;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnResult<T> {
    private Integer totalRegisters;
    private Integer registersThisPage;

    private T data;
}
