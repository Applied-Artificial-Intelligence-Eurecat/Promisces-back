package org.eurecat.promisces.tools;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        if (stringList == null) {
            return "";
        }
        StringBuilder mystring = new StringBuilder();
        Iterator<String> iterator = stringList.iterator();
        while (iterator.hasNext()){
            mystring.append(iterator.next());
            if (iterator.hasNext()){
                mystring.append(";");
            }
        }
        return mystring.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string == null) {
            return emptyList();
        }
        return Arrays.asList(string.split(SPLIT_CHAR));
    }
}
