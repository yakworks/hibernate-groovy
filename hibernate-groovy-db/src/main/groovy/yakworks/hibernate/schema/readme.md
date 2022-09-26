Taken from this stackover flow so we can filter out ddl. 
We do this to filter out the foreign keys which can be a big performance hit
Best practice is to hand select the ones that dont hurt batch inserts or deletes using liquibase or Flyaway.
This is primarily for test and makes it easier to add a remove data without worrying about databse constraints.

This is set in application.yml with
hibernate.schema_management_tool: 'gorm.tools.hibernate.schema.SimpleSchemaManagementTool'
