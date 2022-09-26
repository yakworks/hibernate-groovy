/*
* Copyright 2021 original authors
* SPDX-License-Identifier: Apache-2.0
*/
package yakworks.hibernate.schema

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.hibernate.boot.Metadata
import org.hibernate.boot.registry.selector.spi.StrategySelector
import org.hibernate.cfg.AvailableSettings
import org.hibernate.tool.schema.internal.DefaultSchemaFilterProvider
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool
import org.hibernate.tool.schema.internal.SchemaCreatorImpl
import org.hibernate.tool.schema.internal.SchemaDropperImpl
import org.hibernate.tool.schema.internal.exec.GenerationTarget
import org.hibernate.tool.schema.internal.exec.JdbcContext
import org.hibernate.tool.schema.spi.ExecutionOptions
import org.hibernate.tool.schema.spi.SchemaCreator
import org.hibernate.tool.schema.spi.SchemaDropper
import org.hibernate.tool.schema.spi.SchemaFilter
import org.hibernate.tool.schema.spi.SchemaFilterProvider
import org.hibernate.tool.schema.spi.SourceDescriptor
import org.hibernate.tool.schema.spi.TargetDescriptor

/**
 * Taken from this stackover flow so we can filter out ddl.
 * We do this to filter out the foreign keys which we consider a big nuisance and performance hit
 * when dealing with gorm. We normally hand select the ones that dont hurt batch inserts or deletes using liquibase.
 * but for testing we don't want any.
 *
 * This is set in application.yml with
 * hibernate:
 *   schema_management_tool: 'yakworks.rally.ddl.CustomSchemaManagementTool'
 *
 */
@CompileStatic
class SimpleSchemaManagementTool extends HibernateSchemaManagementTool {
    @Override
    SchemaCreator getSchemaCreator(Map options) {
        return new CustomSchemaCreator(this, getSchemaFilterProvider(options).getCreateFilter())
    }

    @Override
    SchemaDropper getSchemaDropper(Map options) {
        return new CustomSchemaDropper(this, getSchemaFilterProvider(options).getDropFilter())
    }

    // We unfortunately have to copy this private method from HibernateSchemaManagementTool
    @CompileDynamic
    private SchemaFilterProvider getSchemaFilterProvider(Map options) {
        // super.getSchemaFilterProvider(options)
        final Object configuredOption = (options == null) ? null : options.get(AvailableSettings.HBM2DDL_FILTER_PROVIDER)
        return serviceRegistry.getService(StrategySelector).resolveDefaultableStrategy(
            SchemaFilterProvider, configuredOption, DefaultSchemaFilterProvider.INSTANCE
        )
    }

    // only overriding so we can intercept and add our CustomGenerationTarget
    @CompileStatic
    static class CustomSchemaCreator extends SchemaCreatorImpl {
        private final HibernateSchemaManagementTool tool

        CustomSchemaCreator(HibernateSchemaManagementTool tool, SchemaFilter schemaFilter) {
            super(tool, schemaFilter)
            this.tool = tool
        }

        @Override
        void doCreation(Metadata metadata, ExecutionOptions options, SourceDescriptor sourceDescriptor, TargetDescriptor targetDescriptor) {
            final JdbcContext jdbcContext = tool.resolveJdbcContext( options.getConfigurationValues() )
            final GenerationTarget[] targets = new GenerationTarget[ targetDescriptor.getTargetTypes().size() ]
            //replaces the normal one at index 0.
            targets[0] = new FilterFKeyGenerationTarget(tool.getDdlTransactionIsolator(jdbcContext), true)
            super.doCreation(metadata, jdbcContext.getDialect(), options, sourceDescriptor, targets)
        }
    }

    // same concept as SchemaCreator
    @CompileStatic
    static class CustomSchemaDropper extends SchemaDropperImpl {
        private final HibernateSchemaManagementTool tool

        CustomSchemaDropper(HibernateSchemaManagementTool tool, SchemaFilter schemaFilter) {
            super(tool, schemaFilter)
            this.tool = tool
        }

        @Override
        void doDrop(Metadata metadata, ExecutionOptions options, SourceDescriptor sourceDescriptor, TargetDescriptor targetDescriptor) {
            final JdbcContext jdbcContext = tool.resolveJdbcContext( options.getConfigurationValues() )
            final GenerationTarget[] targets = new GenerationTarget[ targetDescriptor.getTargetTypes().size() ]
            //replaces the normal one at index 0.
            targets[0] = new FilterFKeyGenerationTarget(tool.getDdlTransactionIsolator(jdbcContext), true)
            super.doDrop(metadata, options, jdbcContext.getDialect(), sourceDescriptor, targets)
        }
    }
}
