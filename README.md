# CNES
Cloud Native Event-Store - The event store for containers

## A brief introduction to CNES

In several use cases it DOES matter in which sequence data is coming in. 
To do this with a RDBMS is possible, but causing some effort.
If one stores the data in exact the sequence it is entering the systems, may
come in handy.

Aside from the costs for RDBMS licences and special trained staff, it requires some
strong hardware, too.
What if you could use your "no-longer-brand-new-hardware" to store your data on it?
In times when sustainability becomes more and more important and it is difficult to
get new hardware at all, this comes as an great advantage.

As RDBMS require schemas, the effort of migrating these schemas is not negligible, when
one has to change it for whatever reason.
Once the storing of data via CNES starts, the schema may change over time.
CNES will be able to handle that in an easy way. No problem.

If you write data to a CNES with a certain schema, e.g. version 1.0, until there is the
need to change this schema. It may get extended, shrinked or changed in some way.
As data in an event-store is immutable by design, one can't change or update the 
existing schema anyhow. 
So, how can you than migrate the schemas and the contained data?
You bring up just a new version of the CNES container and keep on writing data with 
the updated schema, version 2.0.

Once it comes to reading and processing the data, there will be an appropriate functionality
to read the data. This requires a matching container, that is able to read and process
the data.
When reading older data then written with CNES version 2.0, you have to start a container
that is able to read and process data written with CNES version 1.0.

Sounds easy? - It is as simple as that. As long as you still have the former versions of the
reading containers.

An other great advantage is the event-store may store all its data in ASCII or in any other
simple text format. So the chance it will be readable after decades is quite good.
Especially if one has to store data, that might be read again after 20, 30, 40 years, it is 
an advantage, if the format is still readable or may converted easily in an actual format.

As a picture tells more than 1,000 words, here is a picture ![Write to an Event Store](https://github.com/Javatar81/cnes/blob/c1e7ccc7a0e55d3db78eb8adba4bce218895ef7d/EventStore_write.png)
