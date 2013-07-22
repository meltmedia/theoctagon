package com.meltmedia.serialize;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jheun
 * Date: 6/26/13
 */
public class DoNotSerialize extends JsonSerializer<String> {

  @Override
  public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

    // Do Nothing
    jgen.writeNull();

  }

}
