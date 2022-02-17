package org.tu.streamingclient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBindings
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.material.snackbar.Snackbar
import org.tu.streamingclient.databinding.FragmentSecondBinding
import org.tu.streamingclient.util.HostnameResolver
import java.util.concurrent.TimeUnit

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    //"rtsp://192.168.1.8:8554/stream"
    //"rtsp://raspberrypi.local:8554/stream"
//    private val videoUrl: String = "rtsp://192.168.1.8:8554/stream"
    private var surfaceView: SurfaceView? = null
    private var player: ExoPlayer? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        val raspberryHostname = "raspberrypi.local" //maybe not the best idea to hardcode this
        val hostnameResolver = HostnameResolver()
        val raspberryAddress = hostnameResolver.resolve(raspberryHostname) {
            Snackbar.make(view, "${it.message}", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        if (raspberryAddress == null) {
            Snackbar.make(view, "Can not determine raspberry pi address", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return
        }
        Snackbar.make(view, "Raspberry Pi address: $raspberryAddress", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
        val videoUrl = "rtsp://$raspberryAddress:8554/stream"

        surfaceView = ViewBindings.findChildViewById(view, R.id.surfaceView)
        player = ExoPlayer.Builder(requireContext()).build()
        player?.setVideoSurfaceView(surfaceView)
        val mediaSource = RtspMediaSource.Factory()
                                         .setDebugLoggingEnabled(true)
                                         .setTimeoutMs(TimeUnit.SECONDS.toMillis(5))
                                         .createMediaSource(MediaItem.fromUri(videoUrl))
        player?.setMediaSource(mediaSource)
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Snackbar.make(view, "Error playing track: ${error.cause?.message}", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        })
        player?.setWakeMode(C.WAKE_MODE_NETWORK)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        player?.release()
        player = null
    }
}