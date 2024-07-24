package com.aghajari.axvideotimelineview

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive

internal class AXFrameDecoder(
    view: AXVideoTimelineView,
    private val listener: AXFrameDecoderListener?
) {
    private var decoderScope: CoroutineScope? = null

    internal interface AXFrameDecoderListener {
        fun frameDecoded(frame: Bitmap?, frameNumber: Int)
    }

    init {
        decoderScope = CoroutineScope(Dispatchers.IO)
    }

    fun destroy() {
        decoderScope?.cancel()
    }

    private val utils: AXFrameDecoderUtils = view.utils
    private val mediaMetadataRetriever: MediaMetadataRetriever = view.mediaMetadataRetriever

    private var frameNum: Int = 0

    fun decodeFrame(frameNum: Int) {
        decoderScope?: return
        this.frameNum = frameNum
        var bitmap: Bitmap?
        if (decoderScope!!.isActive) {
            try {
                bitmap = mediaMetadataRetriever.getFrameAtTime(
                    utils.frameTimeOffset * frameNum * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                if (decoderScope!!.isActive.not()) {
                    return
                }
                if (bitmap != null) {
                    bitmap = utils.prepareFrame(bitmap)
                }
                if (listener != null && decoderScope!!.isActive) {
                    listener.frameDecoded(bitmap, frameNum)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
