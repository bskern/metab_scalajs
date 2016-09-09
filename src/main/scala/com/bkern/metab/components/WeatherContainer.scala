package com.bkern.metab.components

import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import org.scalajs.dom.ext.Ajax
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

//for json processing
case class WeatherResponse(query: WeatherMetaObject)

case class WeatherMetaObject(count: Int, created: String, lang: String, results: WeatherResult)

case class WeatherResult(channel: WeatherData)

case class WeatherData(item: WeatherItem)

case class WeatherItem(condition: Condition, forecast: Seq[Forecast])

case class Condition(temp: String, text: String)

case class Forecast(code: String, date: String, day: String, high: String, low: String, text: String)

//for react
case class Weather(currentTemp: String, desc: String, high: String, low: String)

case class State(weather: Weather)

class Backend($: BackendScope[Unit, State]) {
  def render(s: State) = WeatherHeader(WeatherHeader.Props(s.weather.currentTemp, s.weather.desc, s.weather.high, s.weather.low))
}

object WeatherContainer {

  import upickle.default._


  val app = ReactComponentB[Unit]("weatherApp")
    .initialState(State(weather = Weather("000", "empty", "44", "55")))
    .renderBackend[Backend]
    .componentDidMount(scope => Callback {
      val url = "https://query.yahooapis.com/v1/public/yql?q=select * from weather.forecast where u='f' AND woeid in (select woeid from geo.places(1) where text=\"minneapolis\")&format=json"

      Ajax.get(url).onSuccess {
        case xhr => {
          scope.setState(State(getWeatherFromResponse(xhr.responseText))).runNow()
        }
      }
    }).build

  def getWeatherFromResponse(responseText: String): Weather = {
    val weatherResp = read[WeatherResponse](responseText)
    val weatherItem = weatherResp.query.results.channel.item
    val forecast = weatherItem.forecast.head
    val currentTemp = weatherItem.condition.temp
    val Forecast(_, _, _, high, low, desc) = forecast

    Weather(currentTemp, desc, high, low)
  }

  def apply() = app()
}
