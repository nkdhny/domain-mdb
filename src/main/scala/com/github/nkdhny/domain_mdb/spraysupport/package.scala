package com.github.nkdhny.domain_mdb

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import spray.json._
import com.github.nkdhny.domain_mdb._

import scala.collection.mutable

/**
 * Created by alexey on 18.12.14.
 */
package object spraysupport {

  private def valueFor(fieldValue: JsValue): Any = {
    fieldValue match {
      case nestedObj: JsObject                            => asDBObject(nestedObj)
      case array: JsArray                                 => MongoDBList(array.elements.map(valueFor): _*).underlying


      case number: JsNumber if number.value.isValidInt    => number.value.toInt
      case number: JsNumber if number.value.isValidLong   => number.value.toLong
      case number: JsNumber if number.value.isValidDouble => number.value.toDouble
      case number: JsNumber                               => number.value.toString().getBytes

      case string: JsString                               => string.value

      case JsTrue                                         => true
      case JsFalse                                        => false

      case JsNull                                         => null
    }
  }

  private def asDBObject(js: JsValue): DBObject = js match {

    case obj: JsObject =>
      val builder = MongoDBObject()
      for {
        (fieldName, fieldValue) <- obj.fields
      } {
        builder put(fieldName, valueFor(fieldValue))
      }
      builder

    case array: JsArray => MongoDBList(array.elements.map(valueFor): _*).underlying

    case _ => throw new IllegalStateException("Object expected")
  }

  private def valueOf(a: Any): JsValue = a match {
    case list: BasicDBList            => JsArray(list map valueOf toList)
    case obj: DBObject                => fromDBObject(obj)

    case id: ObjectId                 => JsString(id.toString)

    case int: Int                     => JsNumber(int)
    case long: Long                   => JsNumber(long)
    case float: Float                 => JsNumber(float)
    case double: Double               => JsNumber(double)
    case implicitNumber: Array[Byte]  => JsNumber(new String(implicitNumber))

    case string: String               => JsString(string)

    case bool: Boolean                => JsBoolean(bool)

    case null                         => JsNull

  }

  private def fromDBObject(o: DBObject): JsValue = o match {
    case list: BasicDBList => JsArray(list map valueOf toList)
    case obj: DBObject     =>
      val builder = mutable.ListBuffer[(String, JsValue)]()
      for {
        (fieldName, fieldValue) <- obj
      } {
        builder += (fieldName -> valueOf(fieldValue))
      }
      JsObject(builder.result())

    case _ => throw new IllegalStateException("Object expected")

  }

  implicit def fromJsonFormat[T: JsonFormat]: BsonFormat[T] = new BsonFormat[T] {
    override def write(t: T): DBObject = asDBObject(implicitly[JsonFormat[T]] write t)

    override def read(o: DBObject): T = implicitly[JsonFormat[T]] read fromDBObject(o)
  }

}
