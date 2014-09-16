/**
 * Created by ant on 16.09.14.
 */

package scalaromandie

import akka.actor._
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import java.net.InetSocketAddress
import akka.util.ByteString
import akka.io.Tcp.Connected
import akka.io.Tcp.Register
import akka.io.Tcp.Connect
import akka.io.Tcp.CommandFailed

case class EmailAddress(address: String, displayName: Option[String] = None)

case class EmailMessage(from: EmailAddress,
                        recipients: Seq[EmailAddress], cc: Seq[EmailAddress], bcc: Seq[EmailAddress],
                        subject: String, body: String)


class SmtpActor(server: String, port: Int, m: EmailMessage) extends Actor {

  import context.system

  val manager = IO(Tcp)

  manager ! Connect(new InetSocketAddress(server, port))

  def receive = {


    case CommandFailed(_: Connect) =>
      //      listener ! "connect failed"
      context stop self

    case c@Connected(remote, local) =>
      //      listener ! c
      println("connected")
      val connection = sender()
      connection ! Register(self)

      connection ! Write(serialize(m))
      context become {

        case CommandFailed(w: Write) =>
          // O/S buffer was full

          println("write failed")
        case Received(data) =>
          //          listener ! data
          println("#" + data.decodeString("UTF-8"))

        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          //          listener ! "connection closed"
          println ("connection closed")
          context stop self
      }
    case x => println(x)
  }

  def serialize(m: EmailMessage): ByteString = {
    val s: String =
      s"""HELO toto\r
         |MAIL FROM: ${m.from.address}\r
         |RCPT TO:  ${m.recipients.head.address}\r
         |DATA\r
         |Subject: ${m.subject}\r
         |\r
         |Hi\r
         |.\r
       """.stripMargin
//    val s="HELO client\r\n"
    ByteString(s.getBytes("UTF-8"))
  }

}

object test extends App {

  import org.subethamail.wiser.Wiser

  def buildWiser() = new Wiser() {
    setPort(2500)
  }

  var wiser = buildWiser


  wiser.start

  val system = ActorSystem()
  var m = EmailMessage(EmailAddress("toto@titi.org"), List(EmailAddress("tata@titi.org")), Nil, Nil, "sujet", "")
  val ac = system.actorOf(Props(classOf[SmtpActor], "localhost", 2500, m))


  //  wiser.stop


  //  "Email service" should {
  //    "send a simple email " in {
  //      emailservice.sendEmail("sender@example.com", List("recipient@example.com"), "subject", "body")
  //      Thread.sleep(500)
  //      wiser.getMessages().size() must equalTo(1)
  //      val msg = wiser.getMessages().get(0)
  //      msg.getEnvelopeSender() must equalTo("sender@example.com")
  //      msg.getEnvelopeReceiver() must equalTo("recipient@example.com")
  //      msg.getMimeMessage().getSubject() must equalTo("subject")
  //    }
}


