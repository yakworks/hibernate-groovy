misc helpers for hibernate, all require groovy.

## hibernate-groovy-proxy

includes the dependency.

## yakworks.hibernate.h2.ExtendedH2Dialect

registers columns types (used for json types)

- Types.OTHER -> "json"
- Types.VARBINARY -> "BLOB"

## yakworks.hibernate.schema.SimpleSchemaManagementTool

Filters out the foreign key generation when db is created. 
set `hibernate.schema_management_tool: 'yakworks.hibernate.schema.SimpleSchemaManagementTool'`

# JsonType

make its easier to set the JsonType in Gorm mapping blocks. 
you will need to add the appropriate dependency to your project along with the . 
see https://github.com/vladmihalcea/hibernate-types
