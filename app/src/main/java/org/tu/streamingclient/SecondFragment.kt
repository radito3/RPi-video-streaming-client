package org.tu.streamingclient

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

    private lateinit var surfaceView: SurfaceView
    private var player: ExoPlayer? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        surfaceView = binding.surfaceView
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            (requireActivity() as MainActivity).supportActionBar?.hide()
            surfaceView.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.stopButton.setOnClickListener {
            findNavController().navigate(R.id.action_stop_stream)
        }

        val raspberryAddress = resolveRaspberryHostname(view)
        if (raspberryAddress == null) {
            findNavController().navigate(R.id.action_stop_stream)
            return
        }
        player = ExoPlayer.Builder(requireContext()).build()
        player?.setVideoSurfaceView(surfaceView)

        val rtspUsername = arguments?.getString("username") ?: ""
        val rtspPassword = arguments?.getString("password") ?: ""
        val videoUrl = "rtsp://$rtspUsername:$rtspPassword@$raspberryAddress:8554/stream"
        val mediaSource = createMediaSource(videoUrl)
        player?.setMediaSource(mediaSource)
        player?.addListener(onErrorListener(view))
        player?.setWakeMode(C.WAKE_MODE_NETWORK)
        player?.prepare()
        player?.playWhenReady = true
    }

    private fun resolveRaspberryHostname(view: View): String? {
        val raspberryHostname = arguments?.getString("hostname") ?: "raspberrypi"
        val hostnameResolver = HostnameResolver()
        val raspberryAddress = hostnameResolver.resolve("$raspberryHostname.local") {
            Snackbar.make(view, "${it.message}", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        if (raspberryAddress == null) {
            Snackbar.make(view, "Can not determine Raspberry Pi address", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        return raspberryAddress
    }

    private fun createMediaSource(videoUrl: String) = RtspMediaSource.Factory()
        .setDebugLoggingEnabled(true)
        .setTimeoutMs(TimeUnit.SECONDS.toMillis(5))
        .createMediaSource(MediaItem.fromUri(videoUrl))

    private fun onErrorListener(view: View) = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            if (error.cause?.message?.contains("401") == true) {
                Snackbar.make(view, "Unauthorized", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                findNavController().navigate(R.id.action_stop_stream)
                return
            }
            Snackbar.make(view, "Error playing track: ${error.cause?.message}", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        player?.release()
        player = null
    }
}
