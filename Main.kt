import java.time.LocalDateTime

// Location class
data class Location(val name: String, val latitude: Double, val longitude: Double) {
    fun getCoordinates(): String {
        return "Latitude: $latitude, Longitude: $longitude"
    }
}

// WeatherReport class
data class WeatherReport(
    val location: Location,
    val temperature: Double,
    val humidity: Int,
    val condition: String,
    val timestamp: LocalDateTime
) {
    fun getSummary(): String {
        return "Weather in ${location.name}: $temperature°C, $humidity% humidity, $condition"
    }
}

// WeatherForecast class
data class WeatherForecast(val location: Location, val forecast: List<WeatherReport>) {
    fun getForecastForDay(day: Int): WeatherReport? {
        return if (day in forecast.indices) forecast[day] else null
    }
}

// Factory Method for Weather Conditions
sealed class WeatherCondition {
    object Sunny : WeatherCondition() {
        override fun toString() = "Sunny"
    }

    object Rainy : WeatherCondition() {
        override fun toString() = "Rainy"
    }

    object Cloudy : WeatherCondition() {
        override fun toString() = "Cloudy"
    }

    companion object {
        fun create(condition: String): WeatherCondition {
            return when (condition) {
                "Sunny" -> Sunny
                "Rainy" -> Rainy
                "Cloudy" -> Cloudy
                else -> throw IllegalArgumentException("Unknown condition: $condition")
            }
        }
    }
}

// WeatherService class
class WeatherService(private val apiKey: String) {
    fun fetchCurrentWeather(location: Location): WeatherReport {
        val condition = WeatherCondition.create("Sunny") // Example
        return WeatherReport(location, 25.0, 60, condition.toString(), LocalDateTime.now())
    }

    fun fetchForecast(location: Location, days: Int): WeatherForecast {
        val forecast = List(days) {
            val condition = WeatherCondition.create(if (it % 2 == 0) "Sunny" else "Cloudy")
            WeatherReport(location, 25.0 + it, 60 - it, condition.toString(), LocalDateTime.now().plusDays(it.toLong()))
        }
        return WeatherForecast(location, forecast)
    }
}

// Adapter for converting temperature
class TemperatureAdapter(private val weatherReport: WeatherReport) {
    fun getTemperatureInFahrenheit(): Double {
        return weatherReport.temperature * 9 / 5 + 32
    }
}

// Observer pattern: WeatherObserver and WeatherNotifier
interface WeatherObserver {
    fun onWeatherUpdate(report: WeatherReport)
}

class WeatherNotifier {
    private val observers = mutableListOf<WeatherObserver>()

    fun addObserver(observer: WeatherObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: WeatherObserver) {
        observers.remove(observer)
    }

    fun notifyObservers(report: WeatherReport) {
        observers.forEach { it.onWeatherUpdate(report) }
    }
}

// Main function
fun main() {
    val location = Location("Kyiv", 50.45, 30.52)
    val weatherService = WeatherService("813cabdffc00420092671741242609")

    // Fetch current weather
    val currentWeather = weatherService.fetchCurrentWeather(location)
    println(currentWeather.getSummary())

    // Factory Method usage
    println("Weather condition created using Factory: ${WeatherCondition.create("Rainy")}")

    // Adapter usage
    val adapter = TemperatureAdapter(currentWeather)
    println("Temperature in Fahrenheit: ${adapter.getTemperatureInFahrenheit()}°F")

    // Observer usage
    val notifier = WeatherNotifier()
    notifier.addObserver(object : WeatherObserver {
        override fun onWeatherUpdate(report: WeatherReport) {
            println("Observer: Weather updated - ${report.getSummary()}")
        }
    })
    notifier.notifyObservers(currentWeather)

    // Fetch and display forecast
    val forecast = weatherService.fetchForecast(location, 5)
    forecast.forecast.forEachIndexed { index, weatherReport ->
        println("Day $index: ${weatherReport.getSummary()}")
    }
}
