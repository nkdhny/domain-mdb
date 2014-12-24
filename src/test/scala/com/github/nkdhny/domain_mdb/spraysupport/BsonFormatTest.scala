package com.github.nkdhny.domain_mdb.spraysupport

import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import org.specs2.mutable._
import spray.json._
import com.github.nkdhny.domain_mdb._
import spraysupport._

/**
 * Created by alexey on 19.12.14.
 */
class BsonFormatTest extends Specification {

  object NumberTests {
    case class NumberHolder(n: BigDecimal)
    object JsonFormat extends DefaultJsonProtocol {
      implicit val numberFormat = jsonFormat1(NumberHolder)
    }
  }

  object StringTests {
    case class StringHolder(s: String)
    object JsonFormat extends DefaultJsonProtocol {
      implicit val stringFormat = jsonFormat1(StringHolder)
    }
  }

  object BooleanTests {
    case class BooleanHolder(b: Boolean)
    object JsonFormat extends DefaultJsonProtocol {
      implicit val booleanFormat = jsonFormat1(BooleanHolder)
    }
  }
  
  object ComplexObjectTest {
    case class Simple(field: String)
    case class Complex(field: String, simple: Simple, list: List[String])
    case class MostComplex(field: String, complex: Complex, list: List[Simple])
    
    object JsonFormat extends DefaultJsonProtocol {
      implicit val simpleFormat = jsonFormat1(Simple)
      implicit val complexFormat = jsonFormat3(Complex)
      implicit val mostComplexFormat = jsonFormat3(MostComplex)
    }


  }

  object NullTests {
    object JsonFormat extends DefaultJsonProtocol {}
  }

