# cnes
Cloud Native EventStore - The event store for containers

## Containers for writing and reading the data
Once the storing of data via CNES starts, the schema may change over time.
There are many reasons for that and CNES will be able to handle that.

If you write data to a CNES with a certain schema, version 1.0, you keep doing
this until there is the need to change this schema. It may get extended, shrinked
or some other way changed.
As data in an event store is immutable by design, one can't change or update the 
existing schema anyhow.
So you bring up a new version of the CNES container and keep on writing data with 
the updated schema, version 2.0.

Once it comes to reading and processing the data, there must be an appropriate functionality
to read back the data. This requires a matching container, that is able to read and process
the data.
When reading older data then written with CNES version 2.0, you have to start a container
that is able to read and process data written with CNES version 1.0.
