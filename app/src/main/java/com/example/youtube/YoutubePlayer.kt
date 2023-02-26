package com.example.youtube

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.view.SurfaceHolder
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class YoutubePlayer(private val context: Context) : SurfaceHolder.Callback {

    private var mediaPlayer: SimpleExoPlayer? = null

    var youtubePlayerListener: YoutubePlayerListener? =  null
    private lateinit var runnable: Runnable //ira rexecutar a cada um segundo
    private val handler = Handler() // executa periodicamente

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        //se não tiver instancia
        if (mediaPlayer == null) {

            //vamos instancia-lo
            mediaPlayer = SimpleExoPlayer.Builder(context).build()

            //ira rodar nativamente
            mediaPlayer?.setVideoSurfaceHolder(holder)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        //ira destruir o processo caso - para liberar espaço
        mediaPlayer?.release()
    }

    //vamos setar a url do video
    fun setUrl(url: String) {

        //vamos verificar se ele existe
        mediaPlayer?.let {
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, "youtube")
            )

            val videoSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(url))

            it.prepare(videoSource)

            it.addListener(object : Player.EventListener {
                //ira escutar quando o video comecar a tocar
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    // se o video estiver tocando
                    if (isPlaying) {
                        //ira ser rodada uma vez por segundo
                        //delegar para atividade
                        trackTime()
                    }
                }
            })
            play()
        }
    }

    private fun trackTime() {
        mediaPlayer?.let {

            //tempo total do video transformar em parcentual
            // a cada 1s o youtuberPlayerListener será chamado
            youtubePlayerListener?.onTrackTime(
                it.currentPosition, it.currentPosition * 100 / it.duration)

            if (it.isPlaying) {
                runnable = Runnable {
                    trackTime()
                }

                //tempo de delay 1s
                handler.postDelayed(runnable, 1000)
            }
        }
    }

    //tocar o video
    fun play() {
        mediaPlayer?.playWhenReady = true
    }

    //pausa o video
    fun pause() {
        mediaPlayer?.playWhenReady = false
    }

    //destruirá o video
    fun release() {
        mediaPlayer?.release()
    }

    //funcao de buscar o tempo do video atraves da SEEKBAR
    fun seek(progress: Long) {
        if (progress > 0 ) {
            mediaPlayer?.let {
                val seek = progress * it.duration / 100
                it.seekTo(seek)
            }
        }
    }

    //progressao de time do video
    interface YoutubePlayerListener {

        fun onPrepared(duration: Int)

        //tempo corrido do video
        fun onTrackTime(currentPosition: Long, percent: Long)
    }
}