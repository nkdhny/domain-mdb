package ru.nkdhny

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{MongoCollection, MongoCollectionBase}
import com.mongodb.{DBCollection, DBCursor}

/**
 * Created by alexey on 10.07.14.
 */
package object mdb {

  case class ReadException (msg: String) extends Exception
  case class WriteException(msg: String) extends Exception

  trait BsonFormat[T] {
    def write(t: T): DBObject
    def read(o: DBObject): T
  }

  implicit def fromJsonDbView[T: BsonFormat]: T => DBObject = t => implicitly[BsonFormat[T]] write t

  class DomainCursor[D: BsonFormat](val underlying: DBCursor) extends Iterator[D] {
    type T = DBObject

    override def size: Int = underlying.size

    def one(): D = implicitly[BsonFormat[D]] read underlying.one()

    override def hasNext: Boolean = {
      underlying.hasNext
    }

    override def next(): D = {
      implicitly[BsonFormat[D]] read underlying.next
    }
  }

  object DomainMongoCollection {
    def apply[D: BsonFormat](mongoCollection: MongoCollection): DomainMongoCollection[D] = {
      new DomainMongoCollection[D](mongoCollection.underlying)
    }
  }

  class DomainMongoCollection[D: BsonFormat](val underlying: DBCollection) extends MongoCollectionBase {
    override type T = DBObject
    override type CursorType = DomainCursor[D]

    implicit val objView: D => DBObject = implicitly[BsonFormat[D]].write

    override def _newCursor(cursor: DBCursor): CursorType = new DomainCursor[D](cursor)
    override def _newInstance(collection: DBCollection): MongoCollectionBase = new DomainMongoCollection[D](underlying)
  }


}
