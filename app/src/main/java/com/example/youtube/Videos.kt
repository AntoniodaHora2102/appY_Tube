package com.example.youtube

import java.text.SimpleDateFormat
import java.util.*

//mapeando as propriedades do json
//home
data class Video(
    var id: String,
    var thumbnailUrl: String,
    var title: String,
    var publishedAt: Date,
    var viewsCount: Long,
    var viewsCountLabel: String,
    var duration: Int,
    var videoUrl: String,
    var publisher: Publisher
)

data class Publisher(
    var id: String,
    var name: String,
    var pictureProfileUrl: String
)

data class ListVideo(
    val status: Int,
    val data: List<Video>
)

// informacoes para XML video_detail
class VideoBuilder {

    var id: String = ""
    var thumbnailUrl: String = ""
    var title: String = ""
    var publishedAt: Date = Date()
    var viewsCount: Long = 0
    var viewsCountLabel: String = ""
    var duration: Int = 0
    var videoUrl: String = ""
    var publisher: Publisher = PublisherBuilder().build()

        //instancia videos
        fun build() : Video = Video(
            id, thumbnailUrl, title, publishedAt, viewsCount, viewsCountLabel, duration,
            videoUrl, publisher
        )

    fun publisher(block: PublisherBuilder.() -> Unit): Publisher =
        PublisherBuilder().apply(block).build()

}

//construtor de publicadores
class PublisherBuilder {
    var id: String = ""
    var name: String = ""
    var pictureProfileUrl: String = ""

    //instancia um publisher (publicador de video)
    fun build() : Publisher =
        Publisher(id, name, pictureProfileUrl)
}

//funcao geral para o video
// DSL - funcao lambda
fun video(block: VideoBuilder.() -> Unit): Video =
    VideoBuilder().apply(block).build()


//lista de dados falsos
fun videos() : List<Video> {
    return  arrayListOf(
        video {
            id =  "UVpKBHO2fMg"
            thumbnailUrl =  "https://img.youtube.com/vi/UVpKBHO2fMg/maxresdefault.jpg"
            title = "Entrevista com Marlon Wayans | The Noite (14/08/19)"
            publishedAt = "2019-08-15".toDate()
            viewsCount = 742497
            duration = 1886
            videoUrl = ""
            publisher {
                id = "sbtthenoite"
                name =  "The Noite com Danilo Gentili"
                pictureProfileUrl = "https://yt3.ggpht.com/a/AGF-l7_3BYlSlp94WOjGe1UECUCdb73qRJVFH_t9Tw=s48-c-k-c0xffffffff-no-rj-mo"
            }
        },
        video {
            id =  "PlYUZU0H5go"
            thumbnailUrl = "https://img.youtube.com/vi/cuau8E6t2QU/maxresdefault.jpg"
            title = "Relembrando Steve Jobs"
            publishedAt = "2014-08-15".toDate()
            viewsCount = 1703
            duration = 194
            videoUrl = ""
            publisher {
                id = "UCrWWMZ6GVOM5zqYAUI44XXg"
                name =  "Tiago Aguiar"
                pictureProfileUrl =
                    "https://yt3.ggpht.com/ytc/AKedOLT2VtZ3n30tTpDyjAoZGl44EfHhajN1Zy5LYm3iiA=s88-c-k-c0x00ffffff-no-rj"
            }
        }
    )
}


//extension
fun Date.formatted() : String =
    SimpleDateFormat("d MMM yyyy", Locale("pt", "BR")).format(this)

//converter string para DATE
fun String.toDate() : Date =
    SimpleDateFormat("yyyy-mm-dd", Locale("pt", "BR")).parse(this)
