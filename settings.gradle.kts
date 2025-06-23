import java.io.FileOutputStream
import java.net.URL

rootProject.name = "SweetFlight"

@Suppress("deprecation")
fun download(pair: Pair<String, File>) {
    val input = URL(pair.first).openStream()
    input.use {
        FileOutputStream(pair.second).use { out ->
            val buffer = ByteArray(1024 * 1024)
            var length: Int
            while (it.read(buffer).also { length = it } != -1) {
                out.write(buffer, 0, length)
            }
        }
    }
}
val libs = settingsDir.absoluteFile.resolve("libs").also { it.mkdirs() }
val residence = libs.resolve("Residence.jar")
if (!residence.exists()) {
    println("Downloading Residence-5.1.5.2...")
    download("https://zrips.net/Residence/download.php?file=Residence5.1.5.2.jar" to residence)
}
