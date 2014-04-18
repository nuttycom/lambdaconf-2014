package asdf

import scalaz._
import scalaz.syntax.equal._
import scalaz.syntax.std.boolean._
import spire.math._

/**
 * Algebraic Structured Data Format
 */
sealed trait ASDF {
  import ASDF._

  def meta: Option[ASDF]
}

object ASDF {
  case class ABool private[ASDF] (b: Boolean, meta: Option[ASDF]) extends ASDF
  case class ANum  private[ASDF] (r: Real, meta: Option[ASDF]) extends ASDF
  case class AStr  private[ASDF] (s: String, meta: Option[ASDF]) extends ASDF

  case class ASeq  private[ASDF] (v: Vector[ASDF], meta: Option[ASDF]) extends ASDF
  object ASeq {
    def empty(meta: Option[ASDF] = None): ASDF = ASeq(Vector(), meta)
  }

  case class ADict private[ASDF] (data: Map[ADict.Key, ASDF], meta: Option[ASDF]) extends ASDF
  object ADict {
    def empty(meta: Option[ASDF] = None): ASDF = ADict(Map(), meta)

    case class Key private[ADict] (id: String) extends AnyVal

    /**
     * Restrictive constructor for keys; we will want to use dotted and 
     * bracketed path syntax for accessing values, so exclude these characters
     * from keys.
     */
    def key(id: String): Option[Key] = {
      (!id.matches(".*[.\\[\\]].*")).option(Key(id))
    }
  }

  def aBool(b: Boolean, meta: Option[ASDF] = None): ASDF = ABool(b, meta)

  def aNum(r: Real, meta: Option[ASDF] = None): ASDF = ANum(r, meta)

  def aStr(s: String, meta: Option[ASDF] = None): ASDF = AStr(s, meta)

  def aSeq(v: Vector[ASDF], meta: Option[ASDF] = None): ASDF = ASeq(v, meta)

  def aDict(data: Map[ADict.Key, ASDF], meta: Option[ASDF] = None): ASDF = ADict(data, meta)
}

