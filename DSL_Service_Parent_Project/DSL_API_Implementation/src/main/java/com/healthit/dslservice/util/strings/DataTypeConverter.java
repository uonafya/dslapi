package com.healthit.dslservice.util.strings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author duncan
 */
public class DataTypeConverter {

    public static String getJSONFromObject( Object inputMap) {
        Gson gson = new Gson();
        String json = gson.toJson(inputMap);
        return json;
    }
}
