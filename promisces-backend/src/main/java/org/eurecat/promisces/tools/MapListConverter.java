package org.eurecat.promisces.tools;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.util.*;

@Converter
public class MapListConverter implements AttributeConverter<Map<String, List<BigDecimal>>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, List<BigDecimal>> stringStringMap) {
        StringBuilder st = new StringBuilder();
        Iterator<String> it = stringStringMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            st.append(key);
            st.append("|");
            Iterator<BigDecimal> decIt = stringStringMap.get(key).iterator();
            while (decIt.hasNext()) {
                st.append(decIt.next().toString());
                if (decIt.hasNext()) {
                    st.append("$");
                }
            }
            if (it.hasNext()) {
                st.append(";");
            }
        }
        return st.toString();
    }

    @Override
    public Map<String, List<BigDecimal>> convertToEntityAttribute(String s) {
        Map<String, List<BigDecimal>> result = new HashMap<>();
        for (String row : s.split(";")) {
            if (row.isEmpty()) {
                break;
            }
            String key = row.split("\\|")[0];
            String value = row.split("\\|")[1];
            List<BigDecimal> decimalList = new LinkedList<>();
            try {
                for (String val : value.split("\\$")) {
                    decimalList.add(BigDecimal.valueOf(Float.parseFloat(val)));
                }
                result.put(key, decimalList);
            } catch (NumberFormatException e) {
                // Skip
            }
        }
        return result;
    }
}
