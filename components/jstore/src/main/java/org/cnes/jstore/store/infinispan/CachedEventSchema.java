package org.cnes.jstore.store.infinispan;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = { CachedEvent.class }, schemaPackageName = "jstore")
public interface CachedEventSchema extends GeneratedSchema {

}
