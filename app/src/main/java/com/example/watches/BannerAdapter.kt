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
import com.google.android.material.button.MaterialButton

class BannerAdapter(
    private val context: Context,
    private val bannerItems: List<BannerItem>,
    private val onVideoFinished: () -> Unit,
    private val onGrabNowClick: (BannerItem) -> Unit // Callback for button click
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    private val players = mutableMapOf<Int, ExoPlayer>()

    inner class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bannerImage: ImageView = view.findViewById(R.id.bannerImage)
        val bannerPlayerView: PlayerView = view.findViewById(R.id.bannerPlayerView)
        val textOverlay: View = view.findViewById(R.id.textOverlay)
        val title: TextView = view.findViewById(R.id.bannerTitle)
        val description: TextView = view.findViewById(R.id.bannerMainText)
        val btnGrabNow: MaterialButton = view.findViewById(R.id.btnGrabNow)

        fun bind(item: BannerItem) {
            title.text = item.title ?: ""
            description.text = item.description ?: ""

            // Handle button click
            btnGrabNow.setOnClickListener {
                onGrabNowClick(item)
            }

            if (item.type == BannerType.IMAGE) {
                bannerImage.visibility = View.VISIBLE
                bannerPlayerView.visibility = View.GONE
                textOverlay.visibility = View.VISIBLE
                
                val resId = context.resources.getIdentifier(item.resourceUri, "drawable", context.packageName)
                if (resId != 0) {
                    bannerImage.setImageResource(resId)
                }
            } else {
                bannerImage.visibility = View.GONE
                bannerPlayerView.visibility = View.VISIBLE
                textOverlay.visibility = View.GONE
                
                setupPlayer(this, item)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer(holder: BannerViewHolder, item: BannerItem) {
        if (players[holder.adapterPosition] == null) {
            val player = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                volume = 0f 
                playWhenReady = false
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            onVideoFinished()
                        }
                    }
                })

                val mediaItem = MediaItem.fromUri(Uri.parse(item.resourceUri))
                setMediaItem(mediaItem)
                prepare()
            }
            holder.bannerPlayerView.player = player
            players[holder.adapterPosition] = player
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(bannerItems[position])
    }

    override fun getItemCount(): Int = bannerItems.size

    fun playVideo(position: Int) {
        players[position]?.seekTo(0)
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
