package util;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class ArxUtil {

    public static void setDataAttributeTypes(Data data, List<String> columnNames) {
        data.getDefinition().setAttributeType(columnNames.get(0), AttributeType.INSENSITIVE_ATTRIBUTE);
        for (int i = 1; i < columnNames.size(); i++) {
            data.getDefinition().setAttributeType(columnNames.get(i), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        }
    }

    public static void setDataHierarchies(Data data, List<String> columnNames, List<String> hierarchiesFilepath) throws IOException {
        for (int i = 1; i < columnNames.size(); i++) {
            AttributeType.Hierarchy h = AttributeType.Hierarchy.create(hierarchiesFilepath.get(i - 1), Charset.defaultCharset(), ';');
            data.getDefinition().setAttributeType(columnNames.get(i), h);
        }
    }

}