  "Bson format" should {
    "accept null" in {
      import NullTests._
      import JsonFormat._

      val o = JsObject("f" -> JsNull)
      val d = fromJsonFormat[JsObject].write(o)

      d must beEqualTo(MongoDBObject("f" -> null))
      o must beEqualTo(fromJsonFormat[JsObject].read(d))
    }
    "accept string" in {
      import StringTests._
      import JsonFormat._

      val s = StringHolder("this is a string")
      val d = fromJsonFormat[StringHolder].write(s)

      d must beEqualTo(MongoDBObject("s" -> "this is a string"))

      val sAgain = fromJsonFormat[StringHolder].read(d)

      s must beEqualTo(sAgain)
    }

    "accept boolean" in {
      import BooleanTests._
      import JsonFormat._

      "when value is true" in {
        val s = BooleanHolder(true)
        val d = fromJsonFormat[BooleanHolder].write(s)

        d must beEqualTo(MongoDBObject("b" -> true))

        val sAgain = fromJsonFormat[BooleanHolder].read(d)

        s must beEqualTo(sAgain)
      }


      "when value is false" in {
        val s = BooleanHolder(false)
        val d = fromJsonFormat[BooleanHolder].write(s)

        d must beEqualTo(MongoDBObject("b" -> false))

        val sAgain = fromJsonFormat[BooleanHolder].read(d)

        s must beEqualTo(sAgain)
      }
    }

    "accept numeric types" in {
      import NumberTests._
      import JsonFormat._

      "when number is a valid integer" in {
        val n = NumberHolder(1)
        val d = fromJsonFormat[NumberHolder].write(n)

        d must beEqualTo(MongoDBObject("n" -> 1))

        val nAgain = fromJsonFormat[NumberHolder].read(d)

        n must beEqualTo(nAgain)
      }

      "when number is a valid long" in {
        val n = NumberHolder(Int.MaxValue.toLong + 1L)
        val d = fromJsonFormat[NumberHolder].write(n)

        d must beEqualTo(MongoDBObject("n" -> (Int.MaxValue.toLong + 1L)))

        val nAgain = fromJsonFormat[NumberHolder].read(d)

        n must beEqualTo(nAgain)
      }
      
      "when number is a valid double" in {
        val n = NumberHolder(0.25)
        val d = fromJsonFormat[NumberHolder].write(n)

        d must beEqualTo(MongoDBObject("n" -> 0.25))

        val nAgain = fromJsonFormat[NumberHolder].read(d)

        n must beEqualTo(nAgain)
      }

      "when number is a bigdecimal" in {
        val n = NumberHolder(0.1)
        val d = fromJsonFormat[NumberHolder].write(n)

        d must beEqualTo(MongoDBObject("n" -> BigDecimal(0.1).toString().getBytes))

        val nAgain = fromJsonFormat[NumberHolder].read(d)

        n must beEqualTo(nAgain)

      }


    }
    
    "accept a complex object" in {

      import ComplexObjectTest._
      import JsonFormat._

      "with empty list as a field" in {
        val c = Complex("field", Simple("nested field"), Nil)
        val d = fromJsonFormat[Complex].write(c)

        d must beEqualTo(MongoDBObject("field" -> "field", "simple" -> MongoDBObject("field" -> "nested field"), "list" -> MongoDBList()))
        fromJsonFormat[Complex].read(d) must beEqualTo(c)
      }

      "with list of primitive type as a field" in {
        val c = Complex("field", Simple("nested field"), "first"::"rest"::Nil)
        val d = fromJsonFormat[Complex].write(c)

        d must beEqualTo(MongoDBObject("field" -> "field", "simple" -> MongoDBObject("field" -> "nested field"), "list" -> MongoDBList("first", "rest")))
        fromJsonFormat[Complex].read(d) must beEqualTo(c)

      }

      "with a nested object" in {
        val c = MostComplex("field", Complex("nested", Simple("nested"), Nil), Nil)

        val d = fromJsonFormat[MostComplex].write(c)

        d must beEqualTo(
          MongoDBObject(
            "field"     -> "field",
            "complex"   -> MongoDBObject(
              "field"   -> "nested",
              "simple"  -> MongoDBObject(
                "field" -> "nested"
              ),
              "list"    -> MongoDBList.empty
            ),
            "list"      -> MongoDBList.empty
          )
        )
        fromJsonFormat[MostComplex].read(d) must beEqualTo(c)

      }

      "with a list of objects as a field" in {
        val c = MostComplex("field", Complex("nested", Simple("nested"), Nil), Simple("first"):: Simple("rest")::Nil)

        val d = fromJsonFormat[MostComplex].write(c)

        d must beEqualTo(
          MongoDBObject(
            "field"     -> "field",
            "complex"   -> MongoDBObject(
              "field"   -> "nested",
              "simple"  -> MongoDBObject(
                "field" -> "nested"
              ),
              "list"    -> MongoDBList.empty
            ),
            "list"      -> MongoDBList(MongoDBObject("field" -> "first"), MongoDBObject("field" -> "rest"))
          )
        )
        fromJsonFormat[MostComplex].read(d) must beEqualTo(c)

      }

      "with a list of primitive type as an object" in {
        val l = "first"::"rest"::Nil
        val d = fromJsonFormat[List[String]].write(l)

        d must beEqualTo(MongoDBList("first", "rest").underlying)
        fromJsonFormat[List[String]].read(d) must beEqualTo(l)
      }

      "with a list of objects as an object" in {
        val l = Simple("first")::Simple("rest")::Nil
        val d = fromJsonFormat[List[Simple]].write(l)

        d must beEqualTo(MongoDBList(MongoDBObject("field"->"first"), MongoDBObject("field"->"rest")).underlying)
        fromJsonFormat[List[Simple]].read(d) must beEqualTo(l)
      }

      "with a list of lists" in {
        val l = ("first"::"rest"::Nil)::Nil
        val d = fromJsonFormat[List[List[String]]].write(l)

        d must beEqualTo(MongoDBList(MongoDBList("first", "rest").underlying).underlying)
        fromJsonFormat[List[List[String]]].read(d) must beEqualTo(l)

      }

    }
  }


}
