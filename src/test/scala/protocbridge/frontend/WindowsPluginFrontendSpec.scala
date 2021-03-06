package protocbridge.frontend

import java.io.ByteArrayInputStream

import protocbridge.ProtocCodeGenerator

import scala.sys.process.ProcessLogger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class WindowsPluginFrontendSpec extends AnyFlatSpec with Matchers {
  if (PluginFrontend.isWindows) {
    it must "execute a program that forwards input and output to given stream" in {
      val toSend = "ping"
      val toReceive = "pong"

      val fakeGenerator = new ProtocCodeGenerator {
        override def run(request: Array[Byte]): Array[Byte] = {
          request mustBe toSend.getBytes
          toReceive.getBytes
        }
      }
      val (path, state) = WindowsPluginFrontend.prepare(fakeGenerator)
      val actualOutput = scala.collection.mutable.Buffer.empty[String]
      val process = sys.process
        .Process(path.toAbsolutePath.toString)
        .#<(new ByteArrayInputStream(toSend.getBytes))
        .run(ProcessLogger(o => actualOutput.append(o)))
      process.exitValue()
      actualOutput.mkString mustBe toReceive
      WindowsPluginFrontend.cleanup(state)
    }
  }
}
