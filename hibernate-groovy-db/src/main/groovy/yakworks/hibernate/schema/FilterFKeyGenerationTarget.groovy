/*
* Copyright 2021 original authors
* SPDX-License-Identifier: Apache-2.0
*/
package yakworks.hibernate.schema

import groovy.transform.CompileStatic

import org.hibernate.resource.transaction.spi.DdlTransactionIsolator
import org.hibernate.tool.schema.internal.exec.GenerationTargetToDatabase

/**
 * Filters out the foreign key generation
 */
@CompileStatic
class FilterFKeyGenerationTarget extends GenerationTargetToDatabase {

    FilterFKeyGenerationTarget(DdlTransactionIsolator ddlTransactionIsolator, boolean releaseAfterUse) {
        super(ddlTransactionIsolator, releaseAfterUse)
    }

    @Override
    void accept(String command) {
        if (shouldAccept(command)) super.accept(command)
    }

    //this is where we filter out the foreign key
    boolean shouldAccept(String command) {
        // Custom filtering logic here, e.g.:
        //if (command =~ /references legacy\.xyz/)
        //don't generate all foreign keys
        // println command
        return !(command =~ /foreign key/)
    }
}
