// File: DownloadSimulator.scala
case class DownloadTask(fileName: String, downloadSpeed: Int) extends Runnable {

  override def run(): Unit =
    for percent <- 10 to 100 by 10 do
      Thread.sleep(downloadSpeed) // simulate time delay per 10%
      println(s"$fileName: $percent% downloaded")
    println(s"fileName download completed!\n")
}

object DownloadSimulator:
  def main(args: Array[String]): Unit =
    println("Starting file downloads...\n")

    // Create multiple download threads
    val file1 = new Thread(new DownloadTask("Movie.mp4", 300))
    val file2 = new Thread(new DownloadTask("Music.mp3", 200))
    val file3 = new Thread(new DownloadTask("Document.pdf", 500))
    val file4 = new Thread(new DownloadTask("GameInstaller.exe", 150))

    // Setting priority for threads
    file1.setPriority(Thread.NORM_PRIORITY)
    file2.setPriority(Thread.NORM_PRIORITY)
    file3.setPriority(Thread.MAX_PRIORITY)
    file4.setPriority(Thread.MIN_PRIORITY)

    // Start all threads (run concurrently)
    file1.start()
    file2.start()
    file3.start()
    file4.start()

    // Wait for all threads to complete before exiting
    file1.join()
    file2.join()
    file3.join()
    file4.join()

    println("All downloads completed!")
