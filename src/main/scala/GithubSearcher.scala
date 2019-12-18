
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.ahc._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.annotation.tailrec

import scala.concurrent.ExecutionContext.Implicits._
import play.api.libs.ws.JsonBodyReadables._

object GithubSearcher extends App {

  val searchFor = args.mkString(" ")

  if (searchFor.isEmpty) {
    Console.err.println("Please supply a search string")
    System.exit(-1)
  }

  case class Item(language: Option[String]) extends AnyVal

  private implicit val itemReads = Json.reads[Item]

  case class Results(items: Seq[Item]) extends AnyVal

  private implicit val resultReads = Json.reads[Results]

  private implicit val system = ActorSystem("github-searcher-ActorSystem")

  val wsClient = StandaloneAhcWSClient()

  system.registerOnTermination {
    wsClient.close()
    System.exit(0)
  }

  private implicit val materializer = Materializer.matFromSystem

  val items = getAllItems(s"""$searchFor""")

  val summary = resultsToSummary(items)

  summary.foreach { case (name, count) => println(s"$name: $count") }

  val total = summary.map(_._2).sum

  println(s"=> $total total result(s) found")

  system.terminate()

  def getAllItems(text: String): Seq[Item] = {

    @tailrec
    def callForPage(page: Int = 1, in: Seq[Item] = Seq.empty[Item]): Seq[Item] = {

      def call(): Future[Seq[Item]] = {

        wsClient.url(s"https://api.github.com/search/repositories?page=$page&q=$text+in:description")
          .addHttpHeaders("Accept" -> "application/vnd.github.v3+json").get().map { response =>

          try{
            val body = response.body[JsValue]

            val results = body.validate[Results]
            results.fold(
              _ => Seq.empty[Item],
              res => res.items
            )
          } catch {
            case exception: Exception=>
              Console.err.println(exception)
              System.exit(-2)
              Seq.empty[Item]
          }

        }
      }

      val itemsF = call()
      // no point throwing up lots of future code, as we must get them in sequence to decide when to end
      val items = Await.result(itemsF, 10.seconds)

      if (items.isEmpty || page > 1000 / 30) in else callForPage(page + 1, items ++ in)
    }

    callForPage()
  }

  def resultsToSummary(items: Seq[Item]): Seq[(String, Int)] = {
    val filteredItems = items.collect {
      case Item(Some(language)) if !language.isEmpty => language
    }

    filteredItems.groupBy(identity).view.mapValues(_.size).toSeq.sortBy(-_._2)
  }
}
