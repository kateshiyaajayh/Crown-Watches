package com.example.watches

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(
    private val context: Context,
    private val sliderItems: List<SliderItem>
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    private val players = mutableMapOf<Int, ExoPlayer>()

    inner class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.sliderImage)
        val playerView: PlayerView = view.findViewById(R.id.bannerPlayerView)
        val title: TextView = view.findViewById(R.id.sliderTitle)
        val subtitle: TextView = view.findViewById(R.id.sliderSubtitle)
        val textContainer: View = view.findViewById(R.id.textContainer)

        fun bind(item: SliderItem) {
            title.text = item.title
            subtitle.text = item.subtitle

            if (item.isVideo) {
                image.visibility = View.GONE
                playerView.visibility = View.VISIBLE
                textContainer.visibility = View.GONE // Hide text for full video experience
                setupPlayer(this, item)
            } else {
                image.visibility = View.VISIBLE
                playerView.visibility = View.GONE
                textContainer.visibility = View.VISIBLE
                image.setImageResource(item.imageRes)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer(holder: SliderViewHolder, item: SliderItem) {
        if (players[holder.adapterPosition] == null) {
            val player = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = false
                val mediaItem = MediaItem.fromUri(Uri.parse(item.videoUri ?: ""))
                setMediaItem(mediaItem)
                prepare()
            }
            holder.playerView.player = player
            players[holder.adapterPosition] = player
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slider_item, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(sliderItems[position])
    }

    override fun getItemCount(): Int = sliderItems.size

    fun playVideo(position: Int) {
        players[position]?.play()
    }

    fun pauseVideo(position: Int) {
        players[position]?.pause()
    }

    fun releasePlayers() {
        players.values.forEach {
            it.stop()
            it.release()
        }
        players.clear()
    }
}

data class SliderItem(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val isVideo: Boolean = false,
    val videoUri: String? = null
)
