package com.jpmc.ecm.mcp.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building JSON schemas for MCP tool input parameters.
 * Provides a fluent API for schema construction.
 * 
 * Example:
 * <pre>
 * JsonNode schema = JsonSchemaBuilder.object()
 *     .property("endpoint", JsonSchemaBuilder.string()
 *         .description("API endpoint path")
 *         .required(true))
 *     .property("method", JsonSchemaBuilder.string()
 *         .enumValues("GET", "POST", "PUT", "DELETE")
 *         .required(true))
 *     .build();
 * </pre>
 */
public class JsonSchemaBuilder {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final ObjectNode schema;
    private final List<String> requiredFields;
    
    private JsonSchemaBuilder(String type) {
        this.schema = MAPPER.createObjectNode();
        this.requiredFields = new ArrayList<>();
        schema.put("type", type);
    }
    
    /**
     * Create a schema for an object type
     */
    public static JsonSchemaBuilder object() {
        return new JsonSchemaBuilder("object");
    }
    
    /**
     * Create a schema for a string type
     */
    public static JsonSchemaBuilder string() {
        return new JsonSchemaBuilder("string");
    }
    
    /**
     * Create a schema for a number type
     */
    public static JsonSchemaBuilder number() {
        return new JsonSchemaBuilder("number");
    }
    
    /**
     * Create a schema for an integer type
     */
    public static JsonSchemaBuilder integer() {
        return new JsonSchemaBuilder("integer");
    }
    
    /**
     * Create a schema for a boolean type
     */
    public static JsonSchemaBuilder bool() {
        return new JsonSchemaBuilder("boolean");
    }
    
    /**
     * Create a schema for an array type
     */
    public static JsonSchemaBuilder array() {
        return new JsonSchemaBuilder("array");
    }
    
    /**
     * Set the description for this schema
     */
    public JsonSchemaBuilder description(String description) {
        schema.put("description", description);
        return this;
    }
    
    /**
     * Mark this field as required (for use in object properties)
     */
    public JsonSchemaBuilder required(boolean required) {
        if (required && schema.has("title")) {
            // Title is used as property name in parent object
            requiredFields.add(schema.get("title").asText());
        }
        return this;
    }
    
    /**
     * Add a property to an object schema
     */
    public JsonSchemaBuilder property(String name, JsonSchemaBuilder propertySchema) {
        if (!schema.has("properties")) {
            schema.set("properties", MAPPER.createObjectNode());
        }
        
        ObjectNode properties = (ObjectNode) schema.get("properties");
        JsonNode propSchema = propertySchema.build();
        
        // Check if property is required
        if (propertySchema.requiredFields.contains(name) || 
            (propSchema.has("required") && propSchema.get("required").asBoolean())) {
            requiredFields.add(name);
        }
        
        properties.set(name, propSchema);
        return this;
    }
    
    /**
     * Set enum values for a string schema
     */
    public JsonSchemaBuilder enumValues(String... values) {
        ArrayNode enumArray = MAPPER.createArrayNode();
        for (String value : values) {
            enumArray.add(value);
        }
        schema.set("enum", enumArray);
        return this;
    }
    
    /**
     * Set the item type for an array schema
     */
    public JsonSchemaBuilder items(JsonSchemaBuilder itemSchema) {
        schema.set("items", itemSchema.build());
        return this;
    }
    
    /**
     * Set minimum value for number/integer
     */
    public JsonSchemaBuilder minimum(int min) {
        schema.put("minimum", min);
        return this;
    }
    
    /**
     * Set maximum value for number/integer
     */
    public JsonSchemaBuilder maximum(int max) {
        schema.put("maximum", max);
        return this;
    }
    
    /**
     * Set pattern for string validation
     */
    public JsonSchemaBuilder pattern(String regex) {
        schema.put("pattern", regex);
        return this;
    }
    
    /**
     * Set default value
     */
    public JsonSchemaBuilder defaultValue(Object value) {
        schema.putPOJO("default", value);
        return this;
    }
    
    /**
     * Build the final JSON schema
     */
    public JsonNode build() {
        // Add required array if we have required fields
        if (!requiredFields.isEmpty()) {
            ArrayNode required = MAPPER.createArrayNode();
            requiredFields.forEach(required::add);
            schema.set("required", required);
        }
        return schema;
    }
}
