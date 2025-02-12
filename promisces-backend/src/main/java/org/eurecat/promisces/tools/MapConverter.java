package org.eurecat.promisces.tools;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Converter
public class MapConverter implements AttributeConverter<Map<String, BigDecimal>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, BigDecimal> stringStringMap) {
        StringBuilder st = new StringBuilder();
        Iterator<String> it = stringStringMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            st.append(key);
            st.append("|");
            st.append(stringStringMap.get(key).toString());
            if (it.hasNext()){
                st.append(";");
            }
        }
        return st.toString();
    }

    @Override
    public Map<String, BigDecimal> convertToEntityAttribute(String s) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (String row : s.split(";")){
            if (row.isEmpty()) {
                break;
            }
            String key = row.split("\\|")[0];
            String value = row.split("\\|")[1];
            try {
                result.put(key, BigDecimal.valueOf(Float.parseFloat(value)));
            } catch (NumberFormatException e) {
                // Skip
            }
        }
        return result;
    }
}
