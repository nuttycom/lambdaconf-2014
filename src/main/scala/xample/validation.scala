package xample

import scalaz._
import scalaz.NonEmptyList._
import scalaz.std.function._
import scalaz.syntax.monad._
import scalaz.syntax.std.option._

case class IPAddr private (addr: String) extends AnyVal

object IPAddr {
  val pattern = """(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})""".r

  def parse(s: String): Option[IPAddr] = for {
    xs <- pattern.unapplySeq(s) if xs.forall(_.toInt <= 255)
  } yield IPAddr(s)
}

object v {
  sealed trait JValue
  type VPE[B] = Validation[NonEmptyList[ParseError], B]

  case class ParseError(msg: String)
  case class Host(name: String, ipAddr: IPAddr)

  def hostname(jv: JValue): VPE[String] = ???
  def ipField(jv: JValue): ParseError \/ String = ???

  def parseIP(addrStr: String): ParseError \/ IPAddr = 
    IPAddr.parse(addrStr) toRightDisjunction {
      ParseError(s"$addrStr is not a valid IP address")
    }

  def ipAddr(jv: JValue): VPE[IPAddr] = 
    (ipField(jv) >>= parseIP).validation.leftMap(nels(_)) 

  def host(jv: JValue) = 
    ^[VPE, String, IPAddr, Host](hostname(jv), ipAddr(jv)) { Host.apply _ }
}
  
