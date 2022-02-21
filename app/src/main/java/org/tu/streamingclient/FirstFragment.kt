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
            tryStartStream(view)
        }
    }

    private fun tryStartStream(view: View) {
        val hostname = view.findViewById<EditText>(R.id.server_hostname_textfield).text.toString()
        if (hostname.isEmpty()) {
            displayErrorMessage(view, "Hostname")
            return
        }
        val username = view.findViewById<EditText>(R.id.server_username_textfield).text.toString()
        if (username.isEmpty()) {
            displayErrorMessage(view, "Username")
            return
        }
        val password = view.findViewById<EditText>(R.id.server_password_textfield).text.toString()
        if (password.isEmpty()) {
            displayErrorMessage(view, "Password")
            return
        }

        val args = bundleOf(
            "hostname" to hostname.trim(),
            "username" to username.trim(),
            "password" to password.trim()
        )
        findNavController().navigate(R.id.action_start_stream, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private fun displayErrorMessage(view: View, fieldName: String) {
            Snackbar.make(view, "$fieldName is empty", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}