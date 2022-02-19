package org.tu.streamingclient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.tu.streamingclient.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

//    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startButton.setOnClickListener {
            val hostname = view.findViewById<EditText>(R.id.server_hostname_textfield).text.toString()
            if (hostname.isEmpty()) {
                Snackbar.make(view, "Hostname empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                return@setOnClickListener
            }
            val args = bundleOf("hostname" to hostname)
            findNavController().navigate(R.id.action_start_stream, args)
        }

//        thread {
//            requestQueue = Volley.newRequestQueue(this)
//            val url = "https://www.google.com"
//            val customTag = "custom-tag-01"
//
//            val stringRequest = StringRequest(
//                Request.Method.GET, url,
//                Response.Listener<String> { response ->
//                    println("Response is: ${response.substring(0, 500)}")
//                },
//                Response.ErrorListener { println("That didn't work!") })
//            stringRequest.tag = customTag
//
//            requestQueue.add(stringRequest)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        requestQueue.cancelAll("custom-tag-01")
    }
}