package com.example.audio.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.io.IOException

class AudioPlayer (private val context: Context) : MediaPlayer() {
    private var player: MediaPlayer? = null

    fun playAudio(fileUri: Uri) {
        if (player == null) {
            //MediaPlayer.create(context, fileUri).apply{}
            player = MediaPlayer().apply {
                try {
                    setDataSource(context, fileUri)
                    prepare()
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, "prepare() failed")
                }

            }
        }
        super.start()
        Toast.makeText(this.context, "playing file: $fileUri", Toast.LENGTH_SHORT).show()
    }

    fun pauseAudio(){
        this.player?.let {
            if (it.isPlaying) {
                it.pause()
                Toast.makeText(this.context, "playing paused", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stopAudio(){
        this.player?.let {
            if (it.isPlaying) {
                it.stop()
                it.reset() // Reset the MediaPlayer for future use
                Toast.makeText(this.context, "stopped playing", Toast.LENGTH_SHORT).show()
            }
            it.release()
            this.player = null
        }
    }

}