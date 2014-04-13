package fperfect

import scalaz._
import scalaz.syntax.equal._
import spire.math._

/**
 * Algebraic Structured Data Format
 */
sealed trait ASDF {
  def meta: Option[ASDF]
}

object ASDF {
  case class ABool private[ASDF] (b: Boolean, meta: Option[ASDF]) extends ASDF
  case class ANum private[ASDF] (r: Real, meta: Option[ASDF]) extends ASDF
  case class AStr private[ASDF] (s: String, meta: Option[ASDF]) extends ASDF
  case class ASeq private[ASDF] (v: Vector[ASDF], meta: Option[ASDF]) extends ASDF
  case class ADict private[ASDF] (dict: Map[String, ADict.Value], meta: Option[ASDF]) extends ASDF 

  object ADict {
    class DictBuilder private[ADict] (meta: Option[ASDF]) {
      def of(kvs: (Key, ASDF)*): ADict = 
        new ADict(kvs.map({ case (Key(id, kmeta), v) => (id, Value(kmeta, v)) }).toMap, meta)
    }

    def of(kvs: (Key, ASDF)*): ADict = new DictBuilder(None).of(kvs: _*)
    def withMetadata(meta: ASDF) = new DictBuilder(Some(meta))

    case class Key private[ADict] (id: String, meta: Option[ASDF] = None)
    case class Value private[ADict] (kmeta: Option[ASDF], data: ASDF)
  }

  def aBool(b: Boolean, meta: Option[ASDF] = None): ASDF = new ABool(b, meta)
  def aNum(r: Real, meta: Option[ASDF] = None): ASDF = new ANum(r, meta)
  def aStr(s: String, meta: Option[ASDF] = None): ASDF = new AStr(s, meta)
  def aSeq(v: Vector[ASDF], meta: Option[ASDF] = None): ASDF = new ASeq(v, meta)
  def aDict(meta: Option[ASDF], kvs: (ADict.Key, ASDF)*) = meta.map(ADict.withMetadata(_).of(kvs: _*)).getOrElse(ADict.of(kvs: _*))

  sealed trait PathElement
  case class Root extends PathElement
  case class Field(name: String) extends PathElement
  case class Index(i: Int) extends PathElement

  case class Path(elements: List[PathElement]) extends AnyVal
}

