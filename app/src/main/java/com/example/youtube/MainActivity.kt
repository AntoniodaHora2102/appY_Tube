package com.example.youtube


import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.fragment_recycle_main.*
import kotlinx.android.synthetic.main.video_detail.*
import kotlinx.android.synthetic.main.video_detail.view.*
import kotlinx.android.synthetic.main.video_detail_content.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var videoAdapter: VideoAdapter

    private lateinit var youtubePlayer: YoutubePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setSupportActionBar(toolbar) // ira chamar a toolbar da activity e não do sistema android
        supportActionBar?.title = "" // retira o title da toobar principal

        val videos = mutableListOf<Video>()
        videoAdapter =  VideoAdapter(videos) { video ->
            showOverlayView(video)
        }


        //cor (transparente) da view do VIDEO_PLAYER
        view_layer.alpha = 0F


//        var logoYoutbe = findViewById<ImageView>(R.id.logo_youtube)
//        logoYoutbe.setTag(R.drawable.logo_youtube)
//
//
//        when(resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
//
//            Configuration.UI_MODE_NIGHT_YES -> {
//                logoYoutbe.imageTintList = ColorStateList.valueOf(Color.WHITE)
//            }
//            Configuration.UI_MODE_NIGHT_NO -> {
//            }
//        }


        rv_main.layoutManager = LinearLayoutManager(this)
        rv_main.adapter = videoAdapter

        //coroutines - process parelelos
        CoroutineScope(Dispatchers.IO).launch {
           val res = async { getVideo()}
           val listVideo =  res.await()

            withContext(Dispatchers.Main) {
                //vai trazer a lista e delegar a recycleView
                listVideo?.let {
                    videos.clear()
                    videos.addAll(listVideo.data)
                    videoAdapter.notifyDataSetChanged()

                    //remover a progress bar
//                    main_container.removeView(progress_recycler)
                    motion_container.removeView(progress_recycler)
                    //progress_recycler.visibility = View.GONE

                }
            }
        }

        //controla a seekBAR
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    youtubePlayer.seek(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        //funcao - videoPlay
        preparePlayer()

    }

    //metodo para tocao video
    private fun preparePlayer() {
        youtubePlayer = YoutubePlayer(this)

        youtubePlayer.youtubePlayerListener = object : YoutubePlayer.YoutubePlayerListener {
            override fun onPrepared(duration: Int) {
            }

            override fun onTrackTime(currentPosition: Long, percente: Long) {

                motion_container.seek_bar.progress = percente.toInt()
                motion_container.current_time.text = currentPosition.formatTime()
                println(currentPosition.formatTime())
            }
        }

        surface_player.holder.addCallback(youtubePlayer)
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubePlayer.release()
    }

    //ira pausar o app quando houve uma ligacao ou qualquer coisa do tipo
    override fun onPause() {
        super.onPause()
        youtubePlayer.pause()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun showOverlayView(video: Video) {

        //animação da view_layer - VIDEO_DETAIL.XML
        view_layer.animate().apply {
            duration = 400
            alpha(0.5F)
        }


        //transicoes do motionLayout-activity_main.xml
        motion_container.setTransitionListener(object  : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int,
                                             positive: Boolean, progress: Float) {
               Log.i("Transition", "$progress")
                //println("Transition $progress")
            }

            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
                Log.i("Transition", "$startId , $endId")
            }

            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int,
                                            endId: Int, progress: Float) {
                Log.i("Progress", "$progress")

                if (progress > 0.5f) {
                    view_layer.alpha = 1.0f - progress
                } else {
                    view_layer.alpha = 0.5f
                }
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                Log.i("Transition", "$currentId")
            }
        })

        video_player.visibility = View.GONE // ira tira o video player da frente
        youtubePlayer.setUrl(video.videoUrl) // requisição HTTP

        val detailAdapter = VideoDetailAdapter(videos())
        rv_similar.layoutManager = LinearLayoutManager(this)
        rv_similar.adapter = detailAdapter

        content_channel.text = video.publisher.name
        content_title.text = video.title
        Picasso.get().load(video.publisher.pictureProfileUrl).into(img_channel)

        detailAdapter.notifyDataSetChanged()

    }


    private fun getVideo(): ListVideo? {
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .get()
            .url("api")
            .build()

        return try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                GsonBuilder().create()
                    .fromJson(response.body()?.string(), ListVideo::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}