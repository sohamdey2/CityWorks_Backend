package com.cts.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public class StrictLongDeserializer extends JsonDeserializer<Long>{

	@Override
	public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
		String value = p.getText().trim();
		if (value.isEmpty()) {
			throw new InvalidFormatException(p, "workOrderId cannot be empty string", value, Long.class);
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ex) {
			throw new InvalidFormatException(p, "must be a valid number", value, Long.class);
		}
		
	}

}
