package ru.nkdhny.mdb

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{Imports, MongoClient}
import org.specs2.mutable._
import ru.nkdhny.mdb._

/**
 * Created by alexey on 19.12.14.
 */
class DomainMongoCollectionTest extends Specification {

  object WithoutSpraySupport {
    case class Simple(payload: String)

    implicit val format = new BsonFormat[Simple] {
      override def write(t: Simple): Imports.DBObject = MongoDBObject("p" -> t.payload)

      override def read(o: Imports.DBObject): Simple = Simple(o.get("p").asInstanceOf[String])
    }
  }

  object WithSpraySupport {
    import spray.json._

    case class Simple(payload: String)


    object JsonFormat extends DefaultJsonProtocol {
      implicit val simpleFormat = jsonFormat1(Simple)
    }
  }

  "Domain objects" should {
    val mdb = MongoClient()("test")
    "accept io according to a format provided" in {
      import WithoutSpraySupport._

      val domainCollection = DomainMongoCollection[Simple](mdb("explicit_format"))

      domainCollection.drop()
      domainCollection.insert(Simple("first"))
      domainCollection.insert(Simple("rest"))

      val all = domainCollection.find().toList

      all must beEqualTo(Simple("first")::Simple("rest")::Nil)

      val one = domainCollection.find(MongoDBObject("p"->"first")).one()
      one must beEqualTo(Simple("first"))
    }

    "accept io according to a format derived from json format" in {
      import WithSpraySupport._
      import JsonFormat._
      import spraysupport._

      val domainCollection = DomainMongoCollection[Simple](mdb("implicit_format"))

      domainCollection.drop()
      domainCollection.insert(Simple("first"))
      domainCollection.insert(Simple("rest"))

      val all = domainCollection.find().toList

      all must beEqualTo(Simple("first")::Simple("rest")::Nil)

      val one = domainCollection.find(MongoDBObject("payload"->"first")).one()
      one must beEqualTo(Simple("first"))


    }
  }
}
