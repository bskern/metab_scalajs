package com.bkern.metab.components

import com.bkern.metab.components.HNContainer.HNItem
import com.bkern.metab.components.SubredditContainer.{RedditData, RedditLink}
import com.bkern.metab.css.GlobalStyle
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import scalacss.ScalaCssReact._
import scalacss._
import scala.concurrent.Future

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
object SiteContainer {

  import upickle.default._

  @inline private def Style = GlobalStyle

  class Backend($: BackendScope[Unit, State]) {
    def render(s: State) = {
      <.div(Style.app,
        WeatherContainer(),
        <.div(Style.container,
          <.div(Style.row,
            <.div(Style.col(4),
              SubredditContainer(SubredditContainer.Props("scala", s.scala))
            ),
            <.div(Style.col(4),
              SubredditContainer(SubredditContainer.Props("elm", s.elm))
            ),
            <.div(Style.col(4),
              SubredditContainer(SubredditContainer.Props("reactjs", s.reactjs))
            )
          ),
          <.div(Style.row,
            <.div(Style.col(6),
              HNContainer(HNContainer.Props("Top Stories",s.hnTop))),
            <.div(Style.col(6),
              HNContainer(HNContainer.Props("Ask", s.hnAsk)))
          )
        ))
    }
  }

  case class State(scala: Seq[RedditLink], elm: Seq[RedditLink], reactjs: Seq[RedditLink], hnTop: Seq[HNItem], hnAsk: Seq[HNItem])

  case class tempHNAsk(title: String, id: Double)

  def toHNItem(title: String, id: Double): HNItem = {
    HNItem(title, s"https://news.ycombinator.com/item?id=${id}")
  }
  val component = ReactComponentB[Unit]("siteContainer")
    .initialState(State(Nil, Nil, Nil, Nil, Nil))
    .renderBackend[Backend]
    .componentDidMount(scope => Callback {

      val subreddits = Seq("scala", "elm", "reactjs")
      val urlApiPrefix = "https://www.reddit.com/r/"
      subreddits.foreach { subreddit =>
        Ajax.get(s"$urlApiPrefix$subreddit.json").onSuccess {
          case xhr => {
            val data = getRedditDataFromResponse(xhr.responseText)
            subreddit match {
              case "scala" => scope.modState(_.copy(scala = data)).runNow()
              case "elm" => scope.modState(_.copy(elm = data)).runNow()
              case "reactjs" => scope.modState(_.copy(reactjs = data)).runNow()
            }
          }
        }
      }

      for {
        rawIdsTop <- Ajax.get("https://hacker-news.firebaseio.com/v0/topstories.json")
        rawIdsAsk <- Ajax.get("https://hacker-news.firebaseio.com/v0/askstories.json")
        idsAsk <- Future.successful(read[Seq[Double]](rawIdsAsk.responseText))
        idsTop <- Future.successful(read[Seq[Double]](rawIdsTop.responseText))
        askRaw <- loadIndividualStories(idsAsk)
        topRaw <- loadIndividualStories(idsTop)
      } yield {
        val askStories = askRaw.map { xhr =>
          val temp =read[tempHNAsk](xhr.responseText)
          toHNItem(temp.title, temp.id)
        }
        val topStories = topRaw.map { xhr =>
          read[HNItem](xhr.responseText)
        }
        scope.modState(_.copy(hnTop = topStories,hnAsk = askStories)).runNow()
      }
    }).build

  case class RedditWrapper(children: Seq[RedditData])

  case class RedditResponse(data: RedditWrapper)

  def getRedditDataFromResponse(responseText: String): Seq[RedditLink] = {
    val raw = read[RedditResponse](responseText)
    raw.data.children.map(_.data)
  }

  def loadIndividualStories(ids: Seq[Double]): Future[Seq[dom.XMLHttpRequest]] = {
    Future.sequence(ids.take(15).map(getHNItem(_)))
  }

  def getHNItem(id: Double): Future[dom.XMLHttpRequest] = {
    val url = s"https://hacker-news.firebaseio.com/v0/item/$id.json"
    Ajax.get(url)
  }


  def apply() = component()
}
