package com.example.youtube

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.video_detail.view.*
import kotlin.math.abs


//motionLayout Personalizado
class TouchMotionLayout(context: Context, attributeSet: AttributeSet) :
    MotionLayout(context, attributeSet) {


    private val iconArrowDown: ImageView by lazy {
        findViewById<ImageView>(R.id.hide_player)
    }

    private val imgBase: ImageView by lazy {
        findViewById<ImageView>(R.id.video_player)
    }


    private val playButton: ImageView by lazy {
        findViewById<ImageView>(R.id.play_button)
    }

    private val seekBar: SeekBar by lazy {
        findViewById<SeekBar>(R.id.seek_bar)
    }

    //verifica se está clicando ou arrastando
    private var startX: Float? = null
    private var startY: Float? = null

    //verifica se o video está pausado
    private var isPaused = false

    //animação FadeIn e FadeOut
    private lateinit var animFadeIn: AnimatorSet
    private lateinit var animFadeOut: AnimatorSet


    //vamos interceptar o touch de um evento
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val isInTarget = touchEventInsideTargetView(imgBase, event!!)
        val isInProgress = (progress > 0.0f && progress < 1.0f)

        return if (isInProgress || isInTarget) {
            super.onInterceptTouchEvent(event)
        } else {
         false
        }
    }

    //verificaremos em qual parte está sendo clicada
    private fun touchEventInsideTargetView(v: View, ev: MotionEvent) : Boolean {

        if (ev.x > v.left && ev.x < v.right ) {
            if (ev.y > v.top && ev.y < v.bottom) {
                return true
            }
        }

        return false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        //verifica se a tela foi clicada
        when(ev.action) {
            MotionEvent.ACTION_DOWN -> {
                //quando clicarmos na tela vamos pegar o eixo X e Y
                startX = ev.x
                startY = ev.y
            }
            MotionEvent.ACTION_UP -> {
                //aqui será o verso da ação acima
                val endX = ev.x
                val endY = ev.y

                //irá verifica se o eixo foi realmente clicado
                if (isAClick(startX!!, endX, startY!!, endY)) {

                    //ira verifica se o click foi dentro da image
                    if (touchEventInsideTargetView(imgBase, ev)) {

                        //mostrar os controladores do video
                        if (doClick(imgBase)) {
                            return true
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        //iremos a pegar a diferenca entre os eixos
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)

        return !(differenceX > 200 || differenceY > 200)
    }

    private fun doClick(view: View): Boolean {

        //verificar se o click foi manipulado
        var isClickHandled = false

        if (progress < 0.05f) {
            isClickHandled = true

            //verifica a view se estiver pausado ou não
            when(view) {
                imgBase -> {
                    if (isPaused) {

                    } else {
                     // funcao de animacao do objetos da tela
                      animateFade {
                        animFadeOut.startDelay = 1000
                        animFadeOut.start()
                       }
                    }
                }
            }
        }

        return isClickHandled
    }

    private fun animateFade(onAnimationEndOn: () -> Unit) {
        animFadeOut = AnimatorSet()
        animFadeIn = AnimatorSet()

        //funcao para o evento de fade para todas as views
        fade(
            animFadeIn, arrayOf(
                play_button,
                hide_player,
                next_button,
                previous_button,
                playlist_player,
                full_player,
                share_player,
                more_player,
                current_time,
                duration_time
                ), true
        )

        //animacao
        animFadeIn.play(
            ObjectAnimator.ofFloat(view_frame, View.ALPHA, 0f, .5f)
        )

        //animacao seek_bar
        val valueFadeIn = ValueAnimator.ofInt(0, 255)
            .apply {
                addUpdateListener {
                    seek_bar.thumb.mutate().alpha = it.animatedValue as Int
                }
                duration = 200
            }
        animFadeIn.play(valueFadeIn)

        fade(animFadeOut, arrayOf(
            play_button,
            hide_player,
            next_button,
            previous_button,
            playlist_player,
            full_player,
            share_player,
            more_player,
            current_time,
            duration_time
        ),
            false
        )
        val valueFadeOut = ValueAnimator.ofInt(255, 0)
            .apply {
                addUpdateListener {
                    seek_bar.thumb.mutate().alpha = it.animatedValue as Int
                }
                duration = 200
            }
        animFadeOut.play(valueFadeOut)

        animFadeIn.addListener(object  : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                onAnimationEndOn.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })

        animFadeIn.start()
    }

    private fun fade(animatorSet: AnimatorSet, view: Array<View>, toZero: Boolean) {

        //ira trafegar por todas as views
        view.forEach {
            animatorSet.play(
            //altera o valor float das propriedades da view específica
            ObjectAnimator.ofFloat(
                it, View.ALPHA,

                //ira aplicar o FadeIn e FadeOut
                if (toZero) 0f else 1f,
                if (toZero) 1f else 0f
                )
            )
        }
    }
}