package org.eurecat.promisces.tools;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Collections.emptyList;

@Converter
public class DecimalListConverter implements AttributeConverter<List<BigDecimal>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<BigDecimal> decimalList) {
        StringBuilder st = new StringBuilder();
        Iterator<BigDecimal> it = decimalList.iterator();
        while (it.hasNext()) {
            BigDecimal number = it.next();
            st.append(number.toString());
            if (it.hasNext()){
                st.append(SPLIT_CHAR);
            }
        }
        return st.toString();
    }

    @Override
    public List<BigDecimal> convertToEntityAttribute(String s) {
        List<BigDecimal> result = new LinkedList<>();
        for (String row : s.split(";")){
            result.add(BigDecimal.valueOf(Double.parseDouble(row)));
        }
        return result;
    }
}
