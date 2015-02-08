# Domain-mdb

Lightweight wrapper for [casbah](https://github.com/nkdhny/domain-mdb.git). It allows deal with domain objects in pretty same fashion as [spray-json](https://github.com/spray/spray-json) does.
I.e. with `BsonFormat` provided one could seamesly read and write objects to mongodb.

## Versions

### 0.3-SNAPSHOT

Dependencies updated, `scala` to `2.10.4`, `spray-json` to `1.3.1` and `casbah` to `2.8.0`

## Installation

Add following dependency to your `build.sbt` (scala `2.10` only for now)

```scala
libraryDependencies += "com.github.nkdhny"  %%  "domain-mdb"    % "0.2-SNAPSHOT"
```

If required add sonatype snapshots to resolvers

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
```

## Basic Example

Suppose we want to work  with simple object
```scala
case class Simple(payload: String)
```
We are to read and write instances of this onject to mongo db. Now we have to describe how this written to bson and read from it. We are doing it by  defining `BsonFormat`

```scala
object BsonFormat {
  implicit val format = new BsonFormat[Simple] {
	override def write(t: Simple): DBObject = {
	  MongoDBObject("p" -> t.payload)
	}

	override def read(o: Imports.DBObject): Simple = {
	  Simple(o.get("p").asInstanceOf[String])
	}
  }
}
```

Now we can work mongo without convertioning our object to `DBObject`

```scala
import BsonFormat._
import com.github.nkdhny.domain_mdb._ 

val db = MongoClient()("test")
val domainCollection = DomainMongoCollection[Simple](db("explicit_format"))

domainCollection.drop()
domainCollection.insert(Simple("first"))
domainCollection.insert(Simple("rest"))

val all = domainCollection.find().toList

val one = domainCollection.find(MongoDBObject("p"->"first")).one()
```

##Spray support
Library provides implicit conversions from `JsonFormat` defined in [spray-json](https://github.com/spray/spray-json) to `BsonFormat`. 
To build `BsonFormat` one have to bring implicit `Json-Format` into context and import `ru.nkdhny.mdb.spraysupport._`. See [specs](./src/test/scala/ru/nkdhny/mdb/DomainMongoCollectionTest.scala) for example.
